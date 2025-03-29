package com.ramitsuri.expensereports.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ramitsuri.expensereports.model.CurrentBalance
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CurrentBalancesDao {
     @Query("SELECT * FROM db_current_balance")
     fun get(): Flow<List<CurrentBalance>>

     @Insert(onConflict = OnConflictStrategy.REPLACE)
     suspend fun insert(currentBalances: List<CurrentBalance>)
}