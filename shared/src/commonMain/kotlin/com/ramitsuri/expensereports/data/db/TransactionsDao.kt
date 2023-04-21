package com.ramitsuri.expensereports.data.db

import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.db.ReportsQueries
import com.ramitsuri.expensereports.db.TransactionEntity
import com.ramitsuri.expensereports.utils.transactionWithContext
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull

interface TransactionsDao {
    fun get(years: List<Int>, months: List<Int>): Flow<List<Transaction>>

    suspend fun insert(year: Int, month: Int, transactions: List<Transaction>)
}

class TransactionsDaoImpl(
    private val dbQueries: ReportsQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : TransactionsDao {
    override fun get(years: List<Int>, months: List<Int>): Flow<List<Transaction>> {
        return dbQueries.getTransactions(
            years = years.map { it.toLong() },
            months = months.map { it.toLong() })
            .asFlow()
            .mapToList()
            .mapNotNull { transactionEntities ->
                transactionEntities.mapNotNull { transactionEntity ->
                    mapper(transactionEntity)
                }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun insert(year: Int, month: Int, transactions: List<Transaction>) {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.deleteTransactions(year = year.toLong(), month = month.toLong())
            for (transaction in transactions) {
                dbQueries.insertTransaction(
                    year = year.toLong(),
                    month = month.toLong(),
                    date = transaction.date,
                    description = transaction.description,
                    total = transaction.total,
                    splits = transaction.splits
                )
            }
        }
    }

    private fun mapper(transactionEntity: TransactionEntity?): Transaction? {
        return if (transactionEntity != null) {
            Transaction(
                date = transactionEntity.date,
                total = transactionEntity.total,
                description = transactionEntity.description,
                splits = transactionEntity.splits
            )
        } else {
            null
        }
    }
}