package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.data.Error
import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.Response
import com.ramitsuri.expensereports.data.db.ReportDao
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.ReportApi
import com.ramitsuri.expensereports.utils.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class ReportsRepository(
    private val api: ReportApi,
    private val dao: ReportDao,
    private val clock: Clock
) {

    suspend fun getReport(
        year: Int,
        type: ReportType,
        refresh: Boolean = false
    ): Flow<Response<Report>> {
        return dao.get(year, type).map { reportFromDb ->
            val getFromNetwork = reportFromDb == null || refresh || isStale(reportFromDb)
            val report = if (getFromNetwork) {
                getFromNetwork(year, type)
            } else {
                reportFromDb
            }
            if (report == null) {
                Response.Failure(Error.UNAVAILABLE)
            } else {
                Response.Success(report)
            }
        }
    }

    private suspend fun getFromNetwork(
        year: Int,
        type: ReportType
    ): Report? {
        LogHelper.d(TAG, "Getting $type for $year from network")
        return when (val response = api.get(year, type)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                null
            }
            is NetworkResponse.Success -> {
                val report = Report(response.data, fetchedAt = clock.now(), type.hasTotal)
                dao.insert(year, type, report)
                report
            }
        }
    }

    private fun isStale(report: Report): Boolean {
        val lastFetchTimestamp = report.fetchedAt
        return clock.now().minus(lastFetchTimestamp).inWholeMilliseconds >= REFRESH_THRESHOLD_MS
    }

    companion object {
        private const val TAG = "ReportsRepo"
        private const val REFRESH_THRESHOLD_MS = 6 * 60 * 60 * 20_000 // 6 Hours
    }
}