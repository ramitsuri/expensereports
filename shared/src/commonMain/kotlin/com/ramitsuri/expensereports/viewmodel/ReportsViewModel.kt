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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ReportsViewModel(
    private val repository: ReportsRepository,
    private val dispatchers: DispatcherProvider,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    private val _state: MutableStateFlow<ReportsViewState> = MutableStateFlow(ReportsViewState())
    val state: StateFlow<ReportsViewState> = _state

    private lateinit var calculator: ReportCalculator
    private val currentYear = clock.now().toLocalDateTime(timeZone).year

    init {
        val years = (currentYear downTo 2021)
            .toList()
            .map { Year(it, selected = it == currentYear) }
        _state.update { it.copy(years = years) }
        reportSelected()
    }

    fun onErrorShown() {
        _state.update {
            it.copy(error = null)
        }
    }

    fun yearSelected(selectedYear: Year) {
        _state.update {
            it.copy(years = it.years.map { year ->
                Year(year.year, selected = year.year == selectedYear.year)
            })
        }
        reportSelected()
    }

    fun reportTypeSelected(selectedReport: ReportSelection) {
        _state.update {
            it.copy(reports = it.reports.map { report ->
                ReportSelection(report.type, selected = report.type == selectedReport.type)
            })
        }
        reportSelected()
    }

    private fun reportSelected() {
        _state.update {
            it.copy(loading = true)
        }
        val year = _state.value.years.firstOrNull { it.selected }?.year ?: currentYear
        val type = _state.value.reports.firstOrNull { it.selected }?.type ?: DEFAULT_REPORT_TYPE
        viewModelScope.launch(dispatchers.io) {
            repository.getReport(year, type).collect { response ->
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

    fun onAccountFiltersApplied(accountsFilter: List<Account>) {
        _state.update {
            it.copy(loading = true)
        }
        _state.update {
            it.copy(accounts = accountsFilter)
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
                .filter { it.selected }
                .mapNotNull { (it as? Account)?.fullName }
            val selectedBy = when (_state.value.views
                .first { it.selected }.type) {
                ViewType.TABLE -> ReportCalculator.By.FULL
                ViewType.CHART -> ReportCalculator.By.MONTH
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

        val accounts = calculator.getAccounts()
            .map { account ->
                account.copy(
                    selected = calculatedReport.accountTotals.count { it.fullName == account.fullName } != 0,
                )
            }
        _state.update {
            it.copy(
                loading = false,
                report = calculatedReport,
                months = months,
                accounts = accounts,
            )
        }
    }

    companion object {
        private const val TAG = "ExpensesVM"
    }
}

data class ReportsViewState(
    val loading: Boolean = false,
    val years: List<Year> = listOf(),
    val views: List<View> = listOf(
        View(ViewType.TABLE),
        View(ViewType.CHART),
    ),
    val reports: List<ReportSelection> = ReportType.values().filter { it != ReportType.NONE }
        .map { ReportSelection(it) },
    val months: List<FilterItem> = listOf(),
    val accounts: List<Account> = listOf(),
    val report: ReportView? = null,
    val error: Error? = null
)

data class Year(
    val year: Int,
    override val selected: Boolean
) : Selector

data class View(
    val type: ViewType,
    override val selected: Boolean = type == DEFAULT_CHART
) : Selector

data class ReportSelection(
    val type: ReportType,
    override val selected: Boolean = type == DEFAULT_REPORT_TYPE
) : Selector

sealed interface Selector {
    val selected: Boolean
}

enum class ViewType {
    TABLE,
    CHART
}

private val DEFAULT_CHART = ViewType.TABLE
private val DEFAULT_REPORT_TYPE = ReportType.EXPENSE_AFTER_DEDUCTION