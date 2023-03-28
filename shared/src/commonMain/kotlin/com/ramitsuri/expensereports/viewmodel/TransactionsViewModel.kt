package com.ramitsuri.expensereports.viewmodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.utils.endOfMonth
import com.ramitsuri.expensereports.utils.startOfMonth
import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.repository.TransactionsRepository
import com.ramitsuri.expensereports.utils.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


class TransactionsViewModel(
    private val repository: TransactionsRepository,
    private val dispatchers: DispatcherProvider,
    private val clock: Clock,
    private val timeZone: TimeZone
) : ViewModel() {
    private val _state: MutableStateFlow<TransactionsViewState> = MutableStateFlow(
        TransactionsViewState()
    )
    val state: StateFlow<TransactionsViewState> = _state

    init {
        filterUpdated()
    }

    fun onStartDateSelected(date: LocalDate?) {
        _state.update {
            it.copy(filter = it.filter.copy(startDate = date))
        }
        filterUpdated()
    }

    fun onEndDateSelected(date: LocalDate?) {
        _state.update {
            it.copy(filter = it.filter.copy(endDate = date))
        }
        filterUpdated()
    }

    fun onMinAmountUpdated(amount: BigDecimal?) {
        _state.update {
            it.copy(filter = it.filter.copy(minAmount = amount))
        }
        filterUpdated()
    }

    fun onMaxAmountUpdated(amount: BigDecimal?) {
        _state.update {
            it.copy(filter = it.filter.copy(maxAmount = amount))
        }
        filterUpdated()
    }

    private fun filterUpdated() {
        _state.update {
            it.copy(loading = true)
        }
        val filter = _state.value.filter
        val now = clock.now()

        val startDate = filter.startDate ?: now.toLocalDateTime(timeZone).date.startOfMonth()
        val endDate = filter.endDate ?: now.toLocalDateTime(timeZone).date.endOfMonth()

        viewModelScope.launch(dispatchers.io) {
            repository.getTransactions(startDate, endDate).collect { transactions ->
                val filteredTransactions = transactions
                    .asSequence()
                    .filter { transaction ->
                        if (filter.fromAccounts == null) {
                            true
                        } else {
                            transaction.fromAccounts.any { filter.fromAccounts.contains(it) }
                        }
                    }
                    .filter { transaction ->
                        if (filter.toAccounts == null) {
                            true
                        } else {
                            transaction.toAccounts.any { filter.toAccounts.contains(it) }
                        }
                    }
                    .filter { transaction ->
                        if (filter.minAmount == null) {
                            true
                        } else {
                            transaction.amount >= filter.minAmount
                        }
                    }
                    .filter { transaction ->
                        if (filter.maxAmount == null) {
                            true
                        } else {
                            transaction.amount <= filter.maxAmount
                        }
                    }
                    .sortedByDescending { transaction ->
                        transaction.date
                    }
                    .toList()

                _state.update {
                    it.copy(
                        loading = false,
                        transactions = filteredTransactions
                    )
                }
            }
        }
    }
}

data class TransactionsViewState(
    val loading: Boolean = false,
    val transactions: List<Transaction> = listOf(),
    val filter: TransactionsFilter = TransactionsFilter()
)

data class TransactionsFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val fromAccounts: List<String>? = null,
    val toAccounts: List<String>? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null
)