package com.ramitsuri.expensereports.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Report
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.model.toList
import com.ramitsuri.expensereports.repository.MainRepository
import com.ramitsuri.expensereports.utils.format
import com.ramitsuri.expensereports.utils.formatPercent
import com.ramitsuri.expensereports.utils.minus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import java.math.BigDecimal

class HomeViewModel(
    private val mainRepository: MainRepository,
    private val isDesktop: Boolean,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {
    private val selectedNetWorthPeriod: MutableStateFlow<HomeViewState.Period> =
        MutableStateFlow(HomeViewState.Period.AllTime)
    private val isRefreshing = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            mainRepository.refresh()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState =
        combine(
            // Empty so can use combine since probably going to need it in the future
            flowOf(""),
            selectedNetWorthPeriod,
        ) { _, selectedNetWorthPeriod ->
            selectedNetWorthPeriod
        }.flatMapLatest { selectedNetWorthPeriod ->
            combine(
                mainRepository.getCurrentBalances(),
                mainRepository.getReport(
                    reportName = ReportNames.NetWorth.name,
                    monthYears = selectedNetWorthPeriod.toMonthYears(),
                ),
                mainRepository.getReport(
                    reportName = ReportNames.SavingsRate.name,
                    monthYears = HomeViewState.Period.AllTime.toMonthYears(),
                ),
                mainRepository.getReport(
                    reportName = ReportNames.AfterDeductionsExpenses.name,
                    monthYears = HomeViewState.Period.OneYear.toMonthYears(),
                ),
                isRefreshing,
            ) { currentBalances, netWorthReport, savingsRatesReport, expensesReport, isRefreshing ->
                HomeViewState(
                    expandableCardGroups =
                        getSavingsRates(savingsRatesReport)
                            .plus(getExpenses(expensesReport))
                            .plus(getCurrentBalanceGroups(currentBalances)),
                    netWorths = getNetWorths(netWorthReport),
                    selectedNetWorthPeriod = selectedNetWorthPeriod,
                    periods =
                        listOf(
                            HomeViewState.Period.ThisYear,
                            HomeViewState.Period.OneYear,
                            HomeViewState.Period.LastThreeYears,
                            HomeViewState.Period.AllTime,
                        ),
                    refreshState =
                        HomeViewState.Refresh(
                            isRefreshing = isRefreshing,
                            isPullToRefreshAvailable = !isDesktop,
                        ),
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue =
                HomeViewState(
                    selectedNetWorthPeriod = selectedNetWorthPeriod.value,
                ),
        )

    fun onNetWorthPeriodSelected(period: HomeViewState.Period) {
        selectedNetWorthPeriod.value = period
    }

    fun onRefresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            mainRepository.refresh(forced = true)
            isRefreshing.value = false
        }
    }

    private fun getExpenses(report: Report?): List<HomeViewState.ExpandableCardGroup> {
        if (report == null) {
            return emptyList()
        }
        val expensesRow = report.accounts.firstOrNull { it.order == 0 }?.monthTotals ?: return emptyList()

        val currentMonthYear = MonthYear.now(clock, timeZone)
        val expensesThisMonth =
            expensesRow
                .filterKeys { monthYear -> monthYear == currentMonthYear }
                .sum()

        val previousMonthYear = currentMonthYear.previous()
        val expensesLastMonth =
            expensesRow
                .filterKeys { monthYear -> monthYear == previousMonthYear }
                .sum()
        val expensesThisYear =
            expensesRow
                .filterKeys { (_, year) -> year == currentMonthYear.year }
                .sum()
        return listOf(
            HomeViewState.ExpandableCardGroup(
                name = "Expenses this month",
                value = expensesThisMonth.format(),
                isValuePositive = false,
                children =
                    listOf(
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "This year",
                            value = expensesThisYear.format(),
                        ),
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "Last month",
                            value = expensesLastMonth.format(),
                        ),
                    ),
            ),
        )
    }

    private fun getSavingsRates(report: Report?): List<HomeViewState.ExpandableCardGroup> {
        if (report == null) {
            return emptyList()
        }
        val incomes = report.accounts.first { it.order == 0 }.monthTotals
        val taxes = report.accounts.first { it.order == 1 }.monthTotals
        val expenses = report.accounts.first { it.order == 2 }.monthTotals

        val currentMonthYear = MonthYear.now(clock, timeZone)

        val thisYear = currentMonthYear.year
        val savingsRateThisYear =
            getSavingsRate(
                income =
                    incomes
                        .filterKeys { (_, year) -> year == thisYear }
                        .sum(),
                tax =
                    taxes
                        .filterKeys { (_, year) -> year == thisYear }
                        .sum(),
                expense =
                    expenses
                        .filterKeys { (_, year) -> year == thisYear }
                        .sum(),
            )
        val thisMonthYear = MonthYear.now(clock, timeZone)
        val savingsRateThisMonth =
            getSavingsRate(
                income =
                    incomes
                        .filterKeys { monthYear -> monthYear == thisMonthYear }
                        .sum(),
                tax =
                    taxes
                        .filterKeys { monthYear -> monthYear == thisMonthYear }
                        .sum(),
                expense =
                    expenses
                        .filterKeys { monthYear -> monthYear == thisMonthYear }
                        .sum(),
            )
        val lastMonthYear = currentMonthYear.previous()
        val savingsRateLastMonth =
            getSavingsRate(
                income =
                    incomes
                        .filterKeys { monthYear -> monthYear == lastMonthYear }
                        .sum(),
                tax =
                    taxes
                        .filterKeys { monthYear -> monthYear == lastMonthYear }
                        .sum(),
                expense =
                    expenses
                        .filterKeys { monthYear -> monthYear == lastMonthYear }
                        .sum(),
            )
        val lastThreeYears = currentMonthYear.minus(DateTimePeriod(years = 3))
        val savingsRateLastThreeYears =
            getSavingsRate(
                income =
                    incomes
                        .filterKeys { monthYear -> monthYear >= lastThreeYears }
                        .sum(),
                tax =
                    taxes
                        .filterKeys { monthYear -> monthYear >= lastThreeYears }
                        .sum(),
                expense =
                    expenses
                        .filterKeys { monthYear -> monthYear >= lastThreeYears }
                        .sum(),
            )
        val allTime = MonthYear(Month.JANUARY, 2021)
        val savingsRateAllTime =
            getSavingsRate(
                income =
                    incomes
                        .filterKeys { monthYear -> monthYear >= allTime }
                        .sum(),
                tax =
                    taxes
                        .filterKeys { monthYear -> monthYear >= allTime }
                        .sum(),
                expense =
                    expenses
                        .filterKeys { monthYear -> monthYear >= allTime }
                        .sum(),
            )

        return listOf(
            HomeViewState.ExpandableCardGroup(
                name = "Savings this year",
                value = savingsRateThisYear.formatPercent(),
                isValuePositive = savingsRateThisYear >= BigDecimal("0.5"),
                children =
                    listOf(
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "This month",
                            value = savingsRateThisMonth.formatPercent(),
                        ),
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "Last month",
                            value = savingsRateLastMonth.formatPercent(),
                        ),
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "Last 3 years",
                            value = savingsRateLastThreeYears.formatPercent(),
                        ),
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "All time",
                            value = savingsRateAllTime.formatPercent(),
                        ),
                    ),
            ),
        )
    }

    private fun Map<MonthYear, BigDecimal>.sum(): BigDecimal {
        var sum = BigDecimal.ZERO
        forEach { (_, value) ->
            sum += value
        }
        return sum
    }

    private fun getSavingsRate(
        income: BigDecimal,
        tax: BigDecimal,
        expense: BigDecimal,
    ): BigDecimal {
        val afterTaxIncome = income - tax
        val savings = afterTaxIncome - expense
        return savings.div(afterTaxIncome)
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
                    netWorth = total,
                )
            }
    }

    private fun getCurrentBalanceGroups(currentBalances: List<CurrentBalance>) =
        currentBalances
            .groupBy { it.groupName }
            .map { (groupName, currentBalances) ->
                HomeViewState.ExpandableCardGroup(
                    name = groupName,
                    value = currentBalances.sumOf { it.balance }.format(),
                    isValuePositive = getIsValuePositive(groupName),
                    children =
                        currentBalances.map {
                            HomeViewState.ExpandableCardGroup.Child(
                                title = it.name,
                                value = it.balance.format(),
                            )
                        },
                )
            }

    private fun HomeViewState.Period.toMonthYears(): List<MonthYear> {
        val now = MonthYear.now(clock, timeZone)
        val start =
            when (this) {
                is HomeViewState.Period.AllTime -> {
                    MonthYear(Month.JANUARY, 2021)
                }

                is HomeViewState.Period.LastThreeYears -> {
                    now.minus(DateTimePeriod(years = 3))
                }

                is HomeViewState.Period.OneYear -> {
                    now.minus(DateTimePeriod(years = 1))
                }

                is HomeViewState.Period.ThisYear -> {
                    now.copy(month = Month.JANUARY)
                }
            }
        return (start..now).toList()
    }

    private fun getIsValuePositive(name: String): Boolean {
        return when (name) {
            "Travel" -> false
            "Cash" -> true
            "Retirement" -> true
            "Credit Cards" -> false
            "Salary" -> true
            "Taxes" -> false
            else -> false
        }
    }
}
