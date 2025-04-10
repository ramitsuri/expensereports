package com.ramitsuri.expensereports.network.api

import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Report
import com.ramitsuri.expensereports.model.RunInfo
import com.ramitsuri.expensereports.model.Transaction

internal interface Api {
    suspend fun getTransactions(
        baseUrl: String,
        since: MonthYear,
    ): Result<List<Transaction>>

    suspend fun getCurrentBalances(
        baseUrl: String,
        since: MonthYear,
    ): Result<List<CurrentBalance>>

    suspend fun getReports(
        baseUrl: String,
        since: MonthYear,
    ): Result<List<Report>>

    suspend fun getRunInfo(baseUrl: String): Result<RunInfo>
}
