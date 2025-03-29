package com.ramitsuri.expensereports.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.ramitsuri.expensereports.database.dao.CurrentBalancesDao
import com.ramitsuri.expensereports.database.dao.ReportsDao
import com.ramitsuri.expensereports.database.dao.TransactionsDao
import com.ramitsuri.expensereports.database.model.DbReport
import com.ramitsuri.expensereports.database.model.DbReportAccount
import com.ramitsuri.expensereports.database.model.DbReportAccountTotal
import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.Transaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

@Database(
    entities = [
        DbReport::class,
        DbReportAccount::class,
        DbReportAccountTotal::class,
        Transaction::class,
        CurrentBalance::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionsDao(): TransactionsDao
    abstract fun reportsDao(): ReportsDao
    abstract fun currentBalancesDao(): CurrentBalancesDao

    companion object {

        fun getDb(
            builder: Builder<AppDatabase>,
            dispatcher: CoroutineDispatcher,
            json: Json,
        ): AppDatabase {
            val typeConverter = DatabaseConverters(json)
            return builder
                .setDriver(BundledSQLiteDriver())
                .addTypeConverter(typeConverter)
                .setQueryCoroutineContext(dispatcher)
                .addMigrations(

                )
                .build()
        }
    }
}