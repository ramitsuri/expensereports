package com.ramitsuri.expensereports.network.report

import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.ReportDto
import com.ramitsuri.expensereports.network.apiRequest
import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get

internal class ReportApiImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val dispatcherProvider: DispatcherProvider
) : ReportApi {
    override suspend fun get(
        year: Int,
        type: ReportType
    ): NetworkResponse<ReportDto> {
        val apiUrl = "$baseUrl/${year}_${type.reportName}.json"
        return apiRequest(dispatcherProvider.io) { client.get(apiUrl) }
    }

    override val allowsCaching: Boolean = true
}

interface ReportApi {

    suspend fun get(year: Int, type: ReportType): NetworkResponse<ReportDto>

    val allowsCaching: Boolean
}