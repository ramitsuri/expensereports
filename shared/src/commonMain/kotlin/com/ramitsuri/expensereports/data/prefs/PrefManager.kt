package com.ramitsuri.expensereports.data.prefs

import com.ramitsuri.expensereports.data.TransactionGroup
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class PrefManager(
    private val keyValueStore: KeyValueStore,
    private val json: Json,
) {

    init {
        removeLegacyPrefs()
    }

    fun setServerUrl(server: String) {
        val key = Key.SERVER_URL
        putString(key, server)
    }

    fun getServerUrl(): String {
        val key = Key.SERVER_URL
        return getString(key, "") ?: ""
    }

    fun setLastDownloadTime(time: Instant) {
        val key = Key.LAST_DOWNLOAD_TIME
        putString(key, time.toString())
    }

    fun getLastDownloadTime(): Instant? {
        val key = Key.LAST_DOWNLOAD_TIME
        val timestamp = getString(key, null) ?: return null
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            null
        }
    }

    fun setDownloadRecentData(downloadRecentData: Boolean) {
        val key = Key.DOWNLOAD_RECENT_DATA
        putBoolean(key, downloadRecentData)
    }

    fun shouldDownloadRecentData(): Boolean {
        val key = Key.DOWNLOAD_RECENT_DATA
        return getBoolean(key, true)
    }

    fun setTransactionGroups(transactionGroups: List<TransactionGroup>) {
        val transactionGroupsJson =
            json.encodeToString(ListSerializer(TransactionGroup.serializer()), transactionGroups)
        putString(Key.TRANSACTION_GROUPS, transactionGroupsJson)
    }

    fun getTransactionGroups(): List<TransactionGroup> {
        val transactionGroupsJson = getString(Key.TRANSACTION_GROUPS, null) ?: return listOf()
        return json.decodeFromString(transactionGroupsJson)
    }

    private fun putString(key: Key, value: String) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putString(key.key, value)
    }

    private fun getString(key: Key, default: String?): String? {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getString(key.key, default)
    }

    private fun putBoolean(key: Key, value: Boolean) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putBoolean(key.key, value)
    }

    private fun getBoolean(key: Key, default: Boolean): Boolean {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getBoolean(key.key, default)
    }

    private fun putInt(key: Key, value: Int) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putInt(key.key, value)
    }

    private fun getInt(key: Key, default: Int): Int {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getInt(key.key, default)
    }

    private fun putLong(key: Key, value: Long) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putLong(key.key, value)
    }

    private fun getLong(key: Key, default: Long): Long {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getLong(key.key, default)
    }

    private fun putStringList(key: Key, value: List<String>) {
        val keyValueStore = getKeyValueStore(key)
        keyValueStore.putStringList(key.key, value)
    }

    private fun getStringList(key: Key, default: List<String>): List<String> {
        val keyValueStore = getKeyValueStore(key)
        return keyValueStore.getStringList(key.key, default)
    }

    private fun getKeyValueStore(key: Key): KeyValueStore {
        return if (key.isSecure) {
            keyValueStore
        } else {
            keyValueStore
        }
    }

    private fun removeLegacyPrefs() {
        val legacyPrefs = Key.values().filter { it.isLegacy }
        legacyPrefs.forEach { key ->
            getKeyValueStore(key).remove(key.key)
        }
    }

    companion object {
        private enum class Key(
            val key: String,
            val isSecure: Boolean = false,
            val isLegacy: Boolean = false
        ) {
            SERVER_URL(
                key = "server_url"
            ),

            LAST_DOWNLOAD_TIME(
                key = "last_download_time"
            ),

            DOWNLOAD_RECENT_DATA(
                key = "download_recent_data"
            ),

            TRANSACTION_GROUPS(
                key = "transaction_groups",
            ),

            // Legacy - no longer used and should be removed
            TRANSACTION_GROUP_NAME(
                key = "transaction_group_name",
                isLegacy = true,
            ),

            TRANSACTION_GROUP_TOTAL(
                key = "transaction_group_total",
                isLegacy = true,
            ),

            CONFIG(
                key = "config",
                isLegacy = true
            ),

            MISCELLANEOUS(
                key = "miscellaneous",
                isLegacy = true
            )
        }
    }
}