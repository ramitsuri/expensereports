package com.ramitsuri.expensereports.network

import kotlinx.serialization.json.Json

class DummyTransactionsApiImpl(private val json: Json) : TransactionsApi {
    override suspend fun get(year: Int, month: Int): NetworkResponse<TransactionsDto> {
        TODO("Not yet implemented")
    }

    override val allowsCaching: Boolean = false
}