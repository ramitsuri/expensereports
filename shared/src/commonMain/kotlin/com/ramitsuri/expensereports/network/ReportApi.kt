package com.ramitsuri.expensereports.network

import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.ReportWithTotal
import com.ramitsuri.expensereports.data.ReportWithoutTotal
import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get

internal class ReportApiImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val dispatcherProvider: DispatcherProvider
) : ReportApi {
    override suspend fun getWithTotal(
        year: Int,
        type: ReportType
    ): NetworkResponse<ReportWithTotalDto> {
        return apiRequest(dispatcherProvider.io) { client.get(getApiUrl(year, type)) }
    }

    override suspend fun getWithoutTotal(
        year: Int,
        type: ReportType
    ): NetworkResponse<ReportWithoutTotalDto> {
        return apiRequest(dispatcherProvider.io) { client.get(getApiUrl(year, type)) }
    }

    private fun getApiUrl(year: Int, reportType: ReportType): String {
        return "$baseUrl/${year}_${reportType.reportName}.json"
    }
}

interface ReportApi {

    suspend fun getWithTotal(year: Int, type: ReportType): NetworkResponse<ReportWithTotalDto>

    suspend fun getWithoutTotal(year: Int, type: ReportType): NetworkResponse<ReportWithoutTotalDto>
}