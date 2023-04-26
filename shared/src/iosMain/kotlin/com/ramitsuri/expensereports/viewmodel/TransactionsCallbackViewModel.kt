package com.ramitsuri.expensereports.viewmodel

import com.ramitsuri.expensereports.repository.TransactionsRepository
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.ramitsuri.expensereports.utils.bd
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class TransactionsCallbackViewModel(
    repository: TransactionsRepository,
    dispatchers: DispatcherProvider,
    clock: Clock,
    timeZone: TimeZone
) : CallbackViewModel() {
    override val viewModel = TransactionsViewModel(
        repository = repository,
        dispatchers = dispatchers,
        clock = clock,
        timeZone = timeZone
    )

    val state = viewModel.state.asCallbacks()

    fun onFilterUpdated(
        startDate: LocalDateComponents?,
        endDate: LocalDateComponents?,
        minAmount: String?,
        maxAmount: String?
    ) {
        viewModel.onFilterUpdated(
            startDate = startDate?.toLocalDate(),
            endDate = endDate?.toLocalDate(),
            minAmount = minAmount?.bd(),
            maxAmount = maxAmount?.bd()
        )
    }
}

data class LocalDateComponents(val year: Int, val month: Int, val day: Int) {
    fun toLocalDate(): LocalDate {
        return LocalDate(year = year, monthNumber = month, dayOfMonth = day)
    }
}