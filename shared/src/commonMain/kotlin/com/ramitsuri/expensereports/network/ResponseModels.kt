package com.ramitsuri.expensereports.network

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class ConfigDto(
    @SerialName("ignored_expense_accounts")
    val ignoredExpenseAccounts: List<String>,

    @SerialName("main_asset_accounts")
    val mainAssetAccounts: List<String>,

    @SerialName("main_liability_accounts")
    val mainLiabilityAccounts: List<String>,

    @SerialName("main_income_accounts")
    val mainIncomeAccounts: List<String>,

    @SerialName("annual_budget")
    val annualBudget: String,

    @SerialName("annual_savings_target")
    val annualSavingsTarget: String
)

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
    val splits: List<@Contextual SplitDto>
)

@Serializable
data class SplitDto(
    @SerialName("amount")
    val amount: String,

    @SerialName("account")
    val account: String,
)