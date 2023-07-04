package com.ramitsuri.expensereports.network.miscellaneous

import com.ramitsuri.expensereports.network.MiscellaneousDataDto
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.apiRequest
import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get

internal class MiscellaneousApiImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val dispatcherProvider: DispatcherProvider
) : MiscellaneousApi {

    override suspend fun get(): NetworkResponse<MiscellaneousDataDto> {
        val apiUrl = "$baseUrl/Miscellaneous.json"
        return apiRequest(dispatcherProvider.io) { client.get(apiUrl) }
    }
}

interface MiscellaneousApi {
    suspend fun get(): NetworkResponse<MiscellaneousDataDto>
}