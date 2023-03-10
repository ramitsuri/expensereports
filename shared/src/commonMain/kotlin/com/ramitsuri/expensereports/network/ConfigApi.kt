package com.ramitsuri.expensereports.network

import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get

internal class ConfigApiImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val dispatcherProvider: DispatcherProvider
) : ConfigApi {

    override suspend fun get(): NetworkResponse<ConfigDto> {
        val apiUrl = "$baseUrl/config.json"
        return apiRequest(dispatcherProvider.io) { client.get(apiUrl) }
    }
}

interface ConfigApi {
    suspend fun get(): NetworkResponse<ConfigDto>
}