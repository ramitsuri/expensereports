package com.ramitsuri.expensereports.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.model.Transaction
import com.ramitsuri.expensereports.repository.MainRepository
import com.ramitsuri.expensereports.utils.combine
import com.ramitsuri.expensereports.utils.nowLocal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.math.BigDecimal

class TransactionsViewModel(
    mainRepository: MainRepository,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {
    private val selectedDescription = MutableStateFlow("")
    private val selectedEndDate = MutableStateFlow(clock.nowLocal(timeZone).date)
    private val selectStartDate =
        selectedEndDate.value.let {
            MutableStateFlow(
                LocalDate(
                    year = it.year,
                    monthNumber = it.monthNumber,
                    dayOfMonth = 1,
                ),
            )
        }

    private val selectedFromAccount = MutableStateFlow<String?>(null)
    private val selectedToAccount = MutableStateFlow<String?>(null)
    private val selectedMinAmount = MutableStateFlow<BigDecimal?>(null)
    private val selectedMaxAmount = MutableStateFlow<BigDecimal?>(null)

    private val allAccountNames = MutableStateFlow<List<String>>(emptyList())

    private data class ParamsForRepository(
        val desc: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val from: String?,
        val to: String?,
        val min: BigDecimal?,
        val max: BigDecimal?,
        val allAccountsList: List<String>,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState =
        combine(
            selectedDescription,
            selectStartDate,
            selectedEndDate,
            selectedFromAccount,
            selectedToAccount,
            selectedMinAmount,
            selectedMaxAmount,
            allAccountNames,
        ) { desc, startDate, endDate, from, to, min, max, allAccounts ->
            ParamsForRepository(desc, startDate, endDate, from, to, min, max, allAccounts)
        }.flatMapLatest { params ->
            val fromSuggestions =
                if (params.from.isNullOrEmpty()) {
                    emptyList()
                } else {
                    params.allAccountsList.filter {
                        it.contains(params.from, ignoreCase = true)
                    }
                }
            val toSuggestions =
                if (params.to.isNullOrEmpty()) {
                    emptyList()
                } else {
                    params.allAccountsList.filter {
                        it.contains(params.to, ignoreCase = true)
                    }
                }
            mainRepository.getTransactions(
                description = params.desc.ifEmpty { null },
                start = params.startDate,
                end = params.endDate,
                fromAccount = params.from,
                toAccount = params.to,
                minAmount = params.min,
                maxAmount = params.max,
            ).map { transactions ->
                TransactionsViewState(
                    isLoading = false,
                    description = params.desc,
                    startDate = params.startDate,
                    endDate = params.endDate,
                    filterFromAccount = params.from,
                    filterToAccount = params.to,
                    filterMinAmount = params.min,
                    filterMaxAmount = params.max,
                    txOrDateList = mapTransactionsToTxOrDate(transactions),
                    fromAccountSuggestions = fromSuggestions,
                    toAccountSuggestions = toSuggestions,
                )
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TransactionsViewState(
                startDate = selectStartDate.value,
                endDate = selectedEndDate.value,
                description = selectedDescription.value,
            ),
        )

    init {
        viewModelScope.launch {
            mainRepository.getAllAccountNames().collect { names ->
                allAccountNames.value = names
            }
        }
    }

    fun onFilterApplied(
        description: String,
        startDate: LocalDate,
        endDate: LocalDate,
        fromAccount: String?,
        toAccount: String?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
    ) {
        selectedDescription.update { description }
        selectStartDate.update { startDate }
        selectedEndDate.update { endDate }
        selectedFromAccount.update { fromAccount }
        selectedToAccount.update { toAccount }
        selectedMinAmount.update { minAmount }
        selectedMaxAmount.update { maxAmount }
    }

    fun onFromAccountTextUpdated(text: String) {
        selectedFromAccount.update { text }
    }

    fun onToAccountTextUpdated(text: String) {
        selectedToAccount.update { text }
    }

    private fun mapTransactionsToTxOrDate(transactions: List<Transaction>): List<TransactionsViewState.TxOrDate> {
        val list = mutableListOf<TransactionsViewState.TxOrDate>()
        transactions
            .sortedByDescending { it.date }
            .groupBy { it.date }
            .forEach { (date, transactionsOnDate) ->
                list.add(TransactionsViewState.TxOrDate.Date(date))
                // transactionsOnDate are already sorted by date due to the initial sort
                transactionsOnDate.forEach { transaction ->
                    list.add(TransactionsViewState.TxOrDate.Tx(transaction))
                }
            }
        return list
    }
}
