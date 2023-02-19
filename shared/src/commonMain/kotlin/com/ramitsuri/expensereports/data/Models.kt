package com.ramitsuri.expensereports.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.network.AccountTotalDto
import com.ramitsuri.expensereports.network.ExpenseReportDto
import kotlinx.datetime.Instant

data class ExpenseReport(
    val name: String,
    val time: Instant,
    val accountTotal: AccountTotal
) {
    constructor(dto: ExpenseReportDto) : this(
        dto.name,
        dto.time,
        AccountTotal(dto.accountTotal)
    )
}

data class AccountTotal(
    val name: String,
    val fullName: String,
    val children: List<AccountTotal>,
    val monthAmounts: Map<Int, BigDecimal>,
    val total: BigDecimal = BigDecimal.ZERO
) {
    constructor(dto: AccountTotalDto) : this(
        dto.name,
        dto.fullName,
        dto.children.map { AccountTotal(it) },
        dto.balances.associateBy(
            keySelector = { it.month },
            valueTransform = { BigDecimal.parseString(it.amount) })
    )

    companion object {
        fun fromAccountTotalAndChildren(
            accountTotal: AccountTotal,
            children: List<AccountTotal>
        ): AccountTotal {
            var total = BigDecimal.ZERO
            val monthAmounts = if (children.isEmpty()) {
                accountTotal.monthAmounts
            } else {
                val monthAmounts = mutableMapOf<Int, BigDecimal>()
                for (month in (1..12)) {
                    var monthTotal = BigDecimal.ZERO
                    children.forEach { monthTotal += it.monthAmounts[month] ?: BigDecimal.ZERO }
                    monthAmounts[month] = monthTotal
                    total += monthTotal
                }
                monthAmounts
            }
            return AccountTotal(
                name = accountTotal.name,
                fullName = accountTotal.fullName,
                children = children,
                monthAmounts = monthAmounts,
                total = total
            )
        }
    }
}
