package com.ramitsuri.expensereports.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.ui.common.PrivacyText
import com.ramitsuri.expensereports.ui.common.SparklineCanvas
import com.ramitsuri.expensereports.ui.theme.greenColor
import com.ramitsuri.expensereports.ui.theme.redColor
import com.ramitsuri.expensereports.utils.formatCurrency
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.net_worth
import expensereports.shared.generated.resources.vs_last_month
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun NetWorthHeroCard(
    summary: NetWorthSummary,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!summary.isAvailable) return

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Header: Icon + "Net Worth" on left, Privacy eye toggle on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.net_worth),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(
                    onClick = onToggleMask,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector =
                            if (isMasked) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                        contentDescription = if (isMasked) "Show sensitive data" else "Hide sensitive data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Metric: Current Net Worth
            PrivacyText(
                amount = summary.currentNetWorth,
                isMasked = isMasked,
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Month-over-month delta pill
            val isPositive = summary.previousMonthDelta >= 0
            val deltaColor = if (isPositive) greenColor else redColor
            val pillBgColor = deltaColor.copy(alpha = 0.12f)
            val deltaIcon =
                if (isPositive) {
                    Icons.AutoMirrored.Outlined.TrendingUp
                } else {
                    Icons.AutoMirrored.Outlined.TrendingDown
                }

            val formattedDeltaAmount =
                if (isMasked) {
                    "• • •"
                } else {
                    summary.previousMonthDelta.formatCurrency(showSign = true)
                }
            val formattedPercent =
                "${if (isPositive) "+" else ""}${((summary.previousMonthDeltaPercent * 10).roundToInt() / 10.0)}%"

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .background(pillBgColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = deltaIcon,
                            contentDescription = null,
                            tint = deltaColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "$formattedDeltaAmount ($formattedPercent)",
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFeatureSettings = "tnum",
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            color = deltaColor,
                        )
                    }
                }

                Text(
                    text = stringResource(Res.string.vs_last_month),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (summary.trailingHistory.size >= 2) {
                Spacer(modifier = Modifier.height(16.dp))

                SparklineCanvas(
                    points = summary.trailingHistory.map { it.value },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                )
            }
        }
    }
}
