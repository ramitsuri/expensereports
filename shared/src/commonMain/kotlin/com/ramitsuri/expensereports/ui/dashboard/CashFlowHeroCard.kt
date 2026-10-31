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
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.ui.theme.greenColor
import com.ramitsuri.expensereports.ui.theme.redColor
import com.ramitsuri.expensereports.utils.formatCurrency
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.after_tax_income_label
import expensereports.shared.generated.resources.living_expenses_label
import expensereports.shared.generated.resources.monthly_cash_flow
import expensereports.shared.generated.resources.savings_rate_history
import expensereports.shared.generated.resources.tap_bar_for_details
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun CashFlowHeroCard(
    summary: CashFlowSummary,
    modifier: Modifier = Modifier,
) {
    var selectedPoint by remember { mutableStateOf<TrendPoint?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.monthly_cash_flow),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (summary.savingsRatePercent != null) {
                    Box(
                        modifier =
                            Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "${summary.savingsRatePercent.roundToInt()}% Savings YTD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary: After-tax net cash flow delta
            val isPositive = summary.netDelta >= 0
            val deltaColor = if (isPositive) greenColor else redColor
            Text(
                text = summary.netDelta.formatCurrency(showSign = true),
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFeatureSettings = "tnum",
                        fontWeight = FontWeight.Bold,
                    ),
                color = deltaColor,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Context: After-tax income vs. Living expenses breakdown
            val incomeLabel = stringResource(Res.string.after_tax_income_label)
            val expensesLabel = stringResource(Res.string.living_expenses_label)
            val effectiveIncome =
                if (summary.afterTaxIncome != 0.0) {
                    summary.afterTaxIncome
                } else {
                    summary.incomeWithoutGains
                }
            val formattedIncome = effectiveIncome.formatCurrency(showSign = true)
            val formattedExpenses = summary.livingExpenses.formatCurrency()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "$incomeLabel: $formattedIncome",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$expensesLabel: $formattedExpenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Savings rate history header + inspection details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.savings_rate_history),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (selectedPoint != null) {
                    val rate = selectedPoint!!.value
                    val formattedRate = "${((rate * 10).roundToInt() / 10.0)}%"
                    val rateColor = if (rate >= 0) greenColor else redColor
                    Text(
                        text = "${selectedPoint!!.fullLabel}: $formattedRate",
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold,
                            ),
                        color = rateColor,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.tap_bar_for_details),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontally scrollable savings rate bar chart (up to 3 years)
            SparklineCanvas(
                points = summary.trailingSavingsRates,
                selectedPoint = selectedPoint,
                onPointSelected = { selectedPoint = it },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
            )
        }
    }
}
