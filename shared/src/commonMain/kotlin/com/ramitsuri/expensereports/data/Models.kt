package com.ramitsuri.expensereports.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.network.AccountTotalWithTotalDto
import com.ramitsuri.expensereports.network.AccountTotalWithoutTotalDto
import com.ramitsuri.expensereports.network.BigDecimalSerializer
import com.ramitsuri.expensereports.network.IntBigDecimalMapSerializer
import com.ramitsuri.expensereports.network.ReportWithTotalDto
import com.ramitsuri.expensereports.network.ReportWithoutTotalDto
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
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

@Serializable
data class AccountTotalWithTotal(
    @SerialName("name")
    val name: String,

    @SerialName("fullName")
    val fullName: String,

    @SerialName("children")
    val children: List<AccountTotalWithTotal>,

    @Serializable(with = IntBigDecimalMapSerializer::class)
    @SerialName("monthAmounts")
    val monthAmounts: Map<Int, @Contextual BigDecimal>,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("total")
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

@Serializable
data class AccountTotalWithoutTotal(
    @SerialName("name")
    val name: String,

    @SerialName("fullName")
    val fullName: String,

    @SerialName("children")
    val children: List<AccountTotalWithoutTotal>,

    @Serializable(with = IntBigDecimalMapSerializer::class)
    @SerialName("monthAmounts")
    val monthAmounts: Map<Int, @Contextual BigDecimal>
) {
    constructor(dto: AccountTotalWithoutTotalDto) : this(
        dto.name,
        dto.fullName,
        dto.children.map { AccountTotalWithoutTotal(it) },
        dto.balances.associateBy(
            keySelector = { it.month },
            valueTransform = { BigDecimal.parseString(it.amount) })
    )

    companion object {
        fun fromAccountTotalAndChildren(
            accountTotal: AccountTotalWithoutTotal,
            children: List<AccountTotalWithoutTotal>
        ): AccountTotalWithoutTotal {
            val monthAmounts = if (children.isEmpty()) {
                accountTotal.monthAmounts
            } else {
                val monthAmounts = mutableMapOf<Int, BigDecimal>()
                for (month in (1..12)) {
                    var monthTotal = BigDecimal.ZERO
                    children.forEach { monthTotal += it.monthAmounts[month] ?: BigDecimal.ZERO }
                    monthAmounts[month] = monthTotal
                }
                monthAmounts
            }
            return AccountTotalWithoutTotal(
                name = accountTotal.name,
                fullName = accountTotal.fullName,
                children = children,
                monthAmounts = monthAmounts
            )
        }
    }
}
