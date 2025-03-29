package com.ramitsuri.expensereports.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    viewState: HomeViewState,
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(viewState.netWorths.joinToString())
        Text(viewState.savingsRates.joinToString())
        Text(viewState.currentBalanceGroups.joinToString())
    }
}