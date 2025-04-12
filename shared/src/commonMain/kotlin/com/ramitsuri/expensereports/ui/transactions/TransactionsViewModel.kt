package com.ramitsuri.expensereports.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.repository.MainRepository
import com.ramitsuri.expensereports.utils.nowLocal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class TransactionsViewModel(
    mainRepository: MainRepository,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {
    private val selectedEndDate = MutableStateFlow(clock.nowLocal(timeZone).date)
    private val selectedStartDate =
        selectedEndDate.value.let {
            MutableStateFlow(LocalDate(year = it.year, monthNumber = it.monthNumber, dayOfMonth = 1))
        }
    private val description = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState =
        combine(
            selectedStartDate,
            selectedEndDate,
            description,
        ) { selectedStartDate, selectedEndDate, description ->
            Triple(selectedStartDate, selectedEndDate, description)
        }.flatMapLatest { (selectedStartDate, selectedEndDate, description) ->
            mainRepository.getTransactions(
                description = description,
                start = selectedStartDate,
                end = selectedEndDate,
            ).map { transactions ->
                val txOrDateList =
                    transactions
                        .sortedByDescending { it.date }
                        .groupBy { it.date }
                        .flatMap { (date, transactions) ->
                            listOf(TransactionsViewState.TxOrDate.Date(date)) +
                                transactions.map { TransactionsViewState.TxOrDate.Tx(it) }
                        }
                TransactionsViewState(
                    isLoading = false,
                    txOrDateList = txOrDateList,
                    startDate = selectedStartDate,
                    endDate = selectedEndDate,
                    description = description,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue =
                TransactionsViewState(
                    startDate = selectedStartDate.value,
                    endDate = selectedEndDate.value,
                    description = description.value,
                ),
        )

    fun onFilterApplied(
        description: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ) {
        this.description.value = description
        this.selectedStartDate.value = startDate
        this.selectedEndDate.value = endDate
    }
}
