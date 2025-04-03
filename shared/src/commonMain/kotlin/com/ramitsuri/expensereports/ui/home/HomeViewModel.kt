package com.ramitsuri.expensereports.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.Report
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.repository.MainRepository
import com.ramitsuri.expensereports.usecase.ExpensesUseCase
import com.ramitsuri.expensereports.usecase.IncomeUseCase
import com.ramitsuri.expensereports.usecase.SavingsRateUseCase
import com.ramitsuri.expensereports.utils.combine
import com.ramitsuri.expensereports.utils.format
import com.ramitsuri.expensereports.utils.formatPercent
import com.ramitsuri.expensereports.utils.getOrThrow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import java.math.BigDecimal

class HomeViewModel(
    private val mainRepository: MainRepository,
    private val savingsRateUseCase: SavingsRateUseCase,
    private val expensesUseCase: ExpensesUseCase,
    private val incomeUseCase: IncomeUseCase,
    private val isDesktop: Boolean,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {
    private val selectedNetWorthPeriod: MutableStateFlow<Period> =
        MutableStateFlow(Period.AllTime)
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
            val now = MonthYear.now(clock, timeZone)
            combine(
                mainRepository.getCurrentBalances(),
                mainRepository.getReport(
                    reportName = ReportNames.NetWorth.name,
                    monthYears = selectedNetWorthPeriod.toMonthYears(now),
                ),
                savingsRateUseCase(
                    listOf(
                        Period.ThisMonth,
                        Period.ThisYear,
                        Period.PreviousMonth,
                        Period.AllTime,
                        Period.LastThreeYears,
                    ),
                ),
                expensesUseCase(listOf(Period.ThisMonth, Period.ThisYear, Period.PreviousMonth)),
                incomeUseCase(listOf(Period.ThisMonth, Period.ThisYear, Period.PreviousMonth)),
                isRefreshing,
            ) {
                    currentBalances,
                    netWorthReport,
                    savingsRates,
                    expenses,
                    incomes,
                    isRefreshing,
                ->
                HomeViewState(
                    expandableCardGroups =
                        getSavingsRates(savingsRates)
                            .plus(getExpenses(expenses))
                            .plus(getIncomes(incomes))
                            .plus(getCurrentBalanceGroups(currentBalances)),
                    netWorths = getNetWorths(netWorthReport),
                    selectedNetWorthPeriod = selectedNetWorthPeriod,
                    periods =
                        listOf(
                            Period.ThisYear,
                            Period.OneYear,
                            Period.LastThreeYears,
                            Period.AllTime,
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

    fun onNetWorthPeriodSelected(period: Period) {
        selectedNetWorthPeriod.value = period
    }

    fun onRefresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            mainRepository.refresh(forced = true)
            isRefreshing.value = false
        }
    }

    private fun getExpenses(expenses: Map<Period, BigDecimal>): List<HomeViewState.ExpandableCardGroup> {
        if (expenses.isEmpty()) {
            return listOf()
        }
        return listOf(
            HomeViewState.ExpandableCardGroup(
                name = "Expenses MTD",
                value = expenses.getOrThrow(Period.ThisMonth).format(),
                isValuePositive = false,
                children =
                    listOf(
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "YTD",
                            value = expenses.getOrThrow(Period.ThisYear).format(),
                        ),
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "Last month",
                            value = expenses.getOrThrow(Period.PreviousMonth).format(),
                        ),
                    ),
            ),
        )
    }

    private fun getIncomes(incomes: Map<Period, BigDecimal>): List<HomeViewState.ExpandableCardGroup> {
        if (incomes.isEmpty()) {
            return listOf()
        }
        return listOf(
            HomeViewState.ExpandableCardGroup(
                name = "Salary MTD",
                value = incomes.getOrThrow(Period.ThisMonth).format(),
                isValuePositive = true,
                children =
                    listOf(
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "Last month",
                            value = incomes.getOrThrow(Period.PreviousMonth).format(),
                        ),
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "YTD",
                            value = incomes.getOrThrow(Period.ThisYear).format(),
                        ),
                    ),
            ),
        )
    }

    private fun getSavingsRates(
        savingsRates: Map<
            Period,
            SavingsRateUseCase.SavingsRate,
            >,
    ): List<HomeViewState.ExpandableCardGroup> {
        if (savingsRates.isEmpty()) {
            return listOf()
        }
        val savingsRateThisYear = savingsRates.getValue(Period.ThisYear).savingsRate
        val savingsRateThisMonth = savingsRates.getValue(Period.ThisMonth).savingsRate
        val savingsRateLastMonth = savingsRates.getValue(Period.PreviousMonth).savingsRate
        val savingsRateLastThreeYears = savingsRates.getValue(Period.LastThreeYears).savingsRate
        val savingsRateAllTime = savingsRates.getValue(Period.AllTime).savingsRate

        return listOf(
            HomeViewState.ExpandableCardGroup(
                name = "Savings YTD",
                value = savingsRateThisYear.formatPercent(),
                isValuePositive = savingsRateThisYear >= BigDecimal("0.5"),
                children =
                    listOf(
                        HomeViewState.ExpandableCardGroup.Child(
                            title = "MTD",
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

    private fun getIsValuePositive(name: String): Boolean {
        return when (name) {
            "Travel" -> false
            "Cash" -> true
            "Retirement" -> true
            "Credit Cards" -> false
            "Taxes" -> false
            else -> false
        }
    }
}
