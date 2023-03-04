package com.ramitsuri.expensereports.viewmodel

import com.ramitsuri.expensereports.data.Error
import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.Response
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.ui.Account
import com.ramitsuri.expensereports.ui.FilterItem
import com.ramitsuri.expensereports.ui.Month
import com.ramitsuri.expensereports.ui.getNewItemsOnItemClicked
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.utils.LogHelper
import com.ramitsuri.expensereports.utils.ReportCalculator
import com.ramitsuri.expensereports.utils.ReportView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailReportViewModel(
    private val repository: ReportsRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _state: MutableStateFlow<ReportsViewState> = MutableStateFlow(ReportsViewState())
    val state: StateFlow<ReportsViewState> = _state

    private lateinit var calculator: ReportCalculator

    init {
        reportSelected(selectedYear = Year(year = DEFAULT_YEAR))
    }

    fun onErrorShown() {
        _state.update {
            it.copy(error = null)
        }
    }

    fun reportSelected(selectedYear: Year) {
        _state.update {
            it.copy(years = it.years.map { year ->
                Year(year.year, selected = year.year == selectedYear.year)
            })
        }
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            repository.getReport(selectedYear.year, reportType).collect { response ->
                when (response) {
                    is Response.Success -> {
                        onReportAvailableForFirstTime(response.data)
                    }

                    is Response.Failure -> {
                        LogHelper.e(TAG, "Error: ${response.error}")
                        _state.update {
                            it.copy(loading = false)
                        }
                    }
                }
            }
        }
    }

    fun onMonthClicked(month: FilterItem) {
        if (month !is Month) {
            return
        }
        _state.update {
            it.copy(loading = true)
        }
        val currentMonths = _state.value.months
        val newMonths = getNewItemsOnItemClicked(filterItems = currentMonths, filterItem = month)
        _state.update {
            it.copy(months = newMonths)
        }
        recalculate()
    }

    fun onAccountClicked(account: FilterItem) {
        if (account !is Account) {
            return
        }
        _state.update {
            it.copy(loading = true)
        }
        val currentAccounts = _state.value.accounts
        val newAccounts =
            getNewItemsOnItemClicked(filterItems = currentAccounts, filterItem = account)
        _state.update {
            it.copy(accounts = newAccounts)
        }
        recalculate()
    }

    fun onViewSelected(selectedView: View) {
        _state.update {
            it.copy(loading = true)
        }
        _state.update {
            it.copy(views = it.views.map { chart ->
                chart.copy(selected = chart.type == selectedView.type)
            })
        }
        recalculate()
    }

    private fun recalculate() {
        viewModelScope.launch {
            val selectedMonths = _state.value.months
                .filter { !it.isAllFilterItem && it.selected }
                .mapNotNull { (it as? Month)?.month }
            val selectedAccounts = _state.value.accounts
                .filter { !it.isAllFilterItem && it.selected }
                .mapNotNull { (it as? Account)?.accountName }
            val selectedBy = when (_state.value.views
                .first { it.selected }.type) {
                ViewType.TABLE -> ReportCalculator.By.FULL
                ViewType.BAR_MONTH -> ReportCalculator.By.MONTH
                ViewType.BAR_ACCOUNT -> ReportCalculator.By.ACCOUNT
            }

            val reportView = calculator.calculate(
                selectedMonths = selectedMonths,
                selectedAccounts = selectedAccounts,
                by = selectedBy
            )
            _state.update {
                it.copy(
                    loading = false,
                    report = reportView
                )
            }
        }
    }

    private suspend fun onReportAvailableForFirstTime(initialReport: Report) {
        calculator = ReportCalculator(initialReport, dispatchers.default)
        val calculatedReport =
            calculator.calculate(by = ReportCalculator.By.FULL) as? ReportView.Full
        if (calculatedReport == null) {
            _state.update {
                it.copy(loading = false)
            }
            return
        }
        val monthsFromInitialReport = calculator.getMonths()
            .map { monthNumber ->
                Month(
                    month = monthNumber,
                    selected = calculatedReport.total.monthAmounts.keys.contains(monthNumber),
                    id = monthNumber
                )
            }
        val months: List<Month> = listOf(
            Month(
                month = FilterItem.ALL_ID,
                selected = monthsFromInitialReport.size == calculatedReport.total.monthAmounts.size,
                id = FilterItem.ALL_ID
            )
        ) + monthsFromInitialReport

        val accountsFromInitialReport = calculator.getAccounts()
            .mapIndexed { index, accountName ->
                Account(
                    accountName,
                    selected = calculatedReport.accountTotals.count { it.name == accountName } != 0,
                    id = index
                )
            }
        val accounts: List<Account> = listOf(
            Account(
                selected = accountsFromInitialReport.size == calculatedReport.accountTotals.size,
                id = FilterItem.ALL_ID
            )
        ) + accountsFromInitialReport
        _state.update {
            it.copy(
                loading = false,
                report = calculatedReport,
                months = months,
                accounts = accounts
            )
        }
    }

    companion object {
        private const val TAG = "ExpensesVM"

        private val reportType = ReportType.EXPENSE
    }
}

data class ReportsViewState(
    val loading: Boolean = false,
    val years: List<Year> = listOf(
        Year(2023),
        Year(2022),
        Year(2021)
    ),
    val views: List<View> = listOf(
        View(ViewType.TABLE),
        View(ViewType.BAR_ACCOUNT),
        View(ViewType.BAR_MONTH)
    ),
    val months: List<FilterItem> = listOf(),
    val accounts: List<FilterItem> = listOf(),
    val report: ReportView? = null,
    val error: Error? = null
)

data class Year(val year: Int, val selected: Boolean = year == DEFAULT_YEAR)

data class View(val type: ViewType, val selected: Boolean = type == DEFAULT_CHART)

enum class ViewType {
    TABLE,
    BAR_MONTH,
    BAR_ACCOUNT
}

private const val DEFAULT_YEAR = 2023
private val DEFAULT_CHART = ViewType.TABLE