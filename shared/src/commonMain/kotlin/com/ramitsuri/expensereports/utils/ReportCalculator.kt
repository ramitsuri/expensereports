package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.isNotIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class ReportCalculator(
    private val initialReport: Report,
    private val defaultDispatcher: CoroutineDispatcher
) {

    suspend fun calculate(
        selectedMonths: List<Int>? = null,
        selectedAccounts: List<String>? = null,
        by: By = By.FULL
    ): ReportView = withContext(defaultDispatcher) {
        val result = withContext(defaultDispatcher) {
            val accounts = mutableListOf<AccountTotal>()
            val totalsAccountMonthAmounts = mutableMapOf<Int, BigDecimal>()
            // Filter the month-amounts for selected months and accounts only
            for (account in initialReport.accountTotal.children) {
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
            ReportView.Full(
                accountTotals = accounts.sortedBy { it.name },
                total = totalAccount,
                generatedAt = initialReport.generatedAt
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
        return initialReport.accountTotal.children.map { it.name }.sortedBy { it }
    }

    fun getMonths(): List<Int> {
        return initialReport.accountTotal.monthAmounts.map { (month, _) -> month }
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

sealed class ReportView {
    data class Full(
        val accountTotals: List<AccountTotal>,
        val total: AccountTotal,
        val generatedAt: Instant
    ) : ReportView()

    data class ByMonth(
        val monthTotals: Map<Int, BigDecimal>,
        val total: BigDecimal,
        val generatedAt: Instant
    ) : ReportView()

    data class ByAccount(
        val accountTotals: Map<String, BigDecimal>,
        val total: BigDecimal,
        val generatedAt: Instant
    ) : ReportView()
}

fun ReportView.Full.toByMonth(): ReportView.ByMonth {
    val totalsAccount = this.total
    return ReportView.ByMonth(
        monthTotals = totalsAccount.monthAmounts,
        total = totalsAccount.total,
        generatedAt = this.generatedAt
    )
}

fun ReportView.Full.toByAccount(): ReportView.ByAccount {
    val totalsAccount = this.total
    val otherAccounts = this.accountTotals
        .associate { Pair(it.name, it.total) }
    return ReportView.ByAccount(
        accountTotals = otherAccounts,
        total = totalsAccount.total,
        generatedAt = this.generatedAt
    )
}