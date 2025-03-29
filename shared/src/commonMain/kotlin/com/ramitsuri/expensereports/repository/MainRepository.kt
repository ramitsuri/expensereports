package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.database.dao.CurrentBalancesDao
import com.ramitsuri.expensereports.database.dao.ReportsDao
import com.ramitsuri.expensereports.database.dao.TransactionsDao
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.network.api.Api
import com.ramitsuri.expensereports.settings.Settings
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

class MainRepository internal constructor(
    private val api: Api,
    private val transactionsDao: TransactionsDao,
    private val currentBalancesDao: CurrentBalancesDao,
    private val reportsDao: ReportsDao,
    private val settings: Settings,
    private val clock: Clock = Clock.System,
) {
    suspend fun refresh() {
        val fetchFromStart = clock.now().minus(settings.getLastFullFetchTime()) >= 21.days
        listOf(
            refreshTransactions(fetchFromStart),
            refreshCurrentBalances(fetchFromStart),
            refreshReports(fetchFromStart),
        ).let { results ->
            if (fetchFromStart && results.all { successful -> successful }) {
                settings.setLastFullFetchTime(clock.now())
            }
        }
    }

    fun getTransactions(
        start: LocalDate,
        end: LocalDate
    ) = transactionsDao.get(
        start = start,
        end = end
    )

    fun getCurrentBalances() = currentBalancesDao.get()

    fun getReport(
        reportName: String,
        monthYears: List<MonthYear>
    ) = reportsDao.get(reportName, monthYears)

    private suspend fun refreshTransactions(fetchFromStart: Boolean): Boolean {
        val baseUrl = settings.getBaseUrl()
        api.getTransactions(
            baseUrl = baseUrl,
            since = settings.getLastTxFetchTime().toSince(fetchFromStart)
        ).onSuccess {
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
            since = settings.getLastCurrentBalancesFetchTime().toSince(fetchFromStart)
        ).onSuccess {
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
            since = settings.getLastReportsFetchTime().toSince(fetchFromStart)
        ).onSuccess {
            reportsDao.insert(it)
            settings.setLastReportsFetchTime(clock.now())
            return true
        }
        return false
    }

    private suspend fun Instant.toSince(fetchFromStart: Boolean) =
        if (fetchFromStart) {
            MonthYear(Month.JANUARY, 2019)
        } else {
            toLocalDateTime(settings.getTimeZone())
                .let { MonthYear(it.month, it.year) }
        }
}