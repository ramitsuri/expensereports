package com.ramitsuri.expensereports.network

import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class TransactionsApiImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val dispatcherProvider: DispatcherProvider
) : TransactionsApi {

    override suspend fun get(year: Int, month: Int): NetworkResponse<TransactionsDto> {
        val twoDigitMonth = month.toString().padStart(2, '0')
        val apiUrl = "$baseUrl/transactions/$year/$twoDigitMonth.json"
        return apiRequest(dispatcherProvider.io) { client.get(apiUrl) }
    }

    override val allowsCaching: Boolean = true
}

interface TransactionsApi {
    suspend fun get(year: Int, month: Int): NetworkResponse<TransactionsDto>

    val allowsCaching: Boolean
}