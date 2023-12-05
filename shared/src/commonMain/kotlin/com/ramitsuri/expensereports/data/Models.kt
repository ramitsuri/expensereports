package com.ramitsuri.expensereports.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.network.AccountBalanceDto
import com.ramitsuri.expensereports.network.AccountTotalDto
import com.ramitsuri.expensereports.network.BigDecimalSerializer
import com.ramitsuri.expensereports.network.IntBigDecimalMapSerializer
import com.ramitsuri.expensereports.network.MiscellaneousDto
import com.ramitsuri.expensereports.network.ReportDto
import com.ramitsuri.expensereports.network.SplitDto
import com.ramitsuri.expensereports.network.TransactionDto
import com.ramitsuri.expensereports.network.TransactionGroupDto
import com.ramitsuri.expensereports.utils.Constants
import com.ramitsuri.expensereports.utils.bd
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val name: String,
    val generatedAt: Instant,
    val fetchedAt: Instant,
    val accountTotal: AccountTotal,
    val type: ReportType,
    val year: Int
) {
    constructor(
        dto: ReportDto,
        fetchedAt: Instant,
        type: ReportType,
        year: Int
    ) : this(
        name = dto.name,
        generatedAt = dto.time,
        fetchedAt = fetchedAt,
        accountTotal = AccountTotal(dto.accountTotal),
        type = type,
        year = year
    )
}

fun Report.isStale(now: Instant): Boolean {
    return now.minus(fetchedAt).inWholeMilliseconds >= Constants.REFRESH_THRESHOLD_MS
}

@Serializable
data class AccountTotal(
    @SerialName("name")
    val name: String,

    @SerialName("fullName")
    val fullName: String,

    @SerialName("children")
    val children: List<AccountTotal>,

    @Serializable(with = IntBigDecimalMapSerializer::class)
    @SerialName("monthAmounts")
    val monthAmounts: Map<Int, @Contextual BigDecimal>,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("total")
    val total: BigDecimal = BigDecimal.ZERO
) {
    constructor(dto: AccountTotalDto) : this(
        dto.name,
        dto.fullName,
        dto.children.map { AccountTotal(it) },
        dto.balances.associateBy(
            keySelector = { it.month },
            valueTransform = { BigDecimal.parseString(it.amount) }),
        total = BigDecimal.parseString(dto.total)
    )
}


fun AccountTotal.isIn(
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

fun AccountTotal.isNotIn(
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


@Serializable
data class Miscellaneous(
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("incomeTotal")
    val incomeTotal: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("expensesTotal")
    val expensesTotal: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("expensesAfterDeductionTotal")
    val expensesAfterDeductionTotal: BigDecimal,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("savingsTotal")
    val savingsTotal: BigDecimal,

    @SerialName("accountBalances")
    val accountBalances: List<AccountBalance>
) {
    constructor(dto: MiscellaneousDto) : this(
        incomeTotal = dto.incomeTotal.bd(),
        expensesTotal = dto.expensesTotal.bd(),
        expensesAfterDeductionTotal = dto.expensesAfterDeductionTotal.bd(),
        savingsTotal = dto.savingsTotal.bd(),
        accountBalances = dto.accountBalances.map { AccountBalance(it) }
    )
}

data class Transaction(
    val date: LocalDate,
    val total: BigDecimal,
    val description: String,
    val splits: List<Split>,
    val num: String
) {
    constructor(dto: TransactionDto) : this(
        date = dto.date,
        total = BigDecimal.parseString(dto.total),
        description = dto.description,
        splits = dto.splits.map { Split(it) },
        num = dto.num
    )
}

@Serializable
data class Split(
    @SerialName("account")
    val account: String,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amount")
    val amount: BigDecimal
) {
    constructor(dto: SplitDto) : this(
        account = dto.account,
        amount = BigDecimal.parseString(dto.amount)
    )

    fun isDebit() = amount < BigDecimal.ZERO

    fun isCredit() = !isDebit()
}

@Serializable
data class TransactionGroup(
    @SerialName("name")
    val name: String,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("total")
    val total: BigDecimal
) {
    constructor(dto: TransactionGroupDto) : this(
        name = dto.name,
        total = BigDecimal.parseString(dto.total)
    )
}

@Serializable
data class AccountBalance(
    @SerialName("name")
    val name: String,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("balance")
    val balance: BigDecimal
) {
    constructor(dto: AccountBalanceDto) : this(
        name = dto.name,
        balance = dto.balance.bd()
    )
}