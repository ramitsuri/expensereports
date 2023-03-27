package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.data.db.TransactionsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus

class TransactionsRepository(
    private val dao: TransactionsDao
) {

    fun getTransactions(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>> {
        val dateRange = startDate.rangeTo(endDate)
        val dates = getDates(startDate, endDate)
        val years = dates.map { it.year }.distinct()
        val months = dates.map { it.month.number }.distinct()

        return dao.get(years, months).map { list ->
            list.mapNotNull { transaction ->
                if (dateRange.contains(transaction.date)) {
                    transaction
                } else {
                    null
                }
            }
        }
    }

    private fun getDates(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dates = mutableSetOf<LocalDate>()
        var date = startDate
        do {
            dates.add(date)
            date = date.plus(DatePeriod(months = 1))
        } while (date <= endDate)
        dates.add(endDate)
        return dates.toList()
    }

    companion object {
        private const val TAG = "TransactionsRepo"
    }
}