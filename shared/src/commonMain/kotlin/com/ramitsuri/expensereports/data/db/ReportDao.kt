package com.ramitsuri.expensereports.data.db

import com.ramitsuri.expensereports.data.AccountTotalWithTotal
import com.ramitsuri.expensereports.data.AccountTotalWithoutTotal
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.ReportWithTotal
import com.ramitsuri.expensereports.data.ReportWithoutTotal
import com.ramitsuri.expensereports.db.ReportsQueries
import com.ramitsuri.expensereports.utils.transactionWithContext
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface ReportDao {
    suspend fun getWithTotal(year: Int, type: ReportType): Flow<ReportWithTotal?>

    suspend fun getWithoutTotal(year: Int, type: ReportType): Flow<ReportWithoutTotal?>

    suspend fun update(year: Int, type: ReportType, report: ReportWithTotal)

    suspend fun update(year: Int, type: ReportType, report: ReportWithoutTotal)

    suspend fun insert(year: Int, type: ReportType, report: ReportWithTotal)

    suspend fun insert(year: Int, type: ReportType, report: ReportWithoutTotal)

    suspend fun deleteAll()
}

class ReportDaoImpl(
    private val dbQueries: ReportsQueries,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json
) : ReportDao {

    override suspend fun getWithTotal(year: Int, type: ReportType): Flow<ReportWithTotal?> {
        return dbQueries.getReport(year = year.toLong(), type = type)
            .asFlow()
            .mapToOneOrNull()
            .map { reportEntity ->
                if (reportEntity != null) {
                    val accountTotal =
                        json.decodeFromString<AccountTotalWithTotal>(reportEntity.content)
                    ReportWithTotal(reportEntity.name, reportEntity.generatedAt, accountTotal)
                } else {
                    null
                }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getWithoutTotal(year: Int, type: ReportType): Flow<ReportWithoutTotal?> {
        return dbQueries.getReport(year = year.toLong(), type = type)
            .asFlow()
            .mapToOneOrNull()
            .map { reportEntity ->
                if (reportEntity != null) {
                    val accountTotal =
                        json.decodeFromString<AccountTotalWithoutTotal>(reportEntity.content)
                    ReportWithoutTotal(reportEntity.name, reportEntity.generatedAt, accountTotal)
                } else {
                    null
                }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun update(year: Int, type: ReportType, report: ReportWithTotal) {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.updateReport(
                year = year.toLong(),
                type = type,
                generatedAt = report.time,
                content = json.encodeToString(report.accountTotal)
            )
        }
    }

    override suspend fun update(year: Int, type: ReportType, report: ReportWithoutTotal) {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.updateReport(
                year = year.toLong(),
                type = type,
                generatedAt = report.time,
                content = json.encodeToString(report.accountTotal)
            )
        }
    }

    override suspend fun insert(year: Int, type: ReportType, report: ReportWithTotal) {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.insertReport(
                year = year.toLong(),
                type = type,
                name = report.name,
                generatedAt = report.time,
                content = json.encodeToString(report.accountTotal)
            )
        }
    }

    override suspend fun insert(year: Int, type: ReportType, report: ReportWithoutTotal) {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.insertReport(
                year = year.toLong(),
                type = type,
                name = report.name,
                generatedAt = report.time,
                content = json.encodeToString(report.accountTotal)
            )
        }
    }

    override suspend fun deleteAll() {
        dbQueries.transactionWithContext(ioDispatcher) {
            dbQueries.deleteReports()
        }
    }
}