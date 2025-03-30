package com.ramitsuri.expensereports.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(
    dynamicDarkColorScheme: ColorScheme? = null,
    dynamicLightColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (isDarkMode()) {
            dynamicDarkColorScheme ?: darkColorScheme
        } else {
            dynamicLightColorScheme ?: lightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
private val greenDark = Color(0xFF64a880)
private val redDark = Color(0xFFFD9891)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
private val greenLight = Color(0xFF578a4f)
private val redLight = Color(0xFFC9372C)

private val darkColorScheme =
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
    )

private val lightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )

val greenColor: Color
    @Composable
    get() = if (isDarkMode()) greenDark else greenLight

val redColor: Color
    @Composable
    get() = if (isDarkMode()) redDark else redLight