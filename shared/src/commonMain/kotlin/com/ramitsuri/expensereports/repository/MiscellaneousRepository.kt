package com.ramitsuri.expensereports.repository

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountBalance
import com.ramitsuri.expensereports.data.Miscellaneous
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.miscellaneous.MiscellaneousApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MiscellaneousRepository(
    private val miscellaneousApi: MiscellaneousApi,
    private val json: Json,
    private val prefManager: PrefManager
) {

    suspend fun getIncomeTotal(): BigDecimal {
        return get()?.incomeTotal ?: BigDecimal.ZERO
    }

    suspend fun getExpensesTotal(): BigDecimal {
        return get()?.expensesTotal ?: BigDecimal.ZERO
    }

    suspend fun getExpensesAfterDeductionsTotal(): BigDecimal {
        return get()?.expensesAfterDeductionTotal ?: BigDecimal.ZERO
    }

    suspend fun getSavingsTotal(): BigDecimal {
        return get()?.savingsTotal ?: BigDecimal.ZERO
    }

    suspend fun getAccountBalances(): List<AccountBalance> {
        return get()?.accountBalances ?: listOf()
    }

    private suspend fun get(): Miscellaneous? {
        val fromPrefs = prefManager.getMiscellaneousJson()
        return if (fromPrefs != null) {
            try {
                json.decodeFromString<Miscellaneous>(fromPrefs)
            } catch (e: Exception) {
                null
            }
        } else {
            return downloadAndSave()
        }
    }

    suspend fun downloadAndSave(): Miscellaneous? {
        return when (val result = miscellaneousApi.get()) {
            is NetworkResponse.Failure -> {
                null
            }

            is NetworkResponse.Success -> {
                val miscellaneous = Miscellaneous(result.data.miscellaneous)
                prefManager.setMiscellaneousJson(json.encodeToString(miscellaneous))
                miscellaneous
            }
        }
    }
}