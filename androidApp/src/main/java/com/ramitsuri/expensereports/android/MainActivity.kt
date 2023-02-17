package com.ramitsuri.expensereports.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ramitsuri.expensereports.android.expenses.ExpenseReportScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                ExpenseReportScreen()
            }
        }
    }
}