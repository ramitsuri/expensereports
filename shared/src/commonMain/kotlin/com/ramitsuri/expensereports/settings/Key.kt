package com.ramitsuri.expensereports.settings

enum class Key(val value: String) {
    LAST_TX_FETCH_TIME("last_tx_fetch_time"),
    LAST_CURRENT_BALANCES_FETCH_TIME("last_current_balances_fetch_time"),
    LAST_REPORTS_FETCH_TIME("last_reports_fetch_time"),
    TIME_ZONE("time_zone"),
    BASE_URL("base_url"),
    LAST_FULL_FETCH_TIME("last_full_fetch_time"),
}
