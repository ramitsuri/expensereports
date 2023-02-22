package com.ramitsuri.expensereports.network

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportWithTotalDto(
    @SerialName("name")
    val name: String,

    @Serializable(with = InstantSerializer::class)
    @SerialName("time")
    val time: Instant,

    @SerialName("account_total")
    val accountTotal: AccountTotalWithTotalDto
)

@Serializable
data class AccountTotalWithTotalDto(
    @SerialName("name")
    val name: String,

    @SerialName("fullname")
    val fullName: String,

    @SerialName("children")
    val children: List<AccountTotalWithTotalDto>,

    @SerialName("balances")
    val balances: List<BalanceDto>,

    @SerialName("total")
    val total: String
)

@Serializable
data class ReportWithoutTotalDto(
    @SerialName("name")
    val name: String,

    @Serializable(with = InstantSerializer::class)
    @SerialName("time")
    val time: Instant,

    @SerialName("account_total")
    val accountTotal: AccountTotalWithoutTotalDto
)

@Serializable
data class AccountTotalWithoutTotalDto(
    @SerialName("name")
    val name: String,

    @SerialName("fullname")
    val fullName: String,

    @SerialName("children")
    val children: List<AccountTotalWithoutTotalDto>,

    @SerialName("balances")
    val balances: List<BalanceDto>
)

@Serializable
data class BalanceDto(
    @SerialName("month")
    val month: Int,

    @SerialName("amount")
    val amount: String
)
