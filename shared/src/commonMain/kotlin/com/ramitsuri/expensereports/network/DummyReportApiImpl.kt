package com.ramitsuri.expensereports.network

internal class DummyReportApiImpl : ReportApi {
    override suspend fun getExpenseReport(year: Int): NetworkResponse<ExpenseReportDto> {
        val expenseReportDto = ExpenseReportDto(
            name = "Dummy Report $year",
            accountTotals = listOf(
                AccountTotalDto(
                    name = "Rent",
                    fullName = "Rent",
                    children = listOf(),
                    balances = listOf(
                        BalanceDto(
                            month = 1,
                            amount = "100"
                        ),
                        BalanceDto(
                            month = 2,
                            amount = "50"
                        ),
                        BalanceDto(
                            month = 3,
                            amount = "150"
                        ),
                        BalanceDto(
                            month = 4,
                            amount = "34"
                        ),
                        BalanceDto(
                            month = 5,
                            amount = "123"
                        ),
                        BalanceDto(
                            month = 6,
                            amount = "67"
                        ),
                        BalanceDto(
                            month = 7,
                            amount = "1"
                        ),
                        BalanceDto(
                            month = 8,
                            amount = "89"
                        ),
                        BalanceDto(
                            month = 9,
                            amount = "56"
                        ),
                        BalanceDto(
                            month = 10,
                            amount = "9"
                        ),
                        BalanceDto(
                            month = 11,
                            amount = "23"
                        ),
                        BalanceDto(
                            month = 12,
                            amount = "11"
                        )
                    ),
                    total = "0"
                ),
                AccountTotalDto(
                    name = "Groceries",
                    fullName = "Groceries",
                    children = listOf(),
                    balances = listOf(
                        BalanceDto(
                            month = 1,
                            amount = "50"
                        ),
                        BalanceDto(
                            month = 2,
                            amount = "25"
                        ),
                        BalanceDto(
                            month = 3,
                            amount = "75"
                        ),
                        BalanceDto(
                            month = 4,
                            amount = "17"
                        ),
                        BalanceDto(
                            month = 5,
                            amount = "60"
                        ),
                        BalanceDto(
                            month = 6,
                            amount = "34"
                        ),
                        BalanceDto(
                            month = 7,
                            amount = "2"
                        ),
                        BalanceDto(
                            month = 8,
                            amount = "40"
                        ),
                        BalanceDto(
                            month = 9,
                            amount = "23"
                        ),
                        BalanceDto(
                            month = 10,
                            amount = "3"
                        ),
                        BalanceDto(
                            month = 11,
                            amount = "13"
                        ),
                        BalanceDto(
                            month = 12,
                            amount = "17"
                        )
                    ),
                    total = "0"
                ),
                AccountTotalDto(
                    name = "Food",
                    fullName = "Food",
                    children = listOf(),
                    balances = listOf(
                        BalanceDto(
                            month = 1,
                            amount = "34"
                        ),
                        BalanceDto(
                            month = 2,
                            amount = "12"
                        ),
                        BalanceDto(
                            month = 3,
                            amount = "34"
                        ),
                        BalanceDto(
                            month = 4,
                            amount = "89"
                        ),
                        BalanceDto(
                            month = 5,
                            amount = "65"
                        ),
                        BalanceDto(
                            month = 6,
                            amount = "12"
                        ),
                        BalanceDto(
                            month = 7,
                            amount = "72"
                        ),
                        BalanceDto(
                            month = 8,
                            amount = "43"
                        ),
                        BalanceDto(
                            month = 9,
                            amount = "73"
                        ),
                        BalanceDto(
                            month = 10,
                            amount = "38"
                        ),
                        BalanceDto(
                            month = 11,
                            amount = "90"
                        ),
                        BalanceDto(
                            month = 12,
                            amount = "98"
                        )
                    ),
                    total = "0"
                ),
                AccountTotalDto(
                    name = "Travel",
                    fullName = "Travel",
                    children = listOf(),
                    balances = listOf(
                        BalanceDto(
                            month = 1,
                            amount = "24"
                        ),
                        BalanceDto(
                            month = 2,
                            amount = "10"
                        ),
                        BalanceDto(
                            month = 3,
                            amount = "67"
                        ),
                        BalanceDto(
                            month = 4,
                            amount = "0"
                        ),
                        BalanceDto(
                            month = 5,
                            amount = "0"
                        ),
                        BalanceDto(
                            month = 6,
                            amount = "15"
                        ),
                        BalanceDto(
                            month = 7,
                            amount = "90"
                        ),
                        BalanceDto(
                            month = 8,
                            amount = "0"
                        ),
                        BalanceDto(
                            month = 9,
                            amount = "1"
                        ),
                        BalanceDto(
                            month = 10,
                            amount = "12"
                        ),
                        BalanceDto(
                            month = 11,
                            amount = "1"
                        ),
                        BalanceDto(
                            month = 12,
                            amount = "8"
                        )
                    ),
                    total = "0"
                )
            )
        )
        return NetworkResponse.Success(expenseReportDto)
    }
}