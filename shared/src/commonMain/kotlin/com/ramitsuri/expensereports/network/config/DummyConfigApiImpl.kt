package com.ramitsuri.expensereports.network.config

import com.ramitsuri.expensereports.network.ConfigDto
import com.ramitsuri.expensereports.network.NetworkResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal class DummyConfigApiImpl(private val json: Json) : ConfigApi {

    override suspend fun get(): NetworkResponse<ConfigDto> {
        val dto = json.decodeFromString<ConfigDto>(getJson())
        return NetworkResponse.Success(dto)
    }

    private fun getJson(): String {
        return """
            {
              "ignored_expense_accounts": [],
              "main_asset_accounts": [
                "Assets:Current Assets:Checking",
                "Assets:Current Assets:Savings",
                "Assets:Investments:Retirement:401K"
              ],
              "main_liability_accounts": [
                "Liabilities:CreditCard:CreditCard1"
              ],
              "main_income_accounts": [
                "Income:Salary"
              ],
              "annual_budget": "12000",
              "annual_savings_target": "12000"
            }
        """.trimIndent()
    }
}