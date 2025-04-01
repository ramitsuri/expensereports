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

    companion object {
        private const val DEEPLINK_BASE: String = "expense-reports://"
    }
}
