package com.ramitsuri.expensereports.viewmodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ramitsuri.expensereports.data.AccountBalance
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.repository.MiscellaneousRepository
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.utils.bd
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
    private val miscellaneousRepository: MiscellaneousRepository,
    private val prefManager: PrefManager,
    private val clock: Clock,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val timeZone = TimeZone.currentSystemDefault()

    private val _state: MutableStateFlow<HomeViewState> = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state

    init {
        // Miscellaneous
        updateExpenseSavingsShare()

        viewModelScope.launch(dispatcherProvider.io) {
            // NetWorth
            val netWorthPeriod = getMonths(numberOfMonths = MONTHS_FOR_NET_WORTH)
            val yearsInNetWorthPeriod = netWorthPeriod.map { it.year }.distinct()
            repository.get(yearsInNetWorthPeriod, listOf(typeNetWorth))
                .collect { netWorthReports ->
                    val netWorthList = mutableListOf<MonthAccountBalance>()
                    for (netWorthReport in netWorthReports) {
                        for (periodDate in netWorthPeriod) {
                            if (netWorthReport.year == periodDate.year) {
                                val amount =
                                    netWorthReport.accountTotal.monthAmounts[periodDate.monthNumber]
                                        ?: BigDecimal.ZERO
                                netWorthList.add(MonthAccountBalance(amount, periodDate))
                            }
                        }
                    }

                    _state.update { previousState ->
                        previousState.copy(
                            netWorth = netWorthList.sortedBy { it.date }
                        )
                    }
                }
        }

        viewModelScope.launch(dispatcherProvider.io) {
            // Transaction Groups
            val transactionGroup = prefManager.getTransactionGroup()
            if (transactionGroup != null) {
                val transactionGroupAccountBalance =
                    AccountBalance(name = transactionGroup.name, balance = transactionGroup.total)
                _state.update { previousState ->
                    previousState.copy(transactionGroups = listOf(transactionGroupAccountBalance))
                }
            }
        }
    }

    fun onIncludeDeductionsChanged() {
        _state.update { previousState ->
            val previousExpenseSavingsShare = previousState.expenseSavingsShare
            val newExpenseSavingsShare = previousState.expenseSavingsShare?.copy(
                includeDeductions = previousExpenseSavingsShare?.includeDeductions?.not() ?: false
            )
            previousState.copy(expenseSavingsShare = newExpenseSavingsShare)
        }
        updateExpenseSavingsShare()
    }

    private fun updateExpenseSavingsShare() {
        viewModelScope.launch(dispatcherProvider.io) {
            // Miscellaneous
            miscellaneousRepository.get().collect { miscellaneous ->
                if (miscellaneous == null) {
                    return@collect
                }
                _state.update { previousState ->
                    val income = miscellaneous.incomeTotal
                    val expenses = miscellaneous.expensesTotal
                    val expensesAfterDeductions = miscellaneous.expensesAfterDeductionTotal
                    val deductions = expenses.subtract(expensesAfterDeductions)
                    val incomeAfterDeductions = income.subtract(deductions)
                    val savings = incomeAfterDeductions.subtract(expensesAfterDeductions)
                    val previousExpenseSavingsShare = previousState.expenseSavingsShare
                    val newExpenseSavingsShare =
                        if (previousExpenseSavingsShare?.includeDeductions == true) {
                            val expensesShare = expensesAfterDeductions.shareIn(income)
                            val savingsShare = savings.shareIn(income)
                            val deductionsShare = 10000 - savingsShare - expensesShare
                            ExpenseSavingsShare(
                                expensesSharePercent = expensesShare.div(100f),
                                savingsSharePercent = savingsShare.div(100f),
                                deductionsSharePercent = deductionsShare.div(100f),
                                includeDeductions = true
                            )
                        } else {
                            val expensesShare =
                                expensesAfterDeductions.shareIn(incomeAfterDeductions)
                            val savingsShare = savings.shareIn(incomeAfterDeductions)
                            val deductionsShare = 0f
                            ExpenseSavingsShare(
                                expensesSharePercent = expensesShare.div(100f),
                                savingsSharePercent = savingsShare.div(100f),
                                deductionsSharePercent = deductionsShare,
                                includeDeductions = false
                            )
                        }
                    previousState.copy(
                        expenseSavingsShare = newExpenseSavingsShare,
                        accountBalances = miscellaneous.accountBalances
                    )
                }
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

    private fun BigDecimal.shareIn(other: BigDecimal): Int {
        if (other.compareTo(BigDecimal.ZERO) == 0) {
            return 0
        }
        return this.multiply("10000".bd()).divide(other, DecimalMode.US_CURRENCY)
            .floatValue(exactRequired = false).toInt()
    }

    companion object {
        private const val TAG = "HomeVM"
        private const val MONTHS_FOR_NET_WORTH = 24
        private val typeNetWorth = ReportType.NET_WORTH
    }
}

data class HomeViewState(
    val loading: Boolean = false,
    val netWorth: List<MonthAccountBalance> = listOf(),
    val expenseSavingsShare: ExpenseSavingsShare? = null,
    val accountBalances: List<AccountBalance> = listOf(),
    val transactionGroups: List<AccountBalance> = listOf()
)

data class ExpenseSavingsShare(
    val expensesSharePercent: Float,
    val savingsSharePercent: Float,
    val deductionsSharePercent: Float,
    val includeDeductions: Boolean,
)

data class MonthAccountBalance(
    val balance: BigDecimal = BigDecimal.ZERO,
    val date: LocalDate = LocalDate.fromEpochDays(0)
)