package com.ramitsuri.expensereports.network.report

import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.network.NetworkError
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.ReportDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal class DummyReportApiImpl(private val json: Json) : ReportApi {

    override suspend fun get(
        year: Int,
        type: ReportType
    ): NetworkResponse<ReportDto> {
        val jsonString: String = when (type) {
            ReportType.NONE -> {
                ""
            }

            ReportType.EXPENSE -> {
                getExpenseReportJson(year)
            }

            ReportType.EXPENSE_AFTER_DEDUCTION -> {
                getExpenseAfterDeductionReportJson(year)
            }

            ReportType.ASSETS -> {
                getAssetsReportJson(year)
            }

            ReportType.LIABILITIES -> {
                getLiabilitiesReportJson(year)
            }

            ReportType.INCOME -> {
                getIncomeReportJson(year)
            }

            ReportType.NET_WORTH -> {
                getNetWorthReportJson(year)
            }

            ReportType.SAVINGS -> {
                getSavingsReportJson(year)
            }
        }
        return if (jsonString.isNotEmpty()) {
            val dto = json.decodeFromString<ReportDto>(jsonString)
            NetworkResponse.Success(dto)
        } else {
            NetworkResponse.Failure(error = NetworkError.UNKNOWN, null)
        }
    }

    override val allowsCaching: Boolean = false

    private fun getExpenseReportJson(year: Int) = """
    {
      "name": "$year Expenses",
      "time": "$year-02-19T13:55:15Z",
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

    private fun getExpenseAfterDeductionReportJson(year: Int) = getExpenseReportJson(year)

    private fun getAssetsReportJson(year: Int) = """
        {
          "name": "$year Assets",
          "time": "$year-02-19T13:55:15Z",
          "account_total": {
            "name": "Assets",
            "fullName": "Assets",
            "children": [
              {
                "name": "Investments",
                "fullName": "Assets:Investments",
                "children": [
                  {
                    "name": "Retirement",
                    "fullName": "Assets:Investments:Retirement",
                    "children": [
                      {
                        "name": "401K",
                        "fullName": "Assets:Investments:Retirement:401K",
                        "children": [],
                        "monthAmounts": {
                          "1": "100",
                          "2": "200",
                          "3": "300",
                          "4": "400",
                          "5": "500",
                          "6": "600",
                          "7": "700",
                          "8": "800",
                          "9": "900",
                          "10": "1000",
                          "11": "1100",
                          "12": "1200"
                        }
                      }
                    ],
                    "monthAmounts": {
                      "1": "100",
                      "2": "200",
                      "3": "300",
                      "4": "400",
                      "5": "500",
                      "6": "600",
                      "7": "700",
                      "8": "800",
                      "9": "900",
                      "10": "1000",
                      "11": "1100",
                      "12": "1200"
                    }
                  }
                ],
                "monthAmounts": {
                  "1": "100",
                  "2": "200",
                  "3": "300",
                  "4": "400",
                  "5": "500",
                  "6": "600",
                  "7": "700",
                  "8": "800",
                  "9": "900",
                  "10": "1000",
                  "11": "1100",
                  "12": "1200"
                }
              },
              {
                "name": "Current Assets",
                "fullName": "Assets:Current Assets",
                "children": [
                  {
                    "name": "Checking",
                    "fullName": "Assets:Current Assets:Checking",
                    "children": [],
                    "monthAmounts": {
                      "1": "100",
                      "2": "200",
                      "3": "300",
                      "4": "400",
                      "5": "500",
                      "6": "600",
                      "7": "700",
                      "8": "800",
                      "9": "900",
                      "10": "1000",
                      "11": "1100",
                      "12": "1200"
                    }
                  },
                  {
                    "name": "Savings ",
                    "fullName": "Assets:Current Assets:Savings ",
                    "children": [],
                    "monthAmounts": {
                      "1": "100",
                      "2": "200",
                      "3": "300",
                      "4": "400",
                      "5": "500",
                      "6": "600",
                      "7": "700",
                      "8": "800",
                      "9": "900",
                      "10": "1000",
                      "11": "1100",
                      "12": "1200"
                    }
                  }
                ],
                "monthAmounts": {
                  "1": "100",
                  "2": "200",
                  "3": "300",
                  "4": "400",
                  "5": "500",
                  "6": "600",
                  "7": "700",
                  "8": "800",
                  "9": "900",
                  "10": "1000",
                  "11": "1100",
                  "12": "1200"
                }
              }
            ],
            "monthAmounts": {
              "1": "100",
              "2": "200",
              "3": "300",
              "4": "400",
              "5": "500",
              "6": "600",
              "7": "700",
              "8": "800",
              "9": "900",
              "10": "1000",
              "11": "1100",
              "12": "1200"
            }
          }
        }
    """.trimIndent()

    private fun getLiabilitiesReportJson(year: Int) = """
    {
  "name": "$year Liabilities",
  "time": "$year-02-19T13:55:15Z",
  "account_total": {
    "name": "Liabilities",
    "fullName": "Liabilities",
    "children": [
      {
        "name": "CreditCard",
        "fullName": "Liabilities:CreditCard",
        "children": [
          {
            "name": "CreditCard1",
            "fullName": "Liabilities:CreditCard:CreditCard1",
            "children": [],
            "monthAmounts": {
              "1": "100",
              "2": "200",
              "3": "300",
              "4": "400",
              "5": "500",
              "6": "600",
              "7": "700",
              "8": "800",
              "9": "900",
              "10": "1000",
              "11": "1100",
              "12": "1200"
            }
          }
        ],
        "monthAmounts": {
          "1": "100",
          "2": "200",
          "3": "300",
          "4": "400",
          "5": "500",
          "6": "600",
          "7": "700",
          "8": "800",
          "9": "900",
          "10": "1000",
          "11": "1100",
          "12": "1200"
        }
      }
    ],
    "monthAmounts": {
      "1": "100",
      "2": "200",
      "3": "300",
      "4": "400",
      "5": "500",
      "6": "600",
      "7": "700",
      "8": "800",
      "9": "900",
      "10": "1000",
      "11": "1100",
      "12": "1200"
    }
  }
}"""

    private fun getNetWorthReportJson(year: Int) = """
               {
  "name": "$year NetWorth",
  "time": "$year-08-21T12:30:45Z",
  "account_total": {
    "name": "Networth",
    "fullname": "Networth",
    "children": [],
    "balances": [
      {
        "month": 1,
        "amount": "1000"
      },
      {
        "month": 2,
        "amount": "1100"
      },
      {
        "month": 3,
        "amount": "1200"
      },
      {
        "month": 4,
        "amount": "1300"
      },
      {
        "month": 5,
        "amount": "1400"
      },
      {
        "month": 6,
        "amount": "1500"
      },
      {
        "month": 7,
        "amount": "1600"
      },
      {
        "month": 8,
        "amount": "1700"
      },
      {
        "month": 9,
        "amount": "1800"
      },
      {
        "month": 10,
        "amount": "1900"
      },
      {
        "month": 11,
        "amount": "2000"
      },
      {
        "month": 12,
        "amount": "2100"
      }
    ],
    "total": "0.0"
  }
}
    """.trimIndent()

    private fun getSavingsReportJson(year: Int) = """
        {
          "name": "$year Savings",
          "time": "$year-02-19T13:55:15Z",
          "account_total": {
            "name": "Savings",
            "fullName": "Savings",
            "children": [
              {
                "name": "Investments",
                "fullName": "Assets:Investments",
                "children": [
                  {
                    "name": "Retirement",
                    "fullName": "Assets:Investments:Retirement",
                    "children": [
                      {
                        "name": "401K",
                        "fullName": "Assets:Investments:Retirement:401K",
                        "children": [],
                        "monthAmounts": {
                          "1": "100",
                          "2": "200",
                          "3": "300",
                          "4": "400",
                          "5": "500",
                          "6": "600",
                          "7": "700",
                          "8": "800",
                          "9": "900",
                          "10": "1000",
                          "11": "1100",
                          "12": "1200"
                        }
                      }
                    ],
                    "monthAmounts": {
                      "1": "100",
                      "2": "200",
                      "3": "300",
                      "4": "400",
                      "5": "500",
                      "6": "600",
                      "7": "700",
                      "8": "800",
                      "9": "900",
                      "10": "1000",
                      "11": "1100",
                      "12": "1200"
                    }
                  }
                ],
                "monthAmounts": {
                  "1": "100",
                  "2": "200",
                  "3": "300",
                  "4": "400",
                  "5": "500",
                  "6": "600",
                  "7": "700",
                  "8": "800",
                  "9": "900",
                  "10": "1000",
                  "11": "1100",
                  "12": "1200"
                }
              },
              {
                "name": "Current Assets",
                "fullName": "Assets:Current Assets",
                "children": [
                  {
                    "name": "Checking",
                    "fullName": "Assets:Current Assets:Checking",
                    "children": [],
                    "monthAmounts": {
                      "1": "100",
                      "2": "200",
                      "3": "300",
                      "4": "400",
                      "5": "500",
                      "6": "600",
                      "7": "700",
                      "8": "800",
                      "9": "900",
                      "10": "1000",
                      "11": "1100",
                      "12": "1200"
                    }
                  },
                  {
                    "name": "Savings ",
                    "fullName": "Assets:Current Assets:Savings ",
                    "children": [],
                    "monthAmounts": {
                      "1": "100",
                      "2": "200",
                      "3": "300",
                      "4": "400",
                      "5": "500",
                      "6": "600",
                      "7": "700",
                      "8": "800",
                      "9": "900",
                      "10": "1000",
                      "11": "1100",
                      "12": "1200"
                    }
                  }
                ],
                "monthAmounts": {
                  "1": "100",
                  "2": "200",
                  "3": "300",
                  "4": "400",
                  "5": "500",
                  "6": "600",
                  "7": "700",
                  "8": "800",
                  "9": "900",
                  "10": "1000",
                  "11": "1100",
                  "12": "1200"
                }
              }
            ],
            "monthAmounts": {
              "1": "100",
              "2": "200",
              "3": "300",
              "4": "400",
              "5": "500",
              "6": "600",
              "7": "700",
              "8": "800",
              "9": "900",
              "10": "1000",
              "11": "1100",
              "12": "1200"
            }
          }
        }
    """.trimIndent()

    private fun getIncomeReportJson(year: Int) = """
        {
          "name": "$year Income",
          "time": "$year-02-19T13:55:15Z",
          "account_total": {
            "name": "Income",
            "fullName": "Income",
            "children": [
              {
                "name": "Salary",
                "fullName": "Income:Salary",
                "children": [],
                "monthAmounts": {
                  "1": "100",
                  "2": "100",
                  "3": "100",
                  "4": "100",
                  "5": "100",
                  "6": "100",
                  "7": "100",
                  "8": "100",
                  "9": "100",
                  "10": "100",
                  "11": "100",
                  "12": "100"
                },
                "total": "1200"
              }
            ],
            "monthAmounts": {
              "1": "100",
              "2": "100",
              "3": "100",
              "4": "100",
              "5": "100",
              "6": "100",
              "7": "100",
              "8": "100",
              "9": "100",
              "10": "100",
              "11": "100",
              "12": "100"
            },
            "total": "1200"
          }
        }
    """.trimIndent()
}
