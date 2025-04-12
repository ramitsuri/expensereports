package com.ramitsuri.expensereports.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    val deepLinkUri: String

    @Serializable
    data object Home : Destination {
        override val deepLinkUri: String = DEEPLINK_BASE.plus("home")
    }

    @Serializable
    data object Settings : Destination {
        override val deepLinkUri: String = DEEPLINK_BASE.plus("settings")
    }

    @Serializable
    data object Report : Destination {
        override val deepLinkUri: String = DEEPLINK_BASE.plus("report")
    }

    companion object {
        private const val DEEPLINK_BASE: String = "expense-reports://"
    }
}
