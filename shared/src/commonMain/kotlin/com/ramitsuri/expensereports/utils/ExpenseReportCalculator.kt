package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.data.ExpenseReport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ExpenseReportCalculator(
    initialReport: ExpenseReport,
    ignoredExpenseAccounts: List<String>,
    private val defaultDispatcher: CoroutineDispatcher
) {
    private lateinit var filteredReport: ExpenseReport

    init {
        val accountTotal = removeIgnoredAccounts(initialReport.accountTotal, ignoredExpenseAccounts)
        if (accountTotal != null) {
            filteredReport = ExpenseReport(initialReport.name, initialReport.time, accountTotal)
        }
    }

    suspend fun calculate(
        selectedMonths: List<Int>? = null,
        selectedAccounts: List<String>? = null,
        by: By = By.FULL
    ): ExpenseReportView = withContext(defaultDispatcher) {
        if (reportNotInitialized()) {
            return@withContext ExpenseReportView.InvalidReport
        }
        val result = withContext(defaultDispatcher) {
            val accounts = mutableListOf<AccountTotal>()
            val totalsAccountMonthAmounts = mutableMapOf<Int, BigDecimal>()
            // Filter the month-amounts for selected months and accounts only
            for (account in filteredReport.accountTotal.children) {
                if (account.isNotIn(selectedAccounts)) {
                    continue
                }
                val resultAccountMonthAmounts = mutableMapOf<Int, BigDecimal>()
                var resultAccountTotal = BigDecimal.ZERO
                for ((month, amount) in account.monthAmounts) {
                    if (month.isNotIn(selectedMonths)) {
                        continue
                    }
                    resultAccountMonthAmounts[month] = amount
                    resultAccountTotal += amount

                    // Initialize totals row with zero amounts for selected months
                    totalsAccountMonthAmounts[month] = BigDecimal.ZERO
                }
                val resultAccount = AccountTotal(
                    name = account.name,
                    fullName = account.fullName,
                    children = listOf(),
                    monthAmounts = resultAccountMonthAmounts,
                    total = resultAccountTotal
                )
                accounts.add(resultAccount)
            }

            // Calculate amounts for the totals row
            var totalAccountTotal = BigDecimal.ZERO
            for ((totalMonth, _) in totalsAccountMonthAmounts) {
                var amount = BigDecimal.ZERO
                for (resultAccount in accounts) {
                    amount += resultAccount.monthAmounts[totalMonth] ?: BigDecimal.ZERO
                }
                totalAccountTotal += amount
                totalsAccountMonthAmounts[totalMonth] = amount
            }
            val totalAccount = AccountTotal(
                name = TOTAL,
                fullName = TOTAL,
                children = listOf(),
                monthAmounts = totalsAccountMonthAmounts,
                total = totalAccountTotal
            )
            ExpenseReportView.Full(
                accountTotals = accounts.sortedBy { it.name },
                total = totalAccount,
                generatedAt = filteredReport.time.toString()
            )
        }
        val hideZeroTotals = selectedAccounts == null && selectedMonths == null // Filter out zeros
        // only when no selected months and accounts are sent because that's when the report is
        // requested for the first time. After that we want to send back totals for whatever
        // selection even if zero
        val resultWithNoZeros = if (hideZeroTotals) {
            val totalAccountMonthsWithNoZeros =
                result.total.copy(monthAmounts = result.total.monthAmounts.filter {
                    it.value.compare(BigDecimal.ZERO) != 0
                })
            val accountTotalsWithNoZeros = result.accountTotals
                .filter { accountTotal ->
                    accountTotal.total.compare(BigDecimal.ZERO) != 0
                }
                .map { accountTotal ->
                    accountTotal.copy(
                        monthAmounts = accountTotal.monthAmounts
                            .filter {
                                totalAccountMonthsWithNoZeros.monthAmounts.keys.contains(
                                    it.key
                                )
                            }
                    )
                }
            result.copy(
                accountTotals = accountTotalsWithNoZeros,
                total = totalAccountMonthsWithNoZeros
            )
        } else {
            result
        }
        return@withContext when (by) {
            By.FULL -> resultWithNoZeros
            By.MONTH -> resultWithNoZeros.toByMonth()
            By.ACCOUNT -> resultWithNoZeros.toByAccount()
        }
    }

    fun getAccounts(): List<String> {
        if (reportNotInitialized()) {
            return emptyList()
        }

        return filteredReport.accountTotal.children.map { it.name }.sortedBy { it }
    }

    fun getMonths(): List<Int> {
        if (reportNotInitialized()) {
            return emptyList()
        }

        return filteredReport.accountTotal.monthAmounts.map { (month, _) -> month }
    }

    private fun removeIgnoredAccounts(
        rootAccountTotal: AccountTotal,
        ignoredExpenseAccounts: List<String>
    ): AccountTotal? {
        val children =
            rootAccountTotal.children.mapNotNull {
                removeIgnoredAccounts(it, ignoredExpenseAccounts)
            }
        if (children.isEmpty() && rootAccountTotal.children.isNotEmpty()) {
            return null
        }
        val accountTotal = AccountTotal.fromAccountTotalAndChildren(rootAccountTotal, children)
        if (rootAccountTotal.isIn(ignoredExpenseAccounts, fullName = true)) {
            return null
        }
        return accountTotal
    }

    private fun reportNotInitialized(): Boolean {
        return !::filteredReport.isInitialized
    }

    private fun AccountTotal.isIn(
        selectedAccountNames: List<String>?,
        fullName: Boolean = false
    ): Boolean {
        if (selectedAccountNames == null) {
            return true
        }
        return if (fullName) {
            selectedAccountNames.contains(this.fullName)
        } else {
            selectedAccountNames.contains(this.name)
        }
    }

    private fun AccountTotal.isNotIn(
        selectedAccountNames: List<String>?,
        fullName: Boolean = false
    ): Boolean {
        return !isIn(selectedAccountNames, fullName)
    }

    private fun Int.isIn(selectedMonths: List<Int>?): Boolean {
        if (selectedMonths == null) {
            return true
        }
        return selectedMonths.contains(this)
    }

    private fun Int.isNotIn(selectedMonths: List<Int>?): Boolean {
        return !isIn(selectedMonths)
    }

    enum class By {
        FULL,
        MONTH,
        ACCOUNT
    }

    companion object {
        const val TOTAL = "Total"
    }
}

sealed class ExpenseReportView {
    data class Full(
        val accountTotals: List<AccountTotal>,
        val total: AccountTotal,
        val generatedAt: String
    ) : ExpenseReportView()

    data class ByMonth(
        val monthTotals: Map<Int, BigDecimal>,
        val total: BigDecimal,
        val generatedAt: String
    ) : ExpenseReportView()

    data class ByAccount(
        val accountTotals: Map<String, BigDecimal>,
        val total: BigDecimal,
        val generatedAt: String
    ) : ExpenseReportView()

    object InvalidReport : ExpenseReportView()
}

fun ExpenseReportView.Full.toByMonth(): ExpenseReportView.ByMonth {
    val totalsAccount = this.total
    return ExpenseReportView.ByMonth(
        monthTotals = totalsAccount.monthAmounts,
        total = totalsAccount.total,
        generatedAt = this.generatedAt
    )
}

fun ExpenseReportView.Full.toByAccount(): ExpenseReportView.ByAccount {
    val totalsAccount = this.total
    val otherAccounts = this.accountTotals
        .associate { Pair(it.name, it.total) }
    return ExpenseReportView.ByAccount(
        accountTotals = otherAccounts,
        total = totalsAccount.total,
        generatedAt = this.generatedAt
    )
}