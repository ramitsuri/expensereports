package com.ramitsuri.expensereports.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ramitsuri.expensereports.database.model.DbReport
import com.ramitsuri.expensereports.database.model.DbReportAccount
import com.ramitsuri.expensereports.database.model.DbReportAccountTotal
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
internal abstract class ReportsDao {
    @Query("SELECT name FROM db_report")
    abstract fun getReportNames(): Flow<List<String>>

    fun get(reportName: String, monthYears: List<MonthYear>): Flow<Report?> {
        return getReport(reportName).map { dbReport ->
            if (dbReport == null) {
                return@map null
            }
            val accounts = getReportAccounts(reportName).map { dbReportAccount ->
                val totals = getReportAccountTotals(
                    reportName = reportName,
                    accountName = dbReportAccount.accountName,
                    monthYears = monthYears
                ).associate { dbReportAccountTotal ->
                    dbReportAccountTotal.monthYear to dbReportAccountTotal.total
                }
                Report.Account(
                    name = dbReportAccount.accountName,
                    order = dbReportAccount.order,
                    monthTotals = totals,
                )
            }
            Report(
                name = dbReport.name,
                withCumulativeBalance = dbReport.withCumulativeBalance,
                accounts = accounts,
            )
        }
    }

    @Transaction
    open suspend fun insert(reports: List<Report>) {
        insertReports(
            reports.map {
                DbReport(
                    name = it.name,
                    withCumulativeBalance = it.withCumulativeBalance
                )
            }
        )
        insertAccounts(
            reports.flatMap { report ->
                report.accounts.mapIndexed { accountIndex, account ->
                    DbReportAccount(
                        reportName = report.name,
                        accountName = account.name,
                        order = accountIndex,
                    )
                }
            }
        )
        insertTotals(
            reports.flatMap { report ->
                report.accounts.flatMap { account ->
                    account.monthTotals.map { (monthYear, total) ->
                        DbReportAccountTotal(
                            reportName = report.name,
                            accountName = account.name,
                            monthYear = monthYear,
                            total = total
                        )
                    }
                }
            }
        )
    }

    @Transaction
    open suspend fun deleteAll() {
        deleteReports()
        deleteReportAccounts()
        deleteReportAccountTotals()
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertReports(reports: List<DbReport>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAccounts(accounts: List<DbReportAccount>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertTotals(totals: List<DbReportAccountTotal>)

    @Query("SELECT * FROM db_report WHERE name = :reportName")
    protected abstract fun getReport(
        reportName: String,
    ): Flow<DbReport?>

    @Query("SELECT * FROM db_report_account WHERE report_name = :reportName")
    protected abstract suspend fun getReportAccounts(
        reportName: String,
    ): List<DbReportAccount>

    @Query(
        "SELECT * FROM db_report_account_total " +
                "WHERE report_name = :reportName " +
                "AND account_name = :accountName " +
                "AND month_year IN (:monthYears)"
    )
    protected abstract suspend fun getReportAccountTotals(
        reportName: String,
        accountName: String,
        monthYears: List<MonthYear>
    ): List<DbReportAccountTotal>

    @Query("DELETE FROM db_report")
    protected abstract suspend fun deleteReports()

    @Query("DELETE FROM db_report_account")
    protected abstract suspend fun deleteReportAccounts()

    @Query("DELETE FROM db_report_account_total")
    protected abstract suspend fun deleteReportAccountTotals()
}