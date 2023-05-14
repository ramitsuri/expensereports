package com.ramitsuri.expensereports.network.transactiongroups

import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.TransactionGroupsDto
import com.ramitsuri.expensereports.network.apiRequest
import com.ramitsuri.expensereports.utils.DispatcherProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class TransactionGroupsApiImpl(
    private val client: HttpClient,
    private val baseUrl: String,
    private val dispatcherProvider: DispatcherProvider
) : TransactionGroupsApi {
    override suspend fun get(): NetworkResponse<TransactionGroupsDto> {
        val apiUrl = "$baseUrl/TransactionGroups.json"
        return apiRequest(dispatcherProvider.io) { client.get(apiUrl) }
    }

    override val allowsCaching = true
}


interface TransactionGroupsApi {
    suspend fun get(): NetworkResponse<TransactionGroupsDto>

    val allowsCaching: Boolean
}