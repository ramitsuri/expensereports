package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.database.dao.CurrentBalancesDao
import com.ramitsuri.expensereports.database.dao.ReportsDao
import com.ramitsuri.expensereports.database.dao.TransactionsDao
import com.ramitsuri.expensereports.log.logI
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.RunInfo
import com.ramitsuri.expensereports.network.api.Api
import com.ramitsuri.expensereports.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class MainRepository internal constructor(
    private val api: Api,
    private val transactionsDao: TransactionsDao,
    private val currentBalancesDao: CurrentBalancesDao,
    private val reportsDao: ReportsDao,
    private val settings: Settings,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    suspend fun refresh(forced: Boolean = false) {
        val now = clock.now()
        val lastFetch = settings.getLastFetchTime()
        val runInfo = getRunInfo()
        if (runInfo == null) {
            if ((now.minus(lastFetch) < 6.hours) && !forced) {
                logI(TAG) { "Skipping refresh, less than 6 hours since last fetch" }
                return
            }
        } else {
            if (lastFetch > runInfo.lastRunTime) {
                logI(TAG) { "Skipping refresh, last fetch time is after last run time" }
                return
            }
        }
        val fetchFromStart = (now.minus(settings.getLastFullFetchTime()) >= 21.days) || forced
        logI(TAG) { "Refreshing data, fetchFromStart: $fetchFromStart" }
        listOf(
            refreshTransactions(fetchFromStart),
            refreshCurrentBalances(fetchFromStart),
            refreshReports(fetchFromStart),
            refreshRunInfo(),
        ).let { results ->
            if (fetchFromStart && results.all { successful -> successful }) {
                settings.setLastFullFetchTime(clock.now())
            } else if (results.all { successful -> successful }) {
                settings.setLastFetchTime(clock.now())
            }
        }
    }

    fun getTransactions(
        description: String?,
        start: LocalDate,
        end: LocalDate,
        fromAccount: String?,
        toAccount: String?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
    ) = if (description.isNullOrEmpty()) {
        transactionsDao.get(
            start = start,
            end = end,
        )
    } else {
        transactionsDao.get(
            description = description,
            start = start,
            end = end,
        )
    }.map { transactions ->
        transactions.filter { transaction ->
            val fromAccountMatch =
                fromAccount.isNullOrEmpty() ||
                    transaction.fromSplits().any { it.accountName.contains(fromAccount, ignoreCase = true) }
            val toAccountMatch =
                toAccount.isNullOrEmpty() ||
                    transaction.toSplits().any { it.accountName.contains(toAccount, ignoreCase = true) }

            val totalAmount = transaction.fromSplits().sumOf { it.amount.abs() }

            val minAmountMatch = minAmount == null || totalAmount >= minAmount
            val maxAmountMatch = maxAmount == null || totalAmount <= maxAmount

            fromAccountMatch && toAccountMatch && minAmountMatch && maxAmountMatch
        }
    }

    fun getCurrentBalances() = currentBalancesDao.get()

    fun getReport(
        reportName: String,
        monthYears: List<MonthYear>,
    ) = reportsDao.get(reportName, monthYears)

    fun getAllAccountNames(): Flow<List<String>> {
        return transactionsDao.getAll().map { transactions ->
            transactions
                .flatMap { it.splits }
                .map { it.accountName }
                .distinct()
                .sorted()
        }
    }

    private suspend fun refreshTransactions(fetchFromStart: Boolean): Boolean {
        val baseUrl = settings.getBaseUrl()
        api.getTransactions(
            baseUrl = baseUrl,
            since = settings.getLastTxFetchTime().toSince(fetchFromStart),
        ).onSuccess {
            if (fetchFromStart) {
                transactionsDao.deleteAll()
            }
            transactionsDao.insert(it)
            settings.setLastTxFetchTime(clock.now())
            return true
        }
        return false
    }

    private suspend fun refreshCurrentBalances(fetchFromStart: Boolean): Boolean {
        val baseUrl = settings.getBaseUrl()
        api.getCurrentBalances(
            baseUrl = baseUrl,
            since = settings.getLastCurrentBalancesFetchTime().toSince(fetchFromStart),
        ).onSuccess {
            if (fetchFromStart) {
                currentBalancesDao.deleteAll()
            }
            currentBalancesDao.insert(it)
            settings.setLastCurrentBalancesFetchTime(clock.now())
            return true
        }
        return false
    }

    private suspend fun refreshReports(fetchFromStart: Boolean): Boolean {
        val baseUrl = settings.getBaseUrl()
        api.getReports(
            baseUrl = baseUrl,
            since = settings.getLastReportsFetchTime().toSince(fetchFromStart),
        ).onSuccess {
            if (fetchFromStart) {
                reportsDao.deleteAll()
            }
            reportsDao.insert(it)
            settings.setLastReportsFetchTime(clock.now())
            return true
        }
        return false
    }

    private suspend fun refreshRunInfo(): Boolean {
        val runInfo = getRunInfo()
        if (runInfo != null) {
            settings.setRunInfo(runInfo)
            return true
        } else {
            return false
        }
    }

    private suspend fun getRunInfo(): RunInfo? {
        val baseUrl = settings.getBaseUrl()
        api.getRunInfo(
            baseUrl = baseUrl,
        ).onSuccess {
            return it
        }
        return null
    }

    private fun Instant.toSince(fetchFromStart: Boolean) =
        if (fetchFromStart) {
            MonthYear(Month.JANUARY, 2019)
        } else {
            toLocalDateTime(timeZone).let { MonthYear(it.month, it.year) }
        }

    companion object {
        private const val TAG = "MainRepository"
    }
}
