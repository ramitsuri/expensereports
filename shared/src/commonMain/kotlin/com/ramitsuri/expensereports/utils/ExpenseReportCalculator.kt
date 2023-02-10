package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.data.ExpenseReport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ExpenseReportCalculator(
    private val initialReport: ExpenseReport,
    private val defaultDispatcher: CoroutineDispatcher
) {
    suspend fun calculate(
        selectedMonths: List<Int>? = null,
        selectedAccounts: List<String>? = null,
        by: By = By.FULL
    ): ExpenseReportView = withContext(defaultDispatcher) {
        val result = withContext(defaultDispatcher) {
            val accounts = mutableListOf<AccountTotal>()
            val totalsAccountMonthAmounts = mutableMapOf<Int, BigDecimal>()
            // Filter the month-amounts for selected months and accounts only
            for (account in initialReport.accountTotals) {
                if (account.notSelected(selectedAccounts)) {
                    continue
                }
                val resultAccountMonthAmounts = mutableMapOf<Int, BigDecimal>()
                var resultAccountTotal = BigDecimal.ZERO
                for ((month, amount) in account.monthAmounts) {
                    if (month.notSelected(selectedMonths)) {
                        continue
                    }
                    resultAccountMonthAmounts[month] = amount
                    resultAccountTotal += amount

                    // Initialize totals row with zero amounts for selected months
                    totalsAccountMonthAmounts[month] = BigDecimal.ZERO
                }
                val resultAccount = AccountTotal(
                    name = account.name,
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
                children = listOf(),
                monthAmounts = totalsAccountMonthAmounts,
                total = totalAccountTotal
            )
            val sortedMonths = totalAccount.monthAmounts.keys.sorted()
            ExpenseReportView.Full(
                accountTotals = accounts.sortedBy { it.name },
                total = totalAccount,
                sortedMonths = sortedMonths
            )
        }
        return@withContext when (by) {
            By.FULL -> result
            By.MONTH -> result.toByMonth()
            By.ACCOUNT -> result.toByAccount()
        }
    }

    private fun AccountTotal.selected(selectedAccountNames: List<String>?): Boolean {
        if (selectedAccountNames == null) {
            return true
        }
        return selectedAccountNames.contains(this.name)
    }

    private fun AccountTotal.notSelected(selectedAccountNames: List<String>?): Boolean {
        return !selected(selectedAccountNames)
    }

    private fun Int.selected(selectedMonths: List<Int>?): Boolean {
        if (selectedMonths == null) {
            return true
        }
        return selectedMonths.contains(this)
    }

    private fun Int.notSelected(selectedMonths: List<Int>?): Boolean {
        return !selected(selectedMonths)
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
        val sortedMonths: List<Int>
    ) : ExpenseReportView()

    data class ByMonth(
        val monthTotals: Map<Int, BigDecimal>,
        val total: BigDecimal,
        val sortedMonths: List<Int>
    ) : ExpenseReportView()

    data class ByAccount(
        val accountTotals: Map<String, BigDecimal>,
        val total: BigDecimal,
        val sortedAccounts: List<String>
    ) : ExpenseReportView()
}

fun ExpenseReportView.Full.toByMonth(): ExpenseReportView.ByMonth {
    val totalsAccount = this.total
    return ExpenseReportView.ByMonth(
        monthTotals = totalsAccount.monthAmounts,
        total = totalsAccount.total,
        sortedMonths = this.sortedMonths
    )
}

fun ExpenseReportView.Full.toByAccount(): ExpenseReportView.ByAccount {
    val totalsAccount = this.total
    val otherAccounts = this.accountTotals
        .associate { Pair(it.name, it.total) }
    return ExpenseReportView.ByAccount(
        accountTotals = otherAccounts,
        total = totalsAccount.total,
        sortedAccounts = this.accountTotals.map { it.name }.sorted()
    )
}