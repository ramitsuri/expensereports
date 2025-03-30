package com.ramitsuri.expensereports.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class Settings internal constructor(
    private val keyValueStore: KeyValueStore,
) {
    suspend fun getLastTxFetchTime(): Instant {
        return keyValueStore
            .getString(Key.LAST_TX_FETCH_TIME, null)
            .let {
                if (it == null) {
                    Instant.DISTANT_PAST
                } else {
                    Instant.parse(it)
                }
            }
    }

    suspend fun setLastTxFetchTime(time: Instant) {
        keyValueStore.putString(Key.LAST_TX_FETCH_TIME, time.toString())
    }

    suspend fun setLastReportsFetchTime(time: Instant) {
        keyValueStore.putString(Key.LAST_REPORTS_FETCH_TIME, time.toString())
    }

    suspend fun getLastReportsFetchTime(): Instant {
        return keyValueStore
            .getString(Key.LAST_REPORTS_FETCH_TIME, null)
            .let {
                if (it == null) {
                    Instant.DISTANT_PAST
                } else {
                    Instant.parse(it)
                }
            }
    }

    suspend fun getLastCurrentBalancesFetchTime(): Instant {
        return keyValueStore
            .getString(Key.LAST_CURRENT_BALANCES_FETCH_TIME, null)
            .let {
                if (it == null) {
                    Instant.DISTANT_PAST
                } else {
                    Instant.parse(it)
                }
            }
    }

    suspend fun setLastCurrentBalancesFetchTime(time: Instant) {
        keyValueStore.putString(Key.LAST_CURRENT_BALANCES_FETCH_TIME, time.toString())
    }

    suspend fun getLastFullFetchTime(): Instant {
        return getLastFullFetchTimeFlow().first() ?: Instant.DISTANT_PAST
    }

     fun getLastFullFetchTimeFlow(): Flow<Instant?> {
        return keyValueStore
            .getStringFlow(Key.LAST_FULL_FETCH_TIME, null)
            .map {
                if (it == null) {
                    null
                } else {
                    Instant.parse(it)
                }
            }
    }

    suspend fun setLastFullFetchTime(time: Instant) {
        keyValueStore.putString(Key.LAST_FULL_FETCH_TIME, time.toString())
    }

     suspend fun getLastFetchTime(): Instant {
        return getLastFetchTimeFlow().first() ?: Instant.DISTANT_PAST
    }

     fun getLastFetchTimeFlow(): Flow<Instant?> {
        return keyValueStore
            .getStringFlow(Key.LAST_FETCH_TIME, null)
            .map {
                if (it == null) {
                   null
                } else {
                    Instant.parse(it)
                }
            }
    }

    suspend fun setLastFetchTime(time: Instant) {
        keyValueStore.putString(Key.LAST_FETCH_TIME, time.toString())
    }

    suspend fun getBaseUrl(): String {
        return getBaseUrlFlow().first()
    }

    fun getBaseUrlFlow(): Flow<String> {
        return keyValueStore
            .getStringFlow(Key.BASE_URL, "")
            .map { it ?: "" }
    }

    suspend fun setBaseUrl(baseUrl: String) {
        keyValueStore.putString(Key.BASE_URL, baseUrl)
    }
}
