package com.ramitsuri.expensereports.model

import com.ramitsuri.expensereports.network.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Report(

    @SerialName("name")
    val name: String,

    @SerialName("with_cumulative_balance")
    val withCumulativeBalance: Boolean,

    // Rows
    @SerialName("accounts")
    val accounts: List<Account>,
) {
    @Serializable
    data class Account(
        @SerialName("name")
        val name: String,

        @SerialName("order")
        val order: Int,

        // Columns
        @SerialName("month_totals")
        val monthTotals: Map<MonthYear, @Serializable(BigDecimalSerializer::class) BigDecimal>,
    )
}
