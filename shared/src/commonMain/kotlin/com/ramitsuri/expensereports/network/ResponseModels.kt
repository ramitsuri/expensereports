package com.ramitsuri.expensereports.network

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// region Reports

@Serializable
data class ReportDto(
    @SerialName("name")
    val name: String,

    @Serializable(with = InstantSerializer::class)
    @SerialName("time")
    val time: Instant,

    @SerialName("account_total")
    val accountTotal: AccountTotalDto
)

@Serializable
data class AccountTotalDto(
    @SerialName("name")
    val name: String,

    @SerialName("fullname")
    val fullName: String,

    @SerialName("children")
    val children: List<AccountTotalDto>,

    @SerialName("balances")
    val balances: List<BalanceDto>,

    @SerialName("total")
    val total: String
)

@Serializable
data class BalanceDto(
    @SerialName("month")
    val month: Int,

    @SerialName("amount")
    val amount: String
)

//endregion


//region Miscellaneous

@Serializable
data class MiscellaneousDataDto(
    @Serializable(with = InstantSerializer::class)
    @SerialName("time")
    val time: Instant,

    @SerialName("miscellaneous")
    val miscellaneous: MiscellaneousDto
)

@Serializable
data class MiscellaneousDto(
    @SerialName("income_total")
    val incomeTotal: String,

    @SerialName("expense_total")
    val expensesTotal: String,

    @SerialName("expense_after_deduction_total")
    val expensesAfterDeductionTotal: String,

    @SerialName("savings_total")
    val savingsTotal: String,

    @SerialName("account_balances")
    val accountBalances: List<AccountBalanceDto>
)

@Serializable
data class AccountBalanceDto(
    @SerialName("name")
    val name: String,

    @SerialName("balance")
    val balance: String

)

//endregion

//region Transactions

@Serializable
data class TransactionsDto(
    @Serializable(with = InstantSerializer::class)
    @SerialName("time")
    val time: Instant,

    @SerialName("transactions")
    val transactions: List<TransactionDto>
) {
}

@Serializable
data class TransactionDto(
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("date")
    val date: LocalDate,


    @SerialName("total")
    val total: String,


    @SerialName("description")
    val description: String,


    @SerialName("splits")
    val splits: List<@Contextual SplitDto>,

    @SerialName("num")
    val num: String
)

@Serializable
data class SplitDto(
    @SerialName("amount")
    val amount: String,

    @SerialName("account")
    val account: String,
)

//endregion

//region TransactionGroups

@Serializable
data class TransactionGroupsDto(
    @Serializable(with = InstantSerializer::class)
    @SerialName("time")
    val time: Instant,

    @SerialName("transaction_groups")
    val transactionGroups: List<TransactionGroupDto>
)

@Serializable
data class TransactionGroupDto(
    @SerialName("name")
    val name: String,

    @SerialName("total")
    val total: String
)

//endregion