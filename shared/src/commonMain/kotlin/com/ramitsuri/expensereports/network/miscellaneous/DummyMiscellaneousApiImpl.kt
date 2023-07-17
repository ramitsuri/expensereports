package com.ramitsuri.expensereports.network.miscellaneous

import com.ramitsuri.expensereports.network.MiscellaneousDataDto
import com.ramitsuri.expensereports.network.NetworkResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal class DummyMiscellaneousApiImpl(private val json: Json) : MiscellaneousApi {

    override suspend fun get(): NetworkResponse<MiscellaneousDataDto> {
        val dto = json.decodeFromString<MiscellaneousDataDto>(getJson())
        return NetworkResponse.Success(dto)
    }

    private fun getJson(): String {
        return """
            {
              "time": "2023-07-16T14:30:43Z",
              "miscellaneous": {
                "income_total": "250",
                "expense_total": "150",
                "expense_after_deduction_total": "100",
                "savings_total": "100",
                "account_balances": [
                  {
                    "name": "Checking",
                    "balance": "100.00"
                  },
                  {
                    "name": "Cash",
                    "balance": "40"
                  },
                  {
                    "name": "Savings",
                    "balance": "1000.00"
                  },
                  {
                    "name": "401K 1",
                    "balance": "220.50"
                  },
                  {
                    "name": "401K 2",
                    "balance": "250.47"
                  },
                  {
                    "name": "Credit Card 1",
                    "balance": "47.85"
                  },
                  {
                    "name": "Credit Card 2",
                    "balance": "99.12"
                  },
                  {
                    "name": "Credit Card 3",
                    "balance": "1.45"
                  }
                ]
              }
            }
        """.trimIndent()
    }
}