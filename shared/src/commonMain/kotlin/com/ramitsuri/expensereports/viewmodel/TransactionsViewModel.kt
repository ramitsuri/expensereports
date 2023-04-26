package com.ramitsuri.expensereports.viewmodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.utils.endOfMonth
import com.ramitsuri.expensereports.utils.startOfMonth
import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.repository.TransactionsRepository
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.utils.monthDateYear
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

    private val _state: MutableStateFlow<TransactionsViewState> =
        MutableStateFlow(TransactionsViewState(filter = TransactionsFilter()))
    val state: StateFlow<TransactionsViewState> = _state

    init {
        filterUpdated()
    }

    fun onFilterUpdated(
        startDate: LocalDate?,
        endDate: LocalDate?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?
    ) {
        _state.update {
            it.copy(
                filter = TransactionsFilter(
                    startDate = startDate,
                    endDate = endDate,
                    minAmount = minAmount,
                    maxAmount = maxAmount
                )
            )
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
                        if (filter.minAmount == null) {
                            true
                        } else {
                            transaction.total >= filter.minAmount
                        }
                    }
                    .filter { transaction ->
                        if (filter.maxAmount == null) {
                            true
                        } else {
                            transaction.total <= filter.maxAmount
                        }
                    }
                    .sortedByDescending { transaction ->
                        transaction.date
                    }
                    .groupBy { it.date }
                    .mapKeys { (date, _) ->
                        date.monthDateYear()
                    }

                _state.update {
                    it.copy(
                        loading = false,
                        transactions = filteredTransactions,
                        filter = filter.copy(startDate = startDate, endDate = endDate)
                    )
                }
            }
        }
    }
}

data class TransactionsViewState(
    val loading: Boolean = false,
    val transactions: Map<String, List<Transaction>> = mapOf(),
    val filter: TransactionsFilter
)

data class TransactionsFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null
)