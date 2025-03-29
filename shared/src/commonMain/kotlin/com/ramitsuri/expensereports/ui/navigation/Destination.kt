package com.ramitsuri.expensereports.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Home : Destination

    @Serializable
    data object Settings : Destination
}
