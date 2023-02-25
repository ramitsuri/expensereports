package com.ramitsuri.expensereports.data.prefs

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

    fun setAssetAccounts(assetAccounts: List<String>) {
        val key = Key.ASSET_ACCOUNTS
        putStringList(key, assetAccounts)
    }

    fun getAssetAccounts(): List<String> {
        val key = Key.ASSET_ACCOUNTS
        return getStringList(key, listOf())
    }

    fun setLiabilityAccounts(liabilityAccounts: List<String>) {
        val key = Key.LIABILITY_ACCOUNTS
        putStringList(key, liabilityAccounts)
    }

    fun getLiabilityAccounts(): List<String> {
        val key = Key.LIABILITY_ACCOUNTS
        return getStringList(key, listOf())
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

            ASSET_ACCOUNTS(
                key = "asset_accounts",
                isSecure = false
            ),

            LIABILITY_ACCOUNTS(
                key = "liability_accounts",
                isSecure = false
            )
        }
    }
}