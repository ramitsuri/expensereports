package com.ramitsuri.expensereports.network.transactiongroups

import com.ramitsuri.expensereports.network.NetworkError
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.TransactionGroupsDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DummyTransactionGroupsApiImpl(private val json: Json) : TransactionGroupsApi {
    override suspend fun get(): NetworkResponse<TransactionGroupsDto> {
        return if (jsonString.isNotEmpty()) {
            val dto = json.decodeFromString<TransactionGroupsDto>(jsonString)
            NetworkResponse.Success(dto)
        } else {
            NetworkResponse.Failure(error = NetworkError.UNKNOWN, null)
        }
    }

    override val allowsCaching = false

    private val jsonString = """
        {
          "time": "2023-05-14T11:24:32Z",
          "transaction_groups": [
            {
              "name": "Vacation 2023",
              "total": "500"
            }
          ]
        }
    """.trimIndent()
}