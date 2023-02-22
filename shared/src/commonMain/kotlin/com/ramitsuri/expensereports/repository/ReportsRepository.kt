package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.data.Error
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.ReportWithTotal
import com.ramitsuri.expensereports.data.ReportWithoutTotal
import com.ramitsuri.expensereports.data.Response
import com.ramitsuri.expensereports.data.db.ReportDao
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.ReportApi
import com.ramitsuri.expensereports.utils.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class ReportsRepository(
    private val api: ReportApi,
    private val dao: ReportDao,
    private val clock: Clock,
    private val prefManager: PrefManager
) {

    suspend fun getReportWithTotal(
        year: Int,
        type: ReportType,
        refresh: Boolean = false
    ): Flow<Response<ReportWithTotal>> {
        val shouldRefresh = refresh || shouldRefreshReport(type)
        if (shouldRefresh) {
            refreshWithTotal(year, type)
        }
        return dao.getWithTotal(year, type).map { report ->
            if (report == null) {
                LogHelper.d(TAG, "Report not in local storage")
                if (!shouldRefresh) { // Refresh only if not already tried
                    refreshWithTotal(year, type)
                }
                Response.Failure(Error.UNAVAILABLE)
            } else {
                Response.Success(report)
            }
        }
    }

    suspend fun getReportWithoutTotal(
        year: Int,
        type: ReportType,
        refresh: Boolean = false
    ): Flow<Response<ReportWithoutTotal>> {
        val shouldRefresh = refresh || shouldRefreshReport(type)
        if (shouldRefresh) {
            refreshWithTotal(year, type)
        }
        return dao.getWithoutTotal(year, type).map { report ->
            if (report == null) {
                LogHelper.d(TAG, "Report not in local storage")
                if (!shouldRefresh) { // Refresh only if not already tried
                    refreshWithTotal(year, type)
                }
                Response.Failure(Error.UNAVAILABLE)
            } else {
                Response.Success(report)
            }
        }
    }

    private suspend fun refreshWithTotal(
        year: Int,
        type: ReportType
    ) {
        when (val response = api.getWithTotal(year, type)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
            }
            is NetworkResponse.Success -> {
                saveReportFetchTimestamp(type)
                val report = ReportWithTotal(response.data)
                dao.insert(year, type, report)
            }
        }
    }

    private suspend fun refreshWithoutTotal(
        year: Int,
        type: ReportType
    ) {
        when (val response = api.getWithoutTotal(year, type)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
            }
            is NetworkResponse.Success -> {
                saveReportFetchTimestamp(type)
                val report = ReportWithoutTotal(response.data)
                dao.insert(year, type, report)
            }
        }
    }

    private fun shouldRefreshReport(type: ReportType): Boolean {
        val lastFetchTimestamp = when (type) {
            ReportType.NONE -> {
                clock.now()
            }
            ReportType.EXPENSE -> {
                prefManager.getExpenseReportFetchTimestamp()
            }
            ReportType.EXPENSE_AFTER_DEDUCTION -> {
                clock.now()
            }
            ReportType.ASSETS -> {
                clock.now()
            }
            ReportType.LIABILITIES -> {
                clock.now()
            }
            ReportType.INCOME -> {
                clock.now()
            }
            ReportType.NET_WORTH -> {
                clock.now()
            }
            ReportType.SAVINGS -> {
                clock.now()
            }
        }
        return clock.now().minus(lastFetchTimestamp).inWholeHours >= REFRESH_THRESHOLD_HOURS
    }

    private fun saveReportFetchTimestamp(type: ReportType) {
        when (type) {
            ReportType.NONE -> {
                // TODO
            }
            ReportType.EXPENSE -> {
                prefManager.setExpenseReportFetchTimestamp(clock.now())
            }
            ReportType.EXPENSE_AFTER_DEDUCTION -> {
                // TODO
            }
            ReportType.ASSETS -> {
                // TODO
            }
            ReportType.LIABILITIES -> {
                // TODO
            }
            ReportType.INCOME -> {
                // TODO
            }
            ReportType.NET_WORTH -> {
                // TODO
            }
            ReportType.SAVINGS -> {
                // TODO
            }
        }
    }

    companion object {
        private const val TAG = "ReportsRepo"
        private const val REFRESH_THRESHOLD_HOURS = 6
    }
}