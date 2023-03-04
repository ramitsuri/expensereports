package com.ramitsuri.expensereports.viewmodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.data.AccountTotalWithTotal
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.isIn
import com.ramitsuri.expensereports.repository.ConfigRepository
import com.ramitsuri.expensereports.repository.ReportsRepository
import com.ramitsuri.expensereports.utils.by
import com.ramitsuri.expensereports.utils.inverse
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
    private val configRepository: ConfigRepository,
    private val clock: Clock
) : ViewModel() {

    private val timeZone = TimeZone.currentSystemDefault()

    private val _state: MutableStateFlow<HomeViewState> = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state

    init {
        viewModelScope.launch {
            val date = clock.now().toLocalDateTime(timeZone).date
            val generalReportTypes =
                listOf(typeExpenses, typeSavings, typeIncome, typeAssets, typeLiabilities)
            val generalReports = repository.get(listOf(date.year), generalReportTypes)

            // Expenses
            var expenseBalance = Balance()
            val expenseReport = generalReports.firstOrNull { it.type == typeExpenses }
            if (expenseReport != null) {
                val monthAmount = expenseReport.accountTotal.monthAmounts[date.monthNumber]
                    ?: BigDecimal.ZERO
                val accountTotal = expenseReport.accountTotal as? AccountTotalWithTotal
                val annualAmount = accountTotal?.total ?: BigDecimal.ZERO
                val annualMax = configRepository.getAnnualBudget()
                expenseBalance = expenseBalance.copy(
                    date = date,
                    month = monthAmount,
                    annual = annualAmount,
                    monthMax = annualMax.by("12"),
                    annualMax = annualMax
                )
            }

            // Savings
            var savingsBalance = Balance()
            val savingReport = generalReports.firstOrNull { it.type == typeSavings }
            if (savingReport != null) {
                val monthAmount = savingReport.accountTotal.monthAmounts[date.monthNumber]
                    ?: BigDecimal.ZERO
                val accountTotal = savingReport.accountTotal as? AccountTotalWithTotal
                val annualAmount = accountTotal?.total ?: BigDecimal.ZERO
                val annualMax = configRepository.getAnnualSavingsTarget()
                savingsBalance = savingsBalance.copy(
                    date = date,
                    month = monthAmount,
                    annual = annualAmount,
                    monthMax = annualMax.by("12"),
                    annualMax = annualMax
                )
            }

            // Incomes
            var monthIncomeBalance = BigDecimal.ZERO
            val incomeReport = generalReports.firstOrNull { it.type == typeIncome }
            if (incomeReport != null) {
                val filteredIncomes = getFilteredAccounts(
                    incomeReport.accountTotal,
                    date,
                    configRepository.getIncomeAccounts()
                ) { accountTotal ->
                    val balance = accountTotal.monthAmounts[date.monthNumber] ?: BigDecimal.ZERO
                    MonthAccountBalance(balance, date)
                }
                var incomeBalance = BigDecimal.ZERO
                for (filteredIncome in filteredIncomes) {
                    incomeBalance += filteredIncome.balance
                }
                monthIncomeBalance = incomeBalance.inverse()
            }

            // Liability accounts
            val liabilityAccountBalances = mutableListOf<AccountBalance>()
            val liabilityReport = generalReports.firstOrNull { it.type == typeLiabilities }
            if (liabilityReport != null) {
                liabilityAccountBalances.addAll(getFilteredAccounts(
                    accountTotal = liabilityReport.accountTotal,
                    date = date,
                    includeAccounts = configRepository.getLiabilityAccounts()
                ) { accountTotal ->
                    val balance = accountTotal.monthAmounts[date.monthNumber] ?: BigDecimal.ZERO
                    AccountBalance(name = accountTotal.name, balance = balance)
                })
            }

            // Asset accounts
            val assetAccountBalances = mutableListOf<AccountBalance>()
            val assetReport = generalReports.firstOrNull { it.type == typeAssets }
            if (assetReport != null) {
                assetAccountBalances.addAll(getFilteredAccounts(
                    accountTotal = assetReport.accountTotal,
                    date = date,
                    includeAccounts = configRepository.getAssetAccounts()
                ) { accountTotal ->
                    val balance = accountTotal.monthAmounts[date.monthNumber] ?: BigDecimal.ZERO
                    AccountBalance(name = accountTotal.name, balance = balance)
                })
            }

            // NetWorth
            val netWorthPeriod = getMonths(numberOfMonths = MONTHS_FOR_NET_WORTH)
            val yearsInNetWorthPeriod = netWorthPeriod.map { it.year }.distinct()
            val netWorthReports = repository.get(yearsInNetWorthPeriod, listOf(typeNetWorth))

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
                    expenses = expenseBalance,
                    savings = savingsBalance,
                    monthSalary = monthIncomeBalance,
                    netWorth = netWorthList.sortedBy { it.date },
                    liabilityAccountBalances = liabilityAccountBalances,
                    assetAccountBalances = assetAccountBalances
                )
            }
        }
    }


    private fun <T> getFilteredAccounts(
        accountTotal: AccountTotal,
        date: LocalDate,
        includeAccounts: List<String>,
        mapper: (AccountTotal) -> T
    ): List<T> {
        val result = mutableListOf<T>()
        if (accountTotal.isIn(includeAccounts, fullName = true)) {
            result.add(mapper(accountTotal))
        }
        for (childAccountTotal in accountTotal.children) {
            result.addAll(getFilteredAccounts(childAccountTotal, date, includeAccounts, mapper))
        }
        return result
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
        private const val MONTHS_GENERAL = 1

        private val typeNetWorth = ReportType.NET_WORTH
        private val typeSavings = ReportType.SAVINGS
        private val typeExpenses = ReportType.EXPENSE_AFTER_DEDUCTION
        private val typeIncome = ReportType.INCOME
        private val typeAssets = ReportType.ASSETS
        private val typeLiabilities = ReportType.LIABILITIES
    }
}

data class HomeViewState(
    val loading: Boolean = false,
    val netWorth: List<MonthAccountBalance> = listOf(),
    val expenses: Balance = Balance(),
    val savings: Balance = Balance(),
    val topExpenses: List<AccountBalance> = listOf(),
    val assetAccountBalances: List<AccountBalance> = listOf(),
    val liabilityAccountBalances: List<AccountBalance> = listOf(),
    val monthSalary: BigDecimal = BigDecimal.ZERO
)

data class MonthAccountBalance(
    val balance: BigDecimal = BigDecimal.ZERO,
    val date: LocalDate = LocalDate.fromEpochDays(0)
)

data class AccountBalance(val name: String, val balance: BigDecimal)

data class Balance(
    val date: LocalDate = LocalDate.fromEpochDays(0),
    val month: BigDecimal = BigDecimal.ZERO,
    val annual: BigDecimal = BigDecimal.ZERO,
    val monthMax: BigDecimal = BigDecimal.ZERO,
    val annualMax: BigDecimal = BigDecimal.ZERO
)

data class ChildAccountBalance(val name: String, val date: LocalDate, val balance: BigDecimal)