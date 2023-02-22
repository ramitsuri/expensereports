package com.ramitsuri.expensereports.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.network.AccountTotalWithTotalDto
import com.ramitsuri.expensereports.network.AccountTotalWithoutTotalDto
import com.ramitsuri.expensereports.network.ReportWithTotalDto
import com.ramitsuri.expensereports.network.ReportWithoutTotalDto
import kotlinx.datetime.Instant

data class ReportWithTotal(
    val name: String,
    val time: Instant,
    val accountTotal: AccountTotalWithTotal
) {
    constructor(dto: ReportWithTotalDto) : this(
        dto.name,
        dto.time,
        AccountTotalWithTotal(dto.accountTotal)
    )
}

data class AccountTotalWithTotal(
    val name: String,
    val fullName: String,
    val children: List<AccountTotalWithTotal>,
    val monthAmounts: Map<Int, BigDecimal>,
    val total: BigDecimal = BigDecimal.ZERO
) {
    constructor(dto: AccountTotalWithTotalDto) : this(
        dto.name,
        dto.fullName,
        dto.children.map { AccountTotalWithTotal(it) },
        dto.balances.associateBy(
            keySelector = { it.month },
            valueTransform = { BigDecimal.parseString(it.amount) })
    )

    companion object {
        fun fromAccountTotalAndChildren(
            accountTotal: AccountTotalWithTotal,
            children: List<AccountTotalWithTotal>
        ): AccountTotalWithTotal {
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
            return AccountTotalWithTotal(
                name = accountTotal.name,
                fullName = accountTotal.fullName,
                children = children,
                monthAmounts = monthAmounts,
                total = total
            )
        }
    }
}

data class ReportWithoutTotal(
    val name: String,
    val time: Instant,
    val accountTotal: AccountTotalWithoutTotal
) {
    constructor(dto: ReportWithoutTotalDto) : this(
        dto.name,
        dto.time,
        AccountTotalWithoutTotal(dto.accountTotal)
    )
}

data class AccountTotalWithoutTotal(
    val name: String,
    val fullName: String,
    val children: List<AccountTotalWithoutTotal>,
    val monthAmounts: Map<Int, BigDecimal>,
    val total: BigDecimal = BigDecimal.ZERO
) {
    constructor(dto: AccountTotalWithoutTotalDto) : this(
        dto.name,
        dto.fullName,
        dto.children.map { AccountTotalWithoutTotal(it) },
        dto.balances.associateBy(
            keySelector = { it.month },
            valueTransform = { BigDecimal.parseString(it.amount) })
    )
}
