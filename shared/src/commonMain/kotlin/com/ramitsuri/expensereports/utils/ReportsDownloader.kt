package com.ramitsuri.expensereports.utils

import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.db.ReportDao
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.ReportApi
import kotlinx.datetime.Clock

class ReportsDownloader(
    private val dao: ReportDao,
    private val api: ReportApi,
    private val clock: Clock,
) {
    suspend fun downloadAndSaveAll() {
        val reportTypes = ReportType.values()
        for (reportType in reportTypes) {
            if (reportType == ReportType.NONE) {
                continue
            }

            for (year in years) {
                downloadAndSave(year, reportType)
            }
        }
    }

    suspend fun downloadAndSave(year: Int, type: ReportType): Report? {
        LogHelper.d(TAG, "Getting $type for $year from network")
        return when (val response = api.get(year, type)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
                null
            }
            is NetworkResponse.Success -> {
                val report =
                    Report(response.data, fetchedAt = clock.now(), type.hasTotal, type, year)
                dao.insert(year, type, report)
                report
            }
        }
    }

    companion object {
        private const val TAG = "ReportsDownloader"
        private val years = listOf(2021, 2022, 2023)
    }
}