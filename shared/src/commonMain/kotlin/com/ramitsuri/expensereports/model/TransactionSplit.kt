package com.ramitsuri.expensereports.model

import com.ramitsuri.expensereports.network.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class TransactionSplit(
    @SerialName("account_name")
    val accountName: String,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("amount")
    val amount: BigDecimal,
)
