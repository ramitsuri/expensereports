package com.ramitsuri.expensereports.network

import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get

internal class ReportApiImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val dispatcherProvider: DispatcherProvider
) : ReportApi {

    override suspend fun getExpenseReport(year: Int): NetworkResponse<ExpenseReportDto> {
        val url = "$baseUrl/${year}_Expenses_All.json"
        return apiRequest(dispatcherProvider.io) { client.get(url) }
    }
}

interface ReportApi {
    suspend fun getExpenseReport(year: Int): NetworkResponse<ExpenseReportDto>
}