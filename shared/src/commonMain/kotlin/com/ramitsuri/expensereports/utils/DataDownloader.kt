package com.ramitsuri.expensereports.utils

import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.data.TransactionGroup
import com.ramitsuri.expensereports.data.db.ReportDao
import com.ramitsuri.expensereports.data.db.TransactionsDao
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.report.ReportApi
import com.ramitsuri.expensereports.network.transactiongroups.TransactionGroupsApi
import com.ramitsuri.expensereports.network.transactions.TransactionsApi
import com.ramitsuri.expensereports.repository.MiscellaneousRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class DataDownloader(
    private val reportDao: ReportDao,
    private val reportApi: ReportApi,
    private val transactionsDao: TransactionsDao,
    private val transactionsApi: TransactionsApi,
    private val transactionGroupsApi: TransactionGroupsApi,
    private val miscellaneousRepository: MiscellaneousRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
    private val prefManager: PrefManager
) {
    suspend fun download() {
        downloadAndSaveReports()
        downloadAndSaveTransactions()
        downloadAndSaveTransactionGroups()
        miscellaneousRepository.downloadAndSave()
        prefManager.setLastDownloadTime(clock.now())
    }

    private suspend fun downloadAndSaveReports() {
        val reportTypes = ReportType.values()
        val currentYear = clock.now().toLocalDateTime(timeZone).year
        val years = if (prefManager.shouldDownloadRecentData()) {
            listOf(currentYear)
        } else {
            (currentYear downTo currentYear - 2).toList()
        }
        for (reportType in reportTypes) {
            if (reportType == ReportType.NONE) {
                continue
            }
            for (year in years) {
                downloadAndSaveReports(year, reportType)
            }
        }
    }

    private suspend fun downloadAndSaveReports(year: Int, type: ReportType) {
        LogHelper.d(TAG, "Getting $type for $year from network")
        when (val response = reportApi.get(year, type)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
            }

            is NetworkResponse.Success -> {
                val report =
                    Report(response.data, fetchedAt = clock.now(), type, year)
                if (reportApi.allowsCaching) {
                    reportDao.insert(year, type, report)
                }
            }
        }
    }

    private suspend fun downloadAndSaveTransactions() {
        val currentDate = clock.now().toLocalDateTime(timeZone).date
        val dates = if (prefManager.shouldDownloadRecentData()) {
            listOf(currentDate, currentDate.minus(DatePeriod(months = 1)))
        } else {
            getDates(currentDate.minus(DatePeriod(years = 2)), currentDate)
        }
        for (date in dates) {
            downloadAndSaveTransactions(year = date.year, month = date.monthNumber)
        }
    }

    private suspend fun downloadAndSaveTransactions(year: Int, month: Int) {
        LogHelper.d(TAG, "Getting transactions for $year and $month from network")
        when (val response = transactionsApi.get(year, month)) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
            }

            is NetworkResponse.Success -> {
                val transactions = response.data.transactions.map { Transaction(it) }
                if (transactionsApi.allowsCaching) {
                    transactionsDao.insert(year, month, transactions)
                }
            }
        }
    }

    private suspend fun downloadAndSaveTransactionGroups() {
        LogHelper.d(TAG, "Getting transaction groups from network")
        when (val response = transactionGroupsApi.get()) {
            is NetworkResponse.Failure -> {
                LogHelper.e(TAG, "Error: $response.error, message: ${response.throwable?.message}")
            }

            is NetworkResponse.Success -> {
                val transactionGroups = response.data.transactionGroups.map { TransactionGroup(it) }
                if (transactionsApi.allowsCaching) {
                    prefManager.setTransactionGroups(transactionGroups)
                }
            }
        }
    }

    private fun getDates(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dates = mutableSetOf<LocalDate>()
        var date = startDate
        do {
            dates.add(date)
            date = date.plus(DatePeriod(months = 1))
        } while (date <= endDate)
        dates.add(endDate)
        return dates.toList()
    }

    companion object {
        private const val TAG = "ReportsDownloader"
    }
}