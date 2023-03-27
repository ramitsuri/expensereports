package com.ramitsuri.expensereports.data.db

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.db.ReportEntity
import com.ramitsuri.expensereports.db.ReportsDb
import com.ramitsuri.expensereports.db.TransactionEntity
import com.ramitsuri.expensereports.utils.DispatcherProvider
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
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
            fetchedAtAdapter = instantConverter,
            typeAdapter = reportTypeConverter
        ),
        TransactionEntity.Adapter(
            dateAdapter = localDateConverter,
            amountAdapter = bigDecimalConverter,
            fromAccountsAdapter = stringListConverter,
            toAccountsAdapter = stringListConverter
        )
    )
    private val dbQueries = database.reportsQueries

    val reportDao: ReportDao = ReportDaoImpl(dbQueries, dispatcherProvider.io, json)
    val transactionsDao: TransactionsDao = TransactionsDaoImpl(dbQueries, dispatcherProvider.io)
}

private val instantConverter = object : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant {
        return Instant.parse(databaseValue)
    }

    override fun encode(value: Instant): String {
        return value.toString()
    }
}

private val localDateConverter = object : ColumnAdapter<LocalDate, String> {
    override fun decode(databaseValue: String): LocalDate {
        return LocalDate.parse(databaseValue)
    }

    override fun encode(value: LocalDate): String {
        return value.toString()
    }
}

private val bigDecimalConverter = object : ColumnAdapter<BigDecimal, String> {
    override fun decode(databaseValue: String): BigDecimal {
        return BigDecimal.parseString(databaseValue)
    }

    override fun encode(value: BigDecimal): String {
        return value.toString()
    }
}

private val stringListConverter = object : ColumnAdapter<List<String>, String> {
    private val separator: String = ";;;"
    override fun decode(databaseValue: String): List<String> {
        return if (databaseValue.isEmpty()) {
            listOf()
        } else {
            databaseValue.split(separator)
        }
    }

    override fun encode(value: List<String>): String {
        return value.joinToString(separator)
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