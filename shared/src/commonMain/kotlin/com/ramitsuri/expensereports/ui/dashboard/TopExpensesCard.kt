package com.ramitsuri.expensereports.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.ui.theme.greenColor
import com.ramitsuri.expensereports.ui.theme.redColor
import com.ramitsuri.expensereports.utils.formatCurrency
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.top_spending_categories
import expensereports.shared.generated.resources.view_full_expenses
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TopExpensesCard(
    categories: List<CategorySpendItem>,
    onViewFullExpensesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            ) {
                Icon(
                    imageVector = Icons.Outlined.PieChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.top_spending_categories),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (categories.isEmpty()) {
                Text(
                    text = "No expense categories available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                categories.forEachIndexed { index, item ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = item.currentAmount.formatCurrency(),
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontFeatureSettings = "tnum",
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Trailing baseline delta & % of total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            val trailingAvg = item.trailingAverageAmount
                            if (trailingAvg != null && trailingAvg > 0) {
                                val delta = ((item.currentAmount - trailingAvg) / trailingAvg) * 100.0
                                val deltaRounded = abs(delta).roundToInt()
                                val (deltaText, deltaColor) =
                                    when {
                                        delta > 0 -> "↑ $deltaRounded% vs 3-mo avg" to redColor
                                        delta < 0 -> "↓ $deltaRounded% vs 3-mo avg" to greenColor
                                        else -> "~ 0% vs 3-mo avg" to MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                Text(
                                    text = deltaText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = deltaColor,
                                )
                            } else {
                                Text(
                                    text = "New category",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Text(
                                text = "${item.percentageOfTotal.roundToInt()}% of total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { (item.percentageOfTotal / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onViewFullExpensesClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = stringResource(Res.string.view_full_expenses),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
