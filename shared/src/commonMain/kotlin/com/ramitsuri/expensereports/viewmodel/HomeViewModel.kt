package com.ramitsuri.expensereports.viewmodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.Error
import com.ramitsuri.expensereports.data.ReportType
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
    private val clock: Clock
) : ViewModel() {

    private val timeZone = TimeZone.currentSystemDefault()
    private val now = clock.now().toLocalDateTime(timeZone)

    private val _state: MutableStateFlow<HomeViewState> = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state

    init {
        val currentYear = now.year
        val years = listOf(currentYear, currentYear - 1)
        val reportTypes = listOf(reportTypeExpenses, reportTypeSavings, reportTypeNetWorth)
        viewModelScope.launch {
            val lastThreeMonths = getLastThreeMonths()
            val reports = repository.get(years, reportTypes)

            // Expenses
            val lastThreeExpenses = mutableListOf<Expense>()
            val expenseReports = reports.filter { it.type == reportTypeExpenses }
            for (expenseReport in expenseReports) {
                for (date in lastThreeMonths) {
                    if (expenseReport.year == date.year) {
                        val amount = expenseReport.accountTotal.monthAmounts[date.monthNumber]
                            ?: BigDecimal.ZERO
                        lastThreeExpenses.add(Expense(date, amount))
                    }
                }
            }

            // Savings
            val lastThreeSavings = mutableListOf<Saving>()
            val savingReports = reports.filter { it.type == reportTypeSavings }
            for (savingReport in savingReports) {
                for (date in lastThreeMonths) {
                    if (savingReport.year == date.year) {
                        val amount = savingReport.accountTotal.monthAmounts[date.monthNumber]
                            ?: BigDecimal.ZERO
                        lastThreeSavings.add(Saving(date, amount))
                    }
                }
            }

            // NetWorth
            val oneYearNetWorth = mutableListOf<NetWorth>()
            val netWorthReports = reports.filter { it.type == reportTypeNetWorth }
            for (netWorthReport in netWorthReports) {
                for (date in getLastOneYear()) {
                    if (netWorthReport.year == date.year) {
                        val amount = netWorthReport.accountTotal.monthAmounts[date.monthNumber]
                            ?: BigDecimal.ZERO
                        oneYearNetWorth.add(NetWorth(date, amount))
                    }
                }
            }

            _state.update { previousState ->
                previousState.copy(
                    expenses = lastThreeExpenses.sortedByDescending { it.date },
                    savings = lastThreeSavings.sortedByDescending { it.date },
                    netWorth = oneYearNetWorth.sortedBy { it.date }
                )
            }
        }
    }

    private fun getLastThreeMonths(): List<LocalDate> {
        val firstMonth = clock.now().toLocalDateTime(timeZone).date
        val secondMonth = firstMonth.minus(DatePeriod(months = 1))
        val thirdMonth = secondMonth.minus(DatePeriod(months = 1))
        return listOf(firstMonth, secondMonth, thirdMonth)
    }

    private fun getLastOneYear(): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var previousMonth = clock.now().toLocalDateTime(timeZone).date
        repeat(12) {
            dates.add(previousMonth)
            previousMonth = previousMonth.minus(DatePeriod(months = 1))
        }
        return dates
    }

    companion object {
        private const val TAG = "HomeVM"

        private val reportTypeNetWorth = ReportType.NET_WORTH
        private val reportTypeSavings = ReportType.SAVINGS
        private val reportTypeExpenses = ReportType.EXPENSE_AFTER_DEDUCTION
    }
}

data class HomeViewState(
    val loading: Boolean = false,
    val netWorth: List<NetWorth> = listOf(),
    val netWorthError: Error? = null,
    val savings: List<Saving> = listOf(),
    val savingsError: Error? = null,
    val expenses: List<Expense> = listOf(),
    val expensesError: Error? = null
)

data class Saving(val date: LocalDate, val value: BigDecimal)

data class Expense(val date: LocalDate, val value: BigDecimal)

data class NetWorth(val date: LocalDate, val value: BigDecimal)