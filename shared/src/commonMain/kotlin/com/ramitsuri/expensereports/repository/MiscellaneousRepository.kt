package com.ramitsuri.expensereports.repository

import com.ramitsuri.expensereports.data.Miscellaneous
import com.ramitsuri.expensereports.data.prefs.Store
import com.ramitsuri.expensereports.network.NetworkResponse
import com.ramitsuri.expensereports.network.miscellaneous.MiscellaneousApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MiscellaneousRepository(
    private val miscellaneousApi: MiscellaneousApi,
    private val json: Json,
    private val store: Store
) {

    fun get(): Flow<Miscellaneous?> {
        return store.getMiscellaneousJson().map { jsonString ->
            if (jsonString != null) {
                try {
                    json.decodeFromString<Miscellaneous>(jsonString)
                } catch (e: Exception) {
                    null
                }
            } else {
                downloadAndSave()
                null
            }
        }
    }

    suspend fun downloadAndSave(): Miscellaneous? {
        return when (val result = miscellaneousApi.get()) {
            is NetworkResponse.Failure -> {
                null
            }

            is NetworkResponse.Success -> {
                val miscellaneous = Miscellaneous(result.data.miscellaneous)
                store.setMiscellaneousJson(json.encodeToString(miscellaneous))
                miscellaneous
            }
        }
    }
}