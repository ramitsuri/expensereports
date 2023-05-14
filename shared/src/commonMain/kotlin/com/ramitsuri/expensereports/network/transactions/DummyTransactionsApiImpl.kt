package com.ramitsuri.expensereports.network.transactions

import com.ramitsuri.expensereports.network.NetworkError
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.TransactionsDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DummyTransactionsApiImpl(private val json: Json) : TransactionsApi {
    override suspend fun get(year: Int, month: Int): NetworkResponse<TransactionsDto> {
        return if (jsonString.isNotEmpty()) {
            val dto = json.decodeFromString<TransactionsDto>(jsonString)
            NetworkResponse.Success(dto)
        } else {
            NetworkResponse.Failure(error = NetworkError.UNKNOWN, null)
        }
    }

    override val allowsCaching: Boolean = false

    private val jsonString = """
        {
          "time": "2023-05-14T11:24:32Z",
          "transactions": [
            {
              "date": "2023-05-03",
              "total": "20",
              "splits": [
                {
                  "amount": "20",
                  "account": "Expenses:Travel:Food"
                },
                {
                  "amount": "-20",
                  "account": "Liabilities:Credit Card"
                }
              ],
              "description": "Brewery",
              "num": "Num1"
            },
            {
              "date": "2023-05-03",
              "total": "45",
              "splits": [
                {
                  "amount": "45",
                  "account": "Expenses:Travel:Food"
                },
                {
                  "amount": "-45",
                  "account": "Liabilities:Credit Card"
                }
              ],
              "description": "Restaurant",
              "num": ""
            }
          ]
        }
    """.trimIndent()
}