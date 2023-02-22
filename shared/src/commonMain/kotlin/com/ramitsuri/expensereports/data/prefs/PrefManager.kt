package com.ramitsuri.expensereports.data.prefs

import kotlinx.datetime.Instant

class PrefManager(private val keyValueStore: KeyValueStore) {

    fun setServerUrl(server: String) {
        val key = Key.SERVER_URL
        putString(key, server)
    }

    fun getServerUrl(): String {
        val key = Key.SERVER_URL
        return getString(key, "") ?: ""
    }

    fun setIgnoredExpenseAccounts(ignoredAccounts: List<String>) {
        val key = Key.IGNORED_EXPENSE_ACCOUNTS
        putStringList(key, ignoredAccounts)
    }

    fun getIgnoredExpenseAccounts(): List<String> {
        val key = Key.IGNORED_EXPENSE_ACCOUNTS
        return getStringList(key, listOf())
    }

    fun setExpenseReportFetchTimestamp(fetchTimestamp: Instant) {
        val key = Key.EXPENSE_REPORT_FETCH_TIMESTAMP
        putString(key, fetchTimestamp.toString())
    }

    fun getExpenseReportFetchTimestamp(): Instant {
        val key = Key.EXPENSE_REPORT_FETCH_TIMESTAMP
        val timestamp = getString(key, null) ?: return Instant.DISTANT_PAST
        return try {
            Instant.parse(timestamp)
        } catch (e: Exception) {
            Instant.DISTANT_PAST
        }
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

    companion object {
        private const val KV = "KV"
        private const val SKV = "SKV"

        private enum class Key(val key: String, val isSecure: Boolean) {
            SERVER_URL(
                key = "server_url",
                isSecure = false
            ),

            IGNORED_EXPENSE_ACCOUNTS(
                key = "ignored_expense_accounts",
                isSecure = false
            ),

            EXPENSE_REPORT_FETCH_TIMESTAMP(
                key = "last_expense_report_fetch_timestamp",
                isSecure = false
            )
        }
    }
}