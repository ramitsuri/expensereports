package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.data.Error
import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.Response
import com.ramitsuri.expensereports.data.db.ReportDao
import com.ramitsuri.expensereports.data.isStale
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.report.ReportApi
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
            val isReportFromDbNull = reportFromDb == null
            val isReportStale = reportFromDb?.isStale(clock.now()) ?: true
            val getFromNetwork = refresh || isReportFromDbNull || isReportStale
            LogHelper.d(
                TAG,
                "ReportFromDb null:$isReportFromDbNull, Stale: $isReportStale, Refresh: $refresh"
            )
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

    fun get(years: List<Int>, types: List<ReportType>): Flow<List<Report>> {
        return dao.get(years, types)
    }

    private suspend fun getFromNetwork(
        year: Int,
        type: ReportType
    ): Report? {
        return when (val response = api.get(year, type)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(
                    TAG, "Error: $response.error, message: ${response.throwable?.message}"
                )
                null
            }
            is NetworkResponse.Success -> {
                val report = Report(response.data, fetchedAt = clock.now(), type, year)
                if (api.allowsCaching) {
                    dao.insert(year, type, report)
                }
                report
            }
        }
    }

    companion object {
        private const val TAG = "ReportsRepo"
    }
}