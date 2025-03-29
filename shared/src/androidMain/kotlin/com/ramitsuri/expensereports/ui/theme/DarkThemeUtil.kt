package com.ramitsuri.expensereports.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
actual fun isDarkMode(): Boolean {
    return isSystemInDarkTheme()
}
