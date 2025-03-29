package com.ramitsuri.expensereports.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ramitsuri.expensereports.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
internal interface TransactionsDao {
    @Query("SELECT * FROM db_transaction WHERE date BETWEEN :start AND :end AND date")
    fun get(start: LocalDate, end: LocalDate): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: List<Transaction>)
}