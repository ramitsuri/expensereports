package com.ramitsuri.expensereports.network

import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get

internal class ReportApiImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val dispatcherProvider: DispatcherProvider
) : ReportApi {

    override suspend fun getExpenseDetailReport(year: Int): NetworkResponse<ReportWithTotalDto> {
        val url = "$baseUrl/${year}_Expenses.json"
        return apiRequest(dispatcherProvider.io) { client.get(url) }
    }

    override suspend fun getAssetsDetailReport(year: Int): NetworkResponse<ReportWithoutTotalDto> {
        val url = "$baseUrl/${year}_Assets.json"
        return apiRequest(dispatcherProvider.io) { client.get(url) }
    }

    override suspend fun getLiabilitiesDetailReport(year: Int): NetworkResponse<ReportWithoutTotalDto> {
        val url = "$baseUrl/${year}_Liabilities.json"
        return apiRequest(dispatcherProvider.io) { client.get(url) }
    }

    override suspend fun getIncomeDetailReport(year: Int): NetworkResponse<ReportWithTotalDto> {
        val url = "$baseUrl/${year}_Income.json"
        return apiRequest(dispatcherProvider.io) { client.get(url) }
    }

    override suspend fun getNetWorthDetailReport(year: Int): NetworkResponse<ReportWithoutTotalDto> {
        val url = "$baseUrl/${year}_NetWorth.json"
        return apiRequest(dispatcherProvider.io) { client.get(url) }
    }

    override suspend fun getSavingsDetailReport(year: Int): NetworkResponse<ReportWithTotalDto> {
        val url = "$baseUrl/${year}_Savings.json"
        return apiRequest(dispatcherProvider.io) { client.get(url) }
    }

    override suspend fun getExpenseAfterDeductionReport(year: Int): NetworkResponse<ReportWithTotalDto> {
        val url = "$baseUrl/${year}_After_Deduction_Expenses.json"
        return apiRequest(dispatcherProvider.io) { client.get(url) }
    }
}

interface ReportApi {
    suspend fun getExpenseDetailReport(year: Int): NetworkResponse<ReportWithTotalDto>

    suspend fun getAssetsDetailReport(year: Int): NetworkResponse<ReportWithoutTotalDto>

    suspend fun getLiabilitiesDetailReport(year: Int): NetworkResponse<ReportWithoutTotalDto>

    suspend fun getIncomeDetailReport(year: Int): NetworkResponse<ReportWithTotalDto>

    suspend fun getNetWorthDetailReport(year: Int): NetworkResponse<ReportWithoutTotalDto>

    suspend fun getSavingsDetailReport(year: Int): NetworkResponse<ReportWithTotalDto>

    suspend fun getExpenseAfterDeductionReport(year: Int): NetworkResponse<ReportWithTotalDto>
}