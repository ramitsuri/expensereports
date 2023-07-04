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