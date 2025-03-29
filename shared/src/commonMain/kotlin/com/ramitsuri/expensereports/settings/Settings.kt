package com.ramitsuri.expensereports.settings

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

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

    suspend fun getTimeZone(): TimeZone {
        return keyValueStore
            .getString(Key.TIME_ZONE, null)
            ?.let {
                TimeZone.of(it)
            } ?: TimeZone.currentSystemDefault()
    }

    suspend fun getLastFullFetchTime(): Instant {
        return keyValueStore
            .getString(Key.LAST_FULL_FETCH_TIME, null)
            .let {
                if (it == null) {
                    Instant.DISTANT_PAST
                } else {
                    Instant.parse(it)
                }
            }
    }

    suspend fun setLastFullFetchTime(time: Instant) {
        keyValueStore.putString(Key.LAST_FULL_FETCH_TIME, time.toString())
    }

    suspend fun getBaseUrl(): String {
        return keyValueStore.getString(Key.BASE_URL, "") ?: ""
    }

    suspend fun setBaseUrl(baseUrl: String) {
        keyValueStore.putString(Key.BASE_URL, baseUrl)
    }
}
