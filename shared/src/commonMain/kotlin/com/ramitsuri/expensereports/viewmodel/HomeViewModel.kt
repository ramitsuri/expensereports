package com.ramitsuri.expensereports.viewmodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.repository.ReportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val repository: ReportsRepository,
    private val prefManager: PrefManager,
    private val clock: Clock
) : ViewModel() {

    private val timeZone = TimeZone.currentSystemDefault()
    private val now = clock.now().toLocalDateTime(timeZone)

    private val _state: MutableStateFlow<HomeViewState> = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state

    init {
        viewModelScope.launch {
            val generalPeriod = getMonths(numberOfMonths = MONTHS_GENERAL)
            val yearsInGeneralPeriod = generalPeriod.map { it.year }.distinct()
            val generalReportTypes = listOf(typeExpenses, typeSavings, typeIncome)
            val generalReports = repository.get(yearsInGeneralPeriod, generalReportTypes)

            // Expenses
            val expenses = mutableListOf<MainAccountBalance>()
            val expenseReports = generalReports.filter { it.type == typeExpenses }
            for (expenseReport in expenseReports) {
                for (date in generalPeriod) {
                    if (expenseReport.year == date.year) {
                        val amount = expenseReport.accountTotal.monthAmounts[date.monthNumber]
                            ?: BigDecimal.ZERO
                        expenses.add(MainAccountBalance(date, amount))
                    }
                }
            }

            // Savings
            val savings = mutableListOf<MainAccountBalance>()
            val savingReports = generalReports.filter { it.type == typeSavings }
            for (savingReport in savingReports) {
                for (date in generalPeriod) {
                    if (savingReport.year == date.year) {
                        val amount = savingReport.accountTotal.monthAmounts[date.monthNumber]
                            ?: BigDecimal.ZERO
                        savings.add(MainAccountBalance(date, amount))
                    }
                }
            }

            // Incomes
            val incomes = mutableListOf<MainAccountBalance>()
            val incomeReports = generalReports.filter { it.type == typeIncome }
            for (incomeReport in incomeReports) {
                for (date in generalPeriod) {
                    if (incomeReport.year == date.year) {
                        // Income amounts are in negative for gains
                        val amount = (incomeReport.accountTotal.monthAmounts[date.monthNumber]
                            ?: BigDecimal.ZERO).multiply(BigDecimal.parseString("-1"))
                        incomes.add(MainAccountBalance(date, amount))
                    }
                }
            }

            // NetWorth
            val netWorthPeriod = getMonths(numberOfMonths = MONTHS_FOR_NET_WORTH)
            val yearsInNetWorthPeriod = netWorthPeriod.map { it.year }.distinct()
            val netWorthReports = repository.get(yearsInNetWorthPeriod, listOf(typeNetWorth))

            val netWorthList = mutableListOf<MainAccountBalance>()
            for (netWorthReport in netWorthReports) {
                for (date in netWorthPeriod) {
                    if (netWorthReport.year == date.year) {
                        val amount = netWorthReport.accountTotal.monthAmounts[date.monthNumber]
                            ?: BigDecimal.ZERO
                        netWorthList.add(MainAccountBalance(date, amount))
                    }
                }
            }

            _state.update { previousState ->
                previousState.copy(
                    expenses = expenses.sortedByDescending { it.date },
                    savings = savings.sortedByDescending { it.date },
                    incomes = incomes.sortedByDescending { it.date },
                    netWorth = netWorthList.sortedBy { it.date }
                )
            }
        }
    }

    private fun getMonths(numberOfMonths: Int): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var previousMonth = clock.now().toLocalDateTime(timeZone).date
        repeat(numberOfMonths) {
            dates.add(previousMonth)
            previousMonth = previousMonth.minus(DatePeriod(months = 1))
        }
        return dates
    }

    companion object {
        private const val TAG = "HomeVM"

        private const val MONTHS_FOR_NET_WORTH = 24
        private const val MONTHS_GENERAL = 3

        private val typeNetWorth = ReportType.NET_WORTH
        private val typeSavings = ReportType.SAVINGS
        private val typeExpenses = ReportType.EXPENSE_AFTER_DEDUCTION
        private val typeIncome = ReportType.INCOME
    }
}

data class HomeViewState(
    val loading: Boolean = false,
    val netWorth: List<MainAccountBalance> = listOf(),
    val savings: List<MainAccountBalance> = listOf(),
    val expenses: List<MainAccountBalance> = listOf(),
    val incomes: List<MainAccountBalance> = listOf()
)

data class MainAccountBalance(val date: LocalDate, val balance: BigDecimal)

data class ChildAccountBalance(val name: String, val date: LocalDate, val balance: BigDecimal)