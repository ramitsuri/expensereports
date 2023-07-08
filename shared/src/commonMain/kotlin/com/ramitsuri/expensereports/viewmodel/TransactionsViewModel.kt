package com.ramitsuri.expensereports.viewmodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.Split
import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.repository.TransactionsRepository
import com.ramitsuri.expensereports.ui.Account
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.utils.bd
import com.ramitsuri.expensereports.utils.endOfMonth
import com.ramitsuri.expensereports.utils.startOfMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        MutableStateFlow(TransactionsViewState())
    val state: StateFlow<TransactionsViewState> = _state

    init {
        applyDefaultFilter()
    }

    fun onFilterUpdated(
        startDate: LocalDate,
        endDate: LocalDate,
        minAmount: BigDecimal,
        maxAmount: BigDecimal,
        fromAccounts: List<Account>,
        toAccounts: List<Account>
    ) {
        _state.update {
            it.copy(
                filter = TransactionsFilter(
                    startDate = startDate,
                    endDate = endDate,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                    fromAccounts = fromAccounts,
                    toAccounts = toAccounts
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

        val startDate = filter?.startDate ?: now.toLocalDateTime(timeZone).date.startOfMonth()
        val endDate = filter?.endDate ?: now.toLocalDateTime(timeZone).date.endOfMonth()

        viewModelScope.launch(dispatchers.io) {
            repository.getTransactions(startDate, endDate).collect { transactions ->
                val filteredTransactions = transactions
                    .asSequence()
                    .filter { transaction ->
                        if (filter == null) {
                            true
                        } else {
                            val fromAccounts = filter.fromAccounts.map { it.removeRoot() }
                            val toAccounts = filter.toAccounts.map { it.removeRoot() }

                            transaction.total >= filter.minAmount &&
                                    transaction.total <= filter.maxAmount &&
                                    transaction.splits.any { split ->
                                        split.isDebit() &&
                                                fromAccounts.any { filterAccount ->
                                                    filterAccount.selected &&
                                                            filterAccount.fullName == split.account
                                                }
                                    } &&
                                    transaction.splits.any { split ->
                                        split.isCredit() &&
                                                toAccounts.any { filterAccount ->
                                                    filterAccount.selected &&
                                                            filterAccount.fullName == split.account
                                                }
                                    }
                        }
                    }
                    .sortedByDescending { transaction ->
                        transaction.date
                    }
                    .toList()

                _state.update {
                    it.copy(
                        loading = false,
                        transactions = filteredTransactions,
                    )
                }
            }
        }
    }

    private suspend fun getAccountsForFilter(
        transactions: List<Transaction>,
        condition: Split.() -> Boolean
    ): List<Account> {
        fun transactionsToAccounts(condition: Split.() -> Boolean): MutableList<Account> {
            // Get account full names from transaction splits
            return transactions
                .asSequence()
                .map { transaction ->
                    transaction.splits
                }
                .flatten()
                .filter { split ->
                    split.condition()
                }
                .map { split ->
                    split.account
                }
                .distinct()
                // Split each account full name to its constituent parent accounts
                // E:A1:A11:A113 -> [E, E:A1, E:A1:A13, E:A1:A11:A113]
                .map { accountFullName ->
                    val accountNameSplits = accountFullName.split(":")
                    (1..accountNameSplits.size).map { level ->
                        accountNameSplits.take(level).joinToString(":")
                    }
                }
                .flatten()
                .distinct()
                // Convert to Account
                .map { name ->
                    Account.fromFullName(name, selected = true)
                }
                .sortedBy { it.fullName + ":" }
                .toMutableList()
        }

        return withContext(dispatchers.default) {
            listOf(Account.rootAccount(selected = true))
                .plus(transactionsToAccounts(condition))
        }
    }

    private fun applyDefaultFilter() {
        viewModelScope.launch {
            val now = clock.now()
            val startDate = now.toLocalDateTime(timeZone).date.startOfMonth()
            val endDate = now.toLocalDateTime(timeZone).date.endOfMonth()
            val allTransactions = repository.getAllTransactions()
            val fromAccounts = getAccountsForFilter(allTransactions, Split::isDebit)
            val toAccounts = getAccountsForFilter(allTransactions, Split::isCredit)

            val filter = TransactionsFilter(
                startDate = startDate,
                endDate = endDate,
                minAmount = MIN_AMOUNT.bd(),
                maxAmount = MAX_AMOUNT.bd(),
                fromAccounts = fromAccounts,
                toAccounts = toAccounts
            )
            _state.update { previousState ->
                previousState.copy(filter = filter)
            }
            filterUpdated()
        }
    }

    companion object {
        private const val MIN_AMOUNT = "0"
        private const val MAX_AMOUNT = "15000"
    }
}

data class TransactionsViewState(
    val loading: Boolean = true,
    val transactions: List<Transaction> = listOf(),
    val filter: TransactionsFilter? = null
)

data class TransactionsFilter(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val minAmount: BigDecimal,
    val maxAmount: BigDecimal,
    val fromAccounts: List<Account>,
    val toAccounts: List<Account>,
)