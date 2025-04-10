package com.ramitsuri.expensereports.ui.settings

import kotlinx.datetime.Instant

data class SettingsViewState(
    val url: String = "",
    val times: Times = Times(),
) {
    data class Times(
        val lastFetchTime: Instant? = null,
        val lastFullFetchTime: Instant? = null,
        val fileLastModifiedTime: Instant? = null,
        val lastRunTime: Instant? = null,
    )
}
