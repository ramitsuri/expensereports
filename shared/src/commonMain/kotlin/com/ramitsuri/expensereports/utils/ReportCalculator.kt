package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.isIn
import com.ramitsuri.expensereports.ui.Account
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
        val afterFilter = initialReport.accountTotal
            .filterAccounts(selectedAccounts)
            .filterMonths(selectedMonths)
        val result = if (afterFilter == null) {
            ReportView.Full(
                accountTotals = listOf(),
                total = SimpleAccountTotal(initialReport.accountTotal, level = 0),
                generatedAt = initialReport.generatedAt
            )
        } else {
            ReportView.Full(
                accountTotals = afterFilter.flatten().sortedBy { it.fullName },
                total = SimpleAccountTotal(afterFilter, level = 0),
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
                            .filter { (month, _) ->
                                totalAccountMonthsWithNoZeros.monthAmounts.keys.contains(
                                    month
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

    fun getAccounts(): List<Account> {
        return getAccounts(initialReport.accountTotal, level = 0).sortedBy { it.fullName }
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

    private fun AccountTotal.flatten(level: Int = 0): List<SimpleAccountTotal> {
        val result: MutableList<SimpleAccountTotal> = mutableListOf()
        if (level != 0) {
            result.add(SimpleAccountTotal(this, level))
        }
        for (accountTotal in children)
            result.addAll(accountTotal.flatten(level + 1))
        return result
    }

    private fun AccountTotal.filterAccounts(includedAccounts: List<String>?): AccountTotal? {
        if (isIn(includedAccounts, fullName = true)) {
            return this
        }
        val children = children.mapNotNull { child ->
            child.filterAccounts(includedAccounts)
        }
        // The account wasn't included in the included accounts list so, only way it could be
        // made available was for it to have any children.
        if (children.isEmpty()) {
            return null
        }
        return copy(children = children)
    }

    private fun AccountTotal?.filterMonths(includedMonths: List<Int>?): AccountTotal? {
        if (this == null) {
            return null
        }
        val children = children.mapNotNull { child ->
            child.filterMonths(includedMonths)
        }
        val withTotalsPopulated = if (children.isEmpty()) {
            populateTotals(includedMonths)
        } else {
            populateTotals(children, includedMonths)
        }
        return withTotalsPopulated.copy(children = children)
    }

    private fun AccountTotal.populateTotals(includedMonths: List<Int>?): AccountTotal {
        val monthAmounts = monthAmounts.filter { (month, _) ->
            month.isIn(includedMonths)
        }
        var total = BigDecimal.ZERO
        monthAmounts.forEach { (_, amount) ->
            total += amount
        }
        return copy(
            monthAmounts = monthAmounts,
            total = total
        )
    }

    private fun AccountTotal.populateTotals(
        children: List<AccountTotal>,
        includedMonths: List<Int>?
    ): AccountTotal {
        val monthAmounts = mutableMapOf<Int, BigDecimal>()
        for (month in 1..12) {
            if (month.isNotIn(includedMonths)) {
                continue
            }
            var monthTotal = BigDecimal.ZERO
            var noChildHasMonth = true
            for (child in children) {
                val monthAmount = child.monthAmounts[month]
                monthTotal += if (monthAmount == null) {
                    BigDecimal.ZERO
                } else {
                    noChildHasMonth = false
                    monthAmount
                }
            }
            if (noChildHasMonth) {
                continue
            }
            monthAmounts[month] = monthTotal
        }

        var total = BigDecimal.ZERO
        monthAmounts.forEach { (_, amount) ->
            total += amount
        }
        return copy(
            monthAmounts = monthAmounts,
            total = total
        )
    }

    private fun getAccounts(accountTotal: AccountTotal, level: Int = 0): List<Account> {
        val list: MutableList<Account> = mutableListOf()
        list.add(Account(accountTotal, level))
        for (child in accountTotal.children)
            list.addAll(getAccounts(child, level + 1))
        return list
    }

    enum class By {
        FULL,
        MONTH,
        ACCOUNT
    }
}

sealed class ReportView {
    data class Full(
        val accountTotals: List<SimpleAccountTotal>,
        val total: SimpleAccountTotal,
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

data class SimpleAccountTotal(
    val name: String,
    val fullName: String,
    val monthAmounts: Map<Int, BigDecimal>,
    val total: BigDecimal = BigDecimal.ZERO,
    val level: Int
) {
    constructor(accountTotal: AccountTotal, level: Int) : this(
        name = accountTotal.name,
        fullName = accountTotal.fullName,
        monthAmounts = accountTotal.monthAmounts,
        total = accountTotal.total,
        level = level
    )
}