package com.ramitsuri.expensereports.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Store(private val dataStore: DataStore<Preferences>) {

    private val miscellaneousKey = stringPreferencesKey("miscellaneous")

    suspend fun setMiscellaneousJson(json: String) {
        dataStore.edit {
            it[miscellaneousKey] = json
        }
    }

    fun getMiscellaneousJson(): Flow<String?> {
        return dataStore.data.map {
            it[miscellaneousKey]
        }
    }
}