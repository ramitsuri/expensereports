package com.ramitsuri.expensereports.data.db

import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.db.ReportEntity
import com.ramitsuri.expensereports.db.ReportsDb
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

class Database(
    driver: SqlDriver,
    dispatcherProvider: DispatcherProvider,
    json: Json
) {
    private val database = ReportsDb(
        driver = driver,
        ReportEntityAdapter = ReportEntity.Adapter(
            generatedAtAdapter = instantConverter,
            typeAdapter = reportTypeConverter
        )
    )
    private val dbQueries = database.reportsQueries

    val reportDao: ReportDao = ReportDaoImpl(dbQueries, dispatcherProvider.io, json)
}

private val instantConverter = object : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant {
        return Instant.parse(databaseValue)
    }

    override fun encode(value: Instant): String {
        return value.toString()
    }
}

private val reportTypeConverter = object : ColumnAdapter<ReportType, Long> {
    override fun decode(databaseValue: Long): ReportType {
        return ReportType.fromKey(databaseValue)
    }

    override fun encode(value: ReportType): Long {
        return value.key
    }
}