package com.ramitsuri.expensereports.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Report
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.model.toList
import com.ramitsuri.expensereports.repository.MainRepository
import com.ramitsuri.expensereports.settings.Settings
import com.ramitsuri.expensereports.utils.format
import com.ramitsuri.expensereports.utils.formatPercent
import com.ramitsuri.expensereports.utils.formatRounded
import com.ramitsuri.expensereports.utils.minus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Month
import java.math.BigDecimal

class HomeViewModel(
    private val mainRepository: MainRepository,
    private val clock: Clock = Clock.System,
    private val settings: Settings,
) : ViewModel() {

    private val selectedNetWorthPeriod = MutableStateFlow(HomeViewState.Period.AllTime)
    private val selectedSavingsRatePeriod = MutableStateFlow(HomeViewState.Period.ThisYear)

    init {
        viewModelScope.launch {
            mainRepository.refresh()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState = combine(
        selectedSavingsRatePeriod,
        selectedNetWorthPeriod,
    ) { selectedSavingsRatePeriod, selectedNetWorthPeriod ->
        selectedSavingsRatePeriod to selectedNetWorthPeriod
    }.flatMapLatest { (selectedSavingsRatePeriod, selectedNetWorthPeriod) ->
        combine(
            mainRepository.getCurrentBalances(),
            mainRepository.getReport(
                reportName = ReportNames.NetWorth.name,
                monthYears = selectedNetWorthPeriod.toMonthYears()
            ),
            mainRepository.getReport(
                reportName = ReportNames.SavingsRate.name,
                monthYears = selectedSavingsRatePeriod.toMonthYears()
            ),
        ) { currentBalances, netWorthReport, savingsRatesReport ->
            HomeViewState(
                currentBalanceGroups = getCurrentBalanceGroups(currentBalances),
                netWorths = getNetWorths(netWorthReport),
                selectedNetWorthPeriod = selectedNetWorthPeriod,
                selectedSavingsRatePeriod = selectedSavingsRatePeriod,
                savingsRates = getSavingsRates(savingsRatesReport)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeViewState(
            selectedNetWorthPeriod = selectedNetWorthPeriod.value,
            selectedSavingsRatePeriod = selectedSavingsRatePeriod.value,
        ),
    )

    private fun getSavingsRates(report: Report?): List<HomeViewState.SavingsRate> {
        if (report == null) {
            return listOf()
        }
        val incomes = report.accounts[0].monthTotals
        val taxes = report.accounts[1].monthTotals
        val expenses = report.accounts[2].monthTotals
        val savingsRate = incomes.map { (monthYear, income) ->
            val tax = taxes[monthYear] ?: BigDecimal.ZERO
            val expense = expenses[monthYear] ?: BigDecimal.ZERO
            val afterTaxIncome = income - tax
            val savings = afterTaxIncome - expense
            val savingsRate = savings.div(afterTaxIncome)
            HomeViewState.SavingsRate(
                monthYear = monthYear,
                savingsRate = savingsRate.formatPercent()
            )
        }
        return savingsRate
    }

    private fun getNetWorths(report: Report?): List<HomeViewState.NetWorth> {
        if (report == null) {
            return listOf()
        }
        return report
            .accounts
            .first()
            .monthTotals
            .map { (monthYear, total) ->
                HomeViewState.NetWorth(
                    monthYear = monthYear,
                    netWorth = total.formatRounded(),
                )
            }
    }

    private fun getCurrentBalanceGroups(currentBalances: List<CurrentBalance>) = currentBalances
        .groupBy { it.groupName }
        .map { (groupName, currentBalances) ->
            if (groupName == null) {
                HomeViewState.CurrentBalanceGroup(
                    currentBalances = currentBalances,
                )
            } else {
                HomeViewState.CurrentBalanceGroup(
                    groupName = groupName,
                    groupTotal = currentBalances.sumOf { it.balance }.format(),
                    currentBalances = currentBalances,
                )
            }
        }

    private suspend fun HomeViewState.Period.toMonthYears(): List<MonthYear> {
        val timeZone = settings.getTimeZone()
        val now = MonthYear.now(clock, timeZone)
        val start = when (this) {
            HomeViewState.Period.AllTime -> {
                MonthYear(Month.JANUARY, 2021)
            }

            HomeViewState.Period.LastThreeYears -> {
                now.minus(DateTimePeriod(years = 3))
            }

            HomeViewState.Period.OneYear -> {
                now.minus(DateTimePeriod(years = 1))
            }

            HomeViewState.Period.ThisYear -> {
                now.copy(month = Month.JANUARY)
            }
        }
        return (start..now).toList()
    }
}
