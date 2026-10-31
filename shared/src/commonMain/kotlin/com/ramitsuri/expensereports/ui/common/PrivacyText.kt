package com.ramitsuri.expensereports.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.ramitsuri.expensereports.utils.formatCurrency

@Composable
fun PrivacyText(
    amount: Double,
    isMasked: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified,
    maskPlaceholder: String = "$ • • • , • • •",
    showSign: Boolean = false,
) {
    Text(
        text = if (isMasked) maskPlaceholder else amount.formatCurrency(showSign = showSign),
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        modifier = modifier,
    )
}
