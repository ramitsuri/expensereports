package com.ramitsuri.expensereports.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.repository.MainRepository
import com.ramitsuri.expensereports.settings.Settings
import com.ramitsuri.expensereports.utils.combine
import com.ramitsuri.expensereports.utils.nowLocal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

class DashboardViewModel(
    private val mainRepository: MainRepository,
    private val settings: Settings,
    val isDesktop: Boolean,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val isRefreshing = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            mainRepository.refresh()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState =
        isRefreshing.flatMapLatest { refreshing ->
            val now = MonthYear.now(clock, timeZone)
            val monthYears =
                (Period.LastThreeYears.toMonthYears(now) + Period.ThisYear.toMonthYears(now)).distinct().sorted()
            val today = clock.nowLocal(timeZone).date
            // Look back ~2 months for recent transactions preview
            val startMonthYear = now.previous().previous()
            val startDate = LocalDate(year = startMonthYear.year, month = startMonthYear.month, day = 1)

            combine(
                mainRepository.getCurrentBalances(),
                mainRepository.getReport(
                    reportName = ReportNames.IncomeWithoutGains.name,
                    monthYears = monthYears,
                ),
                mainRepository.getReport(
                    reportName = ReportNames.AfterDeductionsExpenses.name,
                    monthYears = monthYears,
                ),
                mainRepository.getReport(
                    reportName = ReportNames.SavingsRate.name,
                    monthYears = monthYears,
                ),
                mainRepository.getTransactions(
                    description = null,
                    start = startDate,
                    end = today,
                    fromAccount = null,
                    toAccount = null,
                    minAmount = null,
                    maxAmount = null,
                ),
                mainRepository.getReport(
                    reportName = ReportNames.NetWorth.name,
                    monthYears = monthYears,
                ),
                settings.getIsPrivacyMaskedFlow(),
            ) { currentBalances, incomeReport, expensesReport, savingsReport, transactions, netWorthReport, isMasked ->
                DashboardDataTransformer.transform(
                    currentMonthYear = now,
                    currentBalances = currentBalances,
                    incomeWithoutGainsReport = incomeReport,
                    afterDeductionExpensesReport = expensesReport,
                    savingsRateReport = savingsReport,
                    transactions = transactions,
                    netWorthReport = netWorthReport,
                ).copy(
                    isLoading = refreshing,
                    isSensitiveDataMasked = isMasked,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.EMPTY,
        )

    fun onRefresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            mainRepository.refresh(forced = true)
            isRefreshing.value = false
        }
    }

    fun togglePrivacyMask() {
        viewModelScope.launch {
            val current = viewState.value.isSensitiveDataMasked
            settings.setIsPrivacyMasked(!current)
        }
    }
}
