package com.ramitsuri.expensereports.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.network.AccountTotalDto
import com.ramitsuri.expensereports.network.ExpenseReportDto

data class ExpenseReport(
    val name: String,
    val accountTotals: List<AccountTotal>
) {
    constructor(dto: ExpenseReportDto) : this(
        dto.name,
        dto.accountTotals.map { AccountTotal(it) }
    )
}

data class AccountTotal(
    val name: String,
    val children: List<AccountTotal>,
    val monthAmounts: Map<Int, BigDecimal>,
    val total: BigDecimal = BigDecimal.ZERO
) {
    constructor(dto: AccountTotalDto) : this(
        dto.name,
        dto.children.map { AccountTotal(it) },
        dto.balances.associateBy(
            keySelector = { it.month },
            valueTransform = { BigDecimal.parseString(it.amount) })
    )
}
