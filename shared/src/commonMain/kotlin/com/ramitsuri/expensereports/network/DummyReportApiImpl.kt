package com.ramitsuri.expensereports.network

import com.ramitsuri.expensereports.data.ReportType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal class DummyReportApiImpl(private val json: Json) : ReportApi {

    override suspend fun get(
        year: Int,
        type: ReportType
    ): NetworkResponse<ReportDto> {
        val dto = json.decodeFromString<ReportDto>(getReportJson(year))
        return NetworkResponse.Success(dto)
    }

    override val allowsCaching: Boolean = false
}

fun getReportJson(year: Int) = """
    {
      "name": "$year Expenses",
      "time": "2023-02-19T13:55:15Z",
      "account_total": {
        "name": "Expenses",
        "fullname": "Expenses",
        "children": [
          {
            "name": "Rent",
            "fullname": "Expenses:Rent",
            "children": [],
            "balances": [
              {
                "month": 1,
                "amount": "2000.0"
              },
              {
                "month": 2,
                "amount": "2010.0"
              },
              {
                "month": 3,
                "amount": "2110.0"
              },
              {
                "month": 4,
                "amount": "1980.0"
              },
              {
                "month": 5,
                "amount": "1230.0"
              },
              {
                "month": 6,
                "amount": "1800.0"
              },
              {
                "month": 7,
                "amount": "1400.0"
              },
              {
                "month": 8,
                "amount": "1200.0"
              },
              {
                "month": 9,
                "amount": "2110.0"
              },
              {
                "month": 10,
                "amount": "2780.0"
              },
              {
                "month": 11,
                "amount": "2120.0"
              },
              {
                "month": 12,
                "amount": "2230.0"
              }
            ],
            "total": "5700.0"
          },
          {
            "name": "Groceries",
            "fullname": "Expenses:Groceries",
            "children": [],
            "balances": [
              {
                "month": 1,
                "amount": "126.0"
              },
              {
                "month": 2,
                "amount": "136.5"
              },
              {
                "month": 3,
                "amount": "45.0"
              },
              {
                "month": 4,
                "amount": "21.0"
              },
              {
                "month": 5,
                "amount": "67.5"
              },
              {
                "month": 6,
                "amount": "22.0"
              },
              {
                "month": 7,
                "amount": "13.0"
              },
              {
                "month": 8,
                "amount": "14.0"
              },
              {
                "month": 9,
                "amount": "20.0"
              },
              {
                "month": 10,
                "amount": "30.0"
              },
              {
                "month": 11,
                "amount": "44.0"
              },
              {
                "month": 12,
                "amount": "45.75"
              }
            ],
            "total": "142.5"
          },
          {
            "name": "Food",
            "fullname": "Expenses:Food",
            "children": [],
            "balances": [
              {
                "month": 1,
                "amount": "45.0"
              },
              {
                "month": 2,
                "amount": "35.0"
              },
              {
                "month": 3,
                "amount": "120.0"
              },
              {
                "month": 4,
                "amount": "50.0"
              },
              {
                "month": 5,
                "amount": "78.0"
              },
              {
                "month": 6,
                "amount": "91.0"
              },
              {
                "month": 7,
                "amount": "32.0"
              },
              {
                "month": 8,
                "amount": "45.0"
              },
              {
                "month": 9,
                "amount": "67.0"
              },
              {
                "month": 10,
                "amount": "89.0"
              },
              {
                "month": 11,
                "amount": "98.0"
              },
              {
                "month": 12,
                "amount": "100.0"
              }
            ],
            "total": "35.0"
          },
          {
            "name": "Travel",
            "fullname": "Expenses:Travel",
            "children": [],
            "balances": [
              {
                "month": 1,
                "amount": "123.17"
              },
              {
                "month": 2,
                "amount": "521.97"
              },
              {
                "month": 3,
                "amount": "214.0"
              },
              {
                "month": 4,
                "amount": "650.0"
              },
              {
                "month": 5,
                "amount": "360.0"
              },
              {
                "month": 6,
                "amount": "340.0"
              },
              {
                "month": 7,
                "amount": "980.0"
              },
              {
                "month": 8,
                "amount": "160.0"
              },
              {
                "month": 9,
                "amount": "250.0"
              },
              {
                "month": 10,
                "amount": "340.0"
              },
              {
                "month": 11,
                "amount": "140.0"
              },
              {
                "month": 12,
                "amount": "130.0"
              }
            ],
            "total": "694.14"
          }
        ],
        "balances": [
          {
            "month": 1,
            "amount": "0.0"
          },
          {
            "month": 2,
            "amount": "0.0"
          },
          {
            "month": 3,
            "amount": "0.0"
          },
          {
            "month": 4,
            "amount": "0.0"
          },
          {
            "month": 5,
            "amount": "0.0"
          },
          {
            "month": 6,
            "amount": "0.0"
          },
          {
            "month": 7,
            "amount": "0.0"
          },
          {
            "month": 8,
            "amount": "0.0"
          },
          {
            "month": 9,
            "amount": "0.0"
          },
          {
            "month": 10,
            "amount": "0.0"
          },
          {
            "month": 11,
            "amount": "0.0"
          },
          {
            "month": 12,
            "amount": "0.0"
          }
        ],
        "total": "100.00"
      }
    }
""".trimIndent()