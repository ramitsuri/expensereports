package com.ramitsuri.expensereports.settings

import com.ramitsuri.expensereports.model.RunInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

class Settings internal constructor(
    private val keyValueStore: KeyValueStore,
    private val json: Json,
) {
    suspend fun getLastTxFetchTime(): Instant {
        return keyValueStore
            .getString(Key.LAST_TX_FETCH_TIME, null)
            .let {
                if (it == null) {
                    distantPast
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
                    distantPast
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
                    distantPast
                } else {
                    Instant.parse(it)
                }
            }
    }

    suspend fun setLastCurrentBalancesFetchTime(time: Instant) {
        keyValueStore.putString(Key.LAST_CURRENT_BALANCES_FETCH_TIME, time.toString())
    }

    suspend fun getLastFullFetchTime(): Instant {
        return getLastFullFetchTimeFlow().first() ?: distantPast
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
        return getLastFetchTimeFlow().first() ?: distantPast
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

    suspend fun getLastMonthEndIncomeExpensesNotification(): Instant? {
        return keyValueStore
            .getString(Key.LAST_MONTH_END_INCOME_EXPENSES_NOTIFICATION, null)
            .let {
                if (it == null) {
                    null
                } else {
                    Instant.parse(it)
                }
            }
    }

    suspend fun setLastMonthEndIncomeExpensesNotification(time: Instant) {
        keyValueStore
            .putString(Key.LAST_MONTH_END_INCOME_EXPENSES_NOTIFICATION, time.toString())
    }

    suspend fun setRunInfo(runInfo: RunInfo) {
        keyValueStore
            .putString(Key.RUN_INFO, json.encodeToString(RunInfo.serializer(), runInfo))
    }

    fun getRunInfoFlow(): Flow<RunInfo?> {
        return keyValueStore
            .getStringFlow(Key.RUN_INFO, null)
            .map {
                if (it == null) {
                    null
                } else {
                    json.decodeFromString(RunInfo.serializer(), it)
                }
            }
    }

    private val distantPast = Instant.parse("2000-01-01T12:00:00Z")
}
