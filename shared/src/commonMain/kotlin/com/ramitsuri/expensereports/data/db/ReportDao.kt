package com.ramitsuri.expensereports.data.db

import com.ramitsuri.expensereports.data.AccountTotalWithTotal
import com.ramitsuri.expensereports.data.AccountTotalWithoutTotal
import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.db.ReportEntity
import com.ramitsuri.expensereports.db.ReportsQueries
import com.ramitsuri.expensereports.utils.transactionWithContext
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

interface ReportDao {
    suspend fun get(year: Int, type: ReportType): Flow<Report?>

    suspend fun get(years: List<Int>, types: List<ReportType>): List<Report>

    suspend fun update(year: Int, type: ReportType, fetchedAt: Instant, report: Report)

    suspend fun insert(year: Int, type: ReportType, report: Report)

    suspend fun deleteAll()
}

class ReportDaoImpl(
    private val dbQueries: ReportsQueries,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : ReportDao {

    override suspend fun get(year: Int, type: ReportType): Flow<Report?> {
        return dbQueries.getReport(year = year.toLong(), type = type)
            .asFlow()
            .mapToOneOrNull()
            .map { reportEntity ->
                mapper(reportEntity)
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun get(years: List<Int>, types: List<ReportType>): List<Report> {
        val result = withContext(ioDispatcher) {
            dbQueries.getReports(years = years.map { it.toLong() }, types = types)
                .executeAsList()
                .mapNotNull { reportEntity ->
                    mapper(reportEntity)
                }
        }
        return result
    }

    override suspend fun update(
        year: Int,
        type: ReportType,
        fetchedAt: Instant,
        report: Report
    ) {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.updateReport(
                year = year.toLong(),
                type = type,
                generatedAt = report.generatedAt,
                fetchedAt = fetchedAt,
                content = when (report.accountTotal) {
                    is AccountTotalWithoutTotal -> {
                        json.encodeToString(
                            AccountTotalWithoutTotal.serializer(),
                            report.accountTotal
                        )
                    }
                    is AccountTotalWithTotal -> {
                        json.encodeToString(AccountTotalWithTotal.serializer(), report.accountTotal)
                    }
                }
            )
        }
    }

    override suspend fun insert(
        year: Int,
        type: ReportType,
        report: Report
    ) {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.insertReport(
                year = year.toLong(),
                type = type,
                name = report.name,
                generatedAt = report.generatedAt,
                fetchedAt = report.fetchedAt,
                content = when (report.accountTotal) {
                    is AccountTotalWithoutTotal -> {
                        json.encodeToString(
                            AccountTotalWithoutTotal.serializer(),
                            report.accountTotal
                        )
                    }
                    is AccountTotalWithTotal -> {
                        json.encodeToString(AccountTotalWithTotal.serializer(), report.accountTotal)
                    }
                }
            )
        }
    }

    override suspend fun deleteAll() {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.deleteReports()
        }
    }

    private fun mapper(reportEntity: ReportEntity?): Report? {
        return if (reportEntity != null) {
            val accountTotal = if (reportEntity.type.hasTotal) {
                json.decodeFromString<AccountTotalWithTotal>(reportEntity.content)
            } else {
                json.decodeFromString<AccountTotalWithoutTotal>(reportEntity.content)
            }
            Report(
                name = reportEntity.name,
                generatedAt = reportEntity.generatedAt,
                fetchedAt = reportEntity.fetchedAt,
                accountTotal = accountTotal,
                type = reportEntity.type,
                year = reportEntity.year.toInt()
            )
        } else {
            null
        }
    }
}