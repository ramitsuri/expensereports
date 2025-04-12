package com.ramitsuri.expensereports.ui.transactions

import com.ramitsuri.expensereports.model.Transaction
import kotlinx.datetime.LocalDate

data class TransactionsViewState(
    val isLoading: Boolean = true,
    val txOrDateList: List<TxOrDate> = listOf(),
    val startDate: LocalDate,
    val endDate: LocalDate,
    val description: String,
) {
    sealed interface TxOrDate {
        data class Tx(val tx: Transaction) : TxOrDate

        data class Date(val date: LocalDate) : TxOrDate
    }
}
