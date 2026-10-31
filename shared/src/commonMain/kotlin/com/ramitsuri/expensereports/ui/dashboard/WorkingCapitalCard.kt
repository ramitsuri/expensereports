package com.ramitsuri.expensereports.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.ui.theme.greenColor
import com.ramitsuri.expensereports.ui.theme.redColor
import com.ramitsuri.expensereports.utils.formatCurrency
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.credit_debt
import expensereports.shared.generated.resources.liquid_cash
import expensereports.shared.generated.resources.working_capital
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@Composable
fun WorkingCapitalCard(
    summary: WorkingCapitalSummary,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
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
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.working_capital),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .rotate(rotationAngle),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Headline with tabular figures
            Text(
                text = summary.netLiquidity.formatCurrency(),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFeatureSettings = "tnum",
                        fontWeight = FontWeight.Bold,
                    ),
                color = if (summary.netLiquidity >= 0) MaterialTheme.colorScheme.onSurface else redColor,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle Row: Liquid Cash vs Credit Balances
            val liquidCashLabel = stringResource(Res.string.liquid_cash)
            val creditDebtLabel = stringResource(Res.string.credit_debt)
            val formattedCash = summary.totalCash.formatCurrency(showSign = true)
            val formattedDebt = (-summary.totalCreditDebt).formatCurrency()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "$liquidCashLabel: $formattedCash",
                    style = MaterialTheme.typography.bodySmall,
                    color = greenColor,
                )
                Text(
                    text = "$creditDebtLabel: $formattedDebt",
                    style = MaterialTheme.typography.bodySmall,
                    color = redColor,
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    val visibleAccounts = summary.accounts.filter { abs(it.balance) > 0.0001 }
                    if (visibleAccounts.isEmpty()) {
                        Text(
                            text = "No accounts available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    } else {
                        visibleAccounts.forEach { account ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = account.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = account.group,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                val isCash = account.group.equals("Cash", ignoreCase = true)
                                val isCredit = account.group.equals("Credit Cards", ignoreCase = true)
                                val balanceColor =
                                    if (isCredit) {
                                        redColor
                                    } else if (isCash) {
                                        if (account.balance >= 0) greenColor else redColor
                                    } else {
                                        if (account.balance >= 0) MaterialTheme.colorScheme.onSurface else redColor
                                    }
                                Text(
                                    text = account.balance.formatCurrency(showSign = isCash && account.balance > 0),
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontFeatureSettings = "tnum",
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                    color = balanceColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
