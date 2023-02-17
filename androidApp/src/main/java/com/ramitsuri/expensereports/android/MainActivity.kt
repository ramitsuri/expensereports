package com.ramitsuri.expensereports.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.ramitsuri.expensereports.android.ui.AppTheme
import com.ramitsuri.expensereports.android.ui.NavGraph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme(theme) {
                NavGraph()
            }
        }
    }
}