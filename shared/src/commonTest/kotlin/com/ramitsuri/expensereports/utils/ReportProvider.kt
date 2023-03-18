package com.ramitsuri.expensereports.utils

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.data.Report
import com.ramitsuri.expensereports.data.ReportType
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun getAssetsReport() = Report(
    name = "Assets",
    generatedAt = Clock.System.now(),
    fetchedAt = Clock.System.now(),
    accountTotal = serializer.decodeFromString(assetsAccountTotal),
    type = ReportType.ASSETS,
    year = 2023
)

private val serializer = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

/**
 * | Name              | Month 1 | Month 2 | Month 3 | Total     |
 * |-------------------|---------|---------|---------|-----------|
 * | Assets            | 736     | 1033    | 1379    | 3148      |
 * | +-Investments     | 486     | 653     | 879     | 2018      |
 * | +--Retirement     | 486     | 653     | 879     | 2018      |
 * | +---401K          | 432     | 545     | 754     | 1731      |
 * | +----Contribution | 400     | 500     | 700     | 1600      |
 * | +----Gains        | 12      | 15      | 14      | 41        |
 * | +----Employer     | 20      | 30      | 40      | 90        |
 * | +---Roth IRA      | 54      | 108     | 125     | 287       |
 * | +----Contribution | 50      | 100     | 120     | 270       |
 * | +----Gains        | 4       | 8       | 5       | 17        |
 * | +-Current Assets  | 150     | 180     | 200     | 530       |
 * | +--Checking       | 50      | 60      | 70      | 180       |
 * | +--Savings        | 100     | 120     | 130     | 350       |
 * | +-Fixed Assets    | 100     | 200     | 300     | 600       |
 */
private const val assetsAccountTotal = """{
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
              "children": [
                {
                  "name": "Contribution",
                  "fullName": "Assets:Investments:Retirement:401K:Contribution",
                  "children": [],
                  "monthAmounts": {
                    "1": "400",
                    "2": "500",
                    "3": "700",
                    "4": "0.0",
                    "5": "0.0",
                    "6": "0.0",
                    "7": "0.0",
                    "8": "0.0",
                    "9": "0.0",
                    "10": "0.0",
                    "11": "0.0",
                    "12": "0.0"
                  },
                  "total": "0"
                },
                {
                  "name": "Gains",
                  "fullName": "Assets:Investments:Retirement:401K:Gains",
                  "children": [],
                  "monthAmounts": {
                    "1": "12",
                    "2": "15",
                    "3": "14",
                    "4": "0.0",
                    "5": "0.0",
                    "6": "0.0",
                    "7": "0.0",
                    "8": "0.0",
                    "9": "0.0",
                    "10": "0.0",
                    "11": "0.0",
                    "12": "0.0"
                  },
                  "total": "0"
                },
                {
                  "name": "Employer",
                  "fullName": "Assets:Investments:Retirement:401K:Employer",
                  "children": [],
                  "monthAmounts": {
                    "1": "20",
                    "2": "30",
                    "3": "40",
                    "4": "0.0",
                    "5": "0.0",
                    "6": "0.0",
                    "7": "0.0",
                    "8": "0.0",
                    "9": "0.0",
                    "10": "0.0",
                    "11": "0.0",
                    "12": "0.0"
                  },
                  "total": "0"
                }
              ],
              "monthAmounts": {
                "1": "0",
                "2": "0",
                "3": "0",
                "4": "0.0",
                "5": "0.0",
                "6": "0.0",
                "7": "0.0",
                "8": "0.0",
                "9": "0.0",
                "10": "0.0",
                "11": "0.0",
                "12": "0.0"
              },
              "total": "0"
            },
            {
              "name": "Roth IRA",
              "fullName": "Assets:Investments:Retirement:Roth IRA",
              "children": [
                {
                  "name": "Contribution",
                  "fullName": "Assets:Investments:Retirement:Roth IRA:Contribution",
                  "children": [],
                  "monthAmounts": {
                    "1": "50",
                    "2": "100",
                    "3": "120",
                    "4": "0.0",
                    "5": "0.0",
                    "6": "0.0",
                    "7": "0.0",
                    "8": "0.0",
                    "9": "0.0",
                    "10": "0.0",
                    "11": "0.0",
                    "12": "0.0"
                  },
                  "total": "0"
                },
                {
                  "name": "Gains",
                  "fullName": "Assets:Investments:Retirement:Roth IRA:Gains",
                  "children": [],
                  "monthAmounts": {
                    "1": "4",
                    "2": "8",
                    "3": "5",
                    "4": "0.0",
                    "5": "0.0",
                    "6": "0.0",
                    "7": "0.0",
                    "8": "0.0",
                    "9": "0.0",
                    "10": "0.0",
                    "11": "0.0",
                    "12": "0.0"
                  },
                  "total": "0"
                }
              ],
              "monthAmounts": {
                "1": "0",
                "2": "0",
                "3": "0",
                "4": "0.0",
                "5": "0.0",
                "6": "0.0",
                "7": "0.0",
                "8": "0.0",
                "9": "0.0",
                "10": "0.0",
                "11": "0.0",
                "12": "0.0"
              },
              "total": "0"
            }
          ],
          "monthAmounts": {
            "1": "0",
            "2": "0",
            "3": "0",
            "4": "0.0",
            "5": "0.0",
            "6": "0.0",
            "7": "0.0",
            "8": "0.0",
            "9": "0.0",
            "10": "0.0",
            "11": "0.0",
            "12": "0.0"
          },
          "total": "0"
        }
      ],
      "monthAmounts": {
        "1": "0",
        "2": "0",
        "3": "0",
        "4": "0.0",
        "5": "0.0",
        "6": "0.0",
        "7": "0.0",
        "8": "0.0",
        "9": "0.0",
        "10": "0.0",
        "11": "0.0",
        "12": "0.0"
      },
      "total": "0"
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
            "1": "50",
            "2": "60",
            "3": "70",
            "4": "0.0",
            "5": "0.0",
            "6": "0.0",
            "7": "0.0",
            "8": "0.0",
            "9": "0.0",
            "10": "0.0",
            "11": "0.0",
            "12": "0.0"
          },
          "total": "0"
        },
        {
          "name": "Savings ",
          "fullName": "Assets:Current Assets:Savings ",
          "children": [],
          "monthAmounts": {
            "1": "100",
            "2": "120",
            "3": "130",
            "4": "0.0",
            "5": "0.0",
            "6": "0.0",
            "7": "0.0",
            "8": "0.0",
            "9": "0.0",
            "10": "0.0",
            "11": "0.0",
            "12": "0.0"
          },
          "total": "5.0"
        }
      ],
      "monthAmounts": {
        "1": "0",
        "2": "0",
        "3": "0",
        "4": "0.0",
        "5": "0.0",
        "6": "0.0",
        "7": "0.0",
        "8": "0.0",
        "9": "0.0",
        "10": "0.0",
        "11": "0.0",
        "12": "0.0"
      },
      "total": "0"
    },
    {
      "name": "Fixed Assets",
      "fullName": "Assets:Fixed Assets",
      "children": [],
      "monthAmounts": {
        "1": "100",
        "2": "200",
        "3": "300",
        "4": "0.0",
        "5": "0.0",
        "6": "0.0",
        "7": "0.0",
        "8": "0.0",
        "9": "0.0",
        "10": "0.0",
        "11": "0.0",
        "12": "0.0"
      },
      "total": "300"
    }
  ],
  "monthAmounts": {
    "1": "0",
    "2": "0",
    "3": "0",
    "4": "0.0",
    "5": "0.0",
    "6": "0.0",
    "7": "0.0",
    "8": "0.0",
    "9": "0.0",
    "10": "0.0",
    "11": "0.0",
    "12": "0.0"
  },
  "total": "0"
}"""


/**
 * Returns a report that looks like
 *
 *     |   Name    | Month 1 | Month 2 | Month 3 | Month 4 |
 *     |-----------|---------|---------|---------|---------|
 *     | Account 1 |    1    |    2    |    3    |    4    |
 *     | Account 2 |    7    |    8    |    9    |   10    |
 *     | Account 3 |   14    |   15    |   16    |   17    |
 */
fun getSimpleReport(): Report {
    return Report(
        name = "Report",
        generatedAt = Clock.System.now(),
        fetchedAt = Clock.System.now(),
        accountTotal = AccountTotal(
            name = "Expenses",
            fullName = "Expenses",
            children = listOf(
                AccountTotal(
                    name = "Account1",
                    fullName = "Account1",
                    children = listOf(),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("1"),
                        2 to BigDecimal.parseString("2"),
                        3 to BigDecimal.parseString("3"),
                        4 to BigDecimal.parseString("4")
                    )
                ),
                AccountTotal(
                    name = "Account2",
                    fullName = "Account2",
                    children = listOf(),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("7"),
                        2 to BigDecimal.parseString("8"),
                        3 to BigDecimal.parseString("9"),
                        4 to BigDecimal.parseString("10")

                    )
                ),
                AccountTotal(
                    name = "Account3",
                    fullName = "Account3",
                    children = listOf(),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("14"),
                        2 to BigDecimal.parseString("15"),
                        3 to BigDecimal.parseString("16"),
                        4 to BigDecimal.parseString("17")
                    )
                )
            ),
            monthAmounts = mapOf<Int, BigDecimal>(),
            total = BigDecimal.ZERO
        ),
        type = ReportType.EXPENSE,
        year = 2023
    )
}

/**
 * Returns a report that looks like
 *
 *     |     Name      | Month 1 | Month 2 | Month 3 | Month 4 |
 *     |---------------|---------|---------|---------|---------|
 *     |   Account 1   |    6    |   11    |   16    |   45    |
 *     |  - Account 11 |    4    |    5    |    8    |   33    |
 *     |  - Account 12 |    2    |    6    |    8    |   12    |
 *     |  Account 2    |    7    |    8    |    9    |   10    |
 *     |  - Account 21 |    7    |    8    |    9    |   10    |
 *     |  Account 3    |   27    |   31    |   40    |   29    |
 *     |  - Account 31 |   12    |    8    |   12    |   10    |
 *     |  - Account 32 |   15    |   23    |   28    |   19    |
 */
fun getComplexReport(): Report {
    return Report(
        name = "Report",
        generatedAt = Clock.System.now(),
        fetchedAt = Clock.System.now(),
        accountTotal = AccountTotal(
            name = "Expenses",
            fullName = "Expenses",
            children = listOf(
                AccountTotal(
                    name = "Account1",
                    fullName = "Account1",
                    children = listOf(
                        AccountTotal(
                            name = "Account11",
                            fullName = "Account11",
                            children = listOf(),
                            monthAmounts = mapOf(
                                1 to BigDecimal.parseString("4"),
                                2 to BigDecimal.parseString("5"),
                                3 to BigDecimal.parseString("8"),
                                4 to BigDecimal.parseString("33")
                            )
                        ), AccountTotal(
                            name = "Account12",
                            fullName = "Account12",
                            children = listOf(),
                            monthAmounts = mapOf(
                                1 to BigDecimal.parseString("2"),
                                2 to BigDecimal.parseString("6"),
                                3 to BigDecimal.parseString("8"),
                                4 to BigDecimal.parseString("12")
                            )
                        )
                    ),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("6"),
                        2 to BigDecimal.parseString("11"),
                        3 to BigDecimal.parseString("16"),
                        4 to BigDecimal.parseString("45")
                    )
                ),
                AccountTotal(
                    name = "Account2",
                    fullName = "Account2",
                    children = listOf(
                        AccountTotal(
                            name = "Account21",
                            fullName = "Account21",
                            children = listOf(),
                            monthAmounts = mapOf(
                                1 to BigDecimal.parseString("7"),
                                2 to BigDecimal.parseString("8"),
                                3 to BigDecimal.parseString("9"),
                                4 to BigDecimal.parseString("10")

                            )
                        )
                    ),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("7"),
                        2 to BigDecimal.parseString("8"),
                        3 to BigDecimal.parseString("9"),
                        4 to BigDecimal.parseString("10")

                    )
                ),
                AccountTotal(
                    name = "Account3",
                    fullName = "Account3",
                    children = listOf(
                        AccountTotal(
                            name = "Account31",
                            fullName = "Account31",
                            children = listOf(),
                            monthAmounts = mapOf(
                                1 to BigDecimal.parseString("12"),
                                2 to BigDecimal.parseString("8"),
                                3 to BigDecimal.parseString("12"),
                                4 to BigDecimal.parseString("10")
                            )
                        ), AccountTotal(
                            name = "Account32",
                            fullName = "Account32",
                            children = listOf(),
                            monthAmounts = mapOf(
                                1 to BigDecimal.parseString("15"),
                                2 to BigDecimal.parseString("23"),
                                3 to BigDecimal.parseString("28"),
                                4 to BigDecimal.parseString("19")
                            )
                        )
                    ),
                    monthAmounts = mapOf(
                        1 to BigDecimal.parseString("27"),
                        2 to BigDecimal.parseString("31"),
                        3 to BigDecimal.parseString("40"),
                        4 to BigDecimal.parseString("29")
                    )
                )
            ),
            monthAmounts = mapOf<Int, BigDecimal>(),
            total = BigDecimal.ZERO
        ),
        type = ReportType.EXPENSE,
        year = 2023
    )
}