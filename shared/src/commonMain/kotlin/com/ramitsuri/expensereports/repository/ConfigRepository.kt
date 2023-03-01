package com.ramitsuri.expensereports.repository

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.Config
import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.network.ConfigApi
import com.ramitsuri.expensereports.network.NetworkResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConfigRepository(
    private val configApi: ConfigApi,
    private val json: Json,
    private val prefManager: PrefManager
) {

    suspend fun getIgnoredExpenseAccounts(): List<String> {
        return get()?.ignoredExpenseAccounts ?: listOf()
    }

    suspend fun getAssetAccounts(): List<String> {
        return get()?.mainAssetAccounts ?: listOf()
    }

    suspend fun getLiabilityAccounts(): List<String> {
        return get()?.mainLiabilityAccounts ?: listOf()
    }

    suspend fun getIncomeAccounts(): List<String> {
        return get()?.mainIncomeAccounts ?: listOf()
    }

    suspend fun getAnnualBudget(): BigDecimal {
        return get()?.annualBudget ?: BigDecimal.ZERO
    }

    suspend fun getAnnualSavingsTarget(): BigDecimal {
        return get()?.annualSavingsTarget ?: BigDecimal.ZERO
    }

    private suspend fun get(): Config? {
        val fromPrefs = prefManager.getConfigJson()
        return if (fromPrefs != null) {
            try {
                json.decodeFromString<Config>(fromPrefs)
            } catch (e: Exception) {
                null
            }
        } else {
            return downloadAndSave()
        }
    }

    suspend fun downloadAndSave(): Config? {
        return when (val result = configApi.get()) {
            is NetworkResponse.Failure -> {
                null
            }
            is NetworkResponse.Success -> {
                val config = Config(result.data)
                prefManager.setConfigJson(json.encodeToString(config))
                config
            }
        }
    }
}