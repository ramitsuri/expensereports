package com.ramitsuri.expensereports.ui.transactions

import com.ramitsuri.expensereports.model.Transaction
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

data class TransactionsViewState(
    val isLoading: Boolean = true,
    val txOrDateList: List<TxOrDate> = listOf(),
    val startDate: LocalDate,
    val endDate: LocalDate,
    val description: String,
    val filterFromAccount: String? = null,
    val filterToAccount: String? = null,
    val filterMinAmount: BigDecimal? = null,
    val filterMaxAmount: BigDecimal? = null,
    val fromAccountSuggestions: List<String> = emptyList(),
    val toAccountSuggestions: List<String> = emptyList(),
) {
    sealed interface TxOrDate {
        data class Tx(val tx: Transaction) : TxOrDate

        data class Date(val date: LocalDate) : TxOrDate
    }
}
