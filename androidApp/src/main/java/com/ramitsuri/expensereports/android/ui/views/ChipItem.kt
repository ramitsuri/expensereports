package com.ramitsuri.expensereports.android.ui.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewChip_Selected() {
    FilterChip(
        selected = true,
        onClick = { },
        label = { Text("Chip") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewChip_NotSelected() {
    FilterChip(
        selected = false,
        onClick = { },
        label = { Text("Chip") }
    )
}