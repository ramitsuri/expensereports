package com.ramitsuri.expensereports.ui.settings

import kotlinx.datetime.Instant

data class SettingsViewState(
    val url: String = "",
    val lastFetchTime: Instant? = null,
    val lastFullFetchTime: Instant? = null,
)
