package com.ramitsuri.expensereports.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Elderly
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.formatted
import com.ramitsuri.expensereports.ui.components.AnimationMode
import com.ramitsuri.expensereports.ui.components.DividerProperties
import com.ramitsuri.expensereports.ui.components.DrawStyle
import com.ramitsuri.expensereports.ui.components.GridProperties
import com.ramitsuri.expensereports.ui.components.HorizontalIndicatorProperties
import com.ramitsuri.expensereports.ui.components.LabelHelperProperties
import com.ramitsuri.expensereports.ui.components.Line
import com.ramitsuri.expensereports.ui.components.LineChart
import com.ramitsuri.expensereports.ui.components.PopupProperties
import com.ramitsuri.expensereports.ui.theme.greenColor
import com.ramitsuri.expensereports.ui.theme.redColor
import com.ramitsuri.expensereports.utils.formatRounded
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.month_names_short
import expensereports.shared.generated.resources.net_worth
import expensereports.shared.generated.resources.value1_value2_formatted
import expensereports.shared.generated.resources.value1_value2_new_line_formatted
import kotlinx.coroutines.delay
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewState: HomeViewState,
    windowSize: WindowSizeClass,
    onNetWorthPeriodSelected: (Period) -> Unit,
    onTransactionsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding(),
    ) {
        val scrollBehavior =
            TopAppBarDefaults.enterAlwaysScrollBehavior(
                rememberTopAppBarState(),
            )
        Toolbar(
            scrollBehavior = scrollBehavior,
            refreshState = viewState.refreshState,
            onTransactionsClick = onTransactionsClick,
            onReportsClick = onReportsClick,
            onSettingsClick = onSettingsClick,
            onRefresh = onRefresh,
        )
        PullToRefreshBox(
            isRefreshing = viewState.refreshState.isPullToRefreshAvailable && viewState.refreshState.isRefreshing,
            onRefresh = onRefresh,
        ) {
            LazyVerticalGrid(
                modifier =
                    Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(horizontal = 16.dp),
                columns =
                    if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                        Fixed(1)
                    } else {
                        Fixed(2)
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    NetWorths(
                        netWorths = viewState.netWorths,
                        selectedPeriod = viewState.selectedNetWorthPeriod,
                        periods = viewState.periods,
                        onPeriodSelected = onNetWorthPeriodSelected,
                    )
                }
                items(viewState.expandableCardGroups) { expandableCardGroups ->
                    ExpandableCard(
                        cardName = expandableCardGroups.name,
                        cardAmount = expandableCardGroups.value,
                        isCardAmountPositive = expandableCardGroups.isValuePositive,
                        children = expandableCardGroups.children,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Toolbar(
    scrollBehavior: TopAppBarScrollBehavior,
    refreshState: HomeViewState.Refresh,
    onTransactionsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    CenterAlignedTopAppBar(
        colors =
            TopAppBarDefaults
                .centerAlignedTopAppBarColors()
                .copy(scrolledContainerColor = MaterialTheme.colorScheme.background),
        title = { },
        actions = {
            if (!refreshState.isPullToRefreshAvailable) {
                IconButton(
                    onClick = {
                        if (!refreshState.isRefreshing) {
                            onRefresh()
                        }
                    },
                    modifier =
                        Modifier
                            .size(48.dp)
                            .padding(4.dp),
                ) {
                    if (refreshState.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "",
                        )
                    }
                }
            }
            IconButton(
                onClick = onTransactionsClick,
                modifier =
                    Modifier
                        .size(48.dp)
                        .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.List,
                    contentDescription = "",
                )
            }
            IconButton(
                onClick = onReportsClick,
                modifier =
                    Modifier
                        .size(48.dp)
                        .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "",
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier =
                    Modifier
                        .size(48.dp)
                        .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "",
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun NetWorths(
    netWorths: List<HomeViewState.NetWorth>,
    selectedPeriod: Period,
    periods: List<Period>,
    onPeriodSelected: (Period) -> Unit,
) {
    val popupLabels = netWorths.map { it.formatted() }
    var periodSelectorVisible by remember(selectedPeriod) { mutableStateOf(false) }
    LaunchedEffect(selectedPeriod) {
        delay(1500)
        periodSelectorVisible = true
    }
    val data =
        remember(netWorths) {
            Line(
                label = "",
                values = netWorths.map { it.netWorth.toDouble() },
                color = SolidColor(Color(0xFF23af92)),
                firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                secondGradientFillColor = Color.Transparent,
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(width = 2.dp),
                curvedEdges = true,
            )
        }
    Card(
        modifier =
            Modifier
                .height(320.dp)
                .fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.net_worth),
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp),
            )
            LineChart(
                modifier = Modifier.weight(1f),
                data = listOf(data),
                dividerProperties = DividerProperties(enabled = false),
                animationMode = AnimationMode.Together(delayBuilder = { it * 500L }),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                gridProperties = GridProperties(enabled = false),
                indicatorProperties = HorizontalIndicatorProperties(enabled = false),
                popupProperties =
                    PopupProperties(
                        textStyle =
                            TextStyle.Default.copy(
                                color = Color.White,
                                fontSize = 12.sp,
                            ),
                        contentBuilder = { index ->
                            popupLabels[index]
                        },
                    ),
            )
            AnimatedVisibility(
                visible = periodSelectorVisible,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        netWorths.firstOrNull()?.let {
                            Text(
                                text = it.monthYear.formatted(withNewLine = true),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                        netWorths.lastOrNull()?.let {
                            Text(
                                text = it.monthYear.formatted(withNewLine = true),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    PeriodSelector(
                        selectedPeriod = selectedPeriod,
                        periods = periods,
                        onPeriodSelected = onPeriodSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableCard(
    cardName: String,
    cardAmount: String,
    isCardAmountPositive: Boolean,
    children: List<HomeViewState.ExpandableCardGroup.Child>,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var rotateCount by remember(Unit) { mutableIntStateOf(0) }
    val iconRotate by animateFloatAsState(
        targetValue = if (isExpanded) (0f + (rotateCount * 180f)) else (180f + ((rotateCount - 1) * 180f)),
        animationSpec = tween(300, easing = LinearOutSlowInEasing),
        label = "buttonRotate",
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            isExpanded = !isExpanded
                            rotateCount++
                        }
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = getExpandableCardImage(cardName),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isCardAmountPositive) greenColor else redColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    text = cardName,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    text = cardAmount,
                    color = if (isCardAmountPositive) greenColor else redColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    modifier =
                        Modifier.size(16.dp)
                            .rotate(iconRotate),
                )
            }
            AnimatedVisibility(isExpanded) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        children.forEach {
                            Column(
                                modifier =
                                    Modifier
                                        .width(160.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = it.title,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = it.value, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun getExpandableCardImage(name: String): ImageVector {
    return when (name) {
        "Expenses MTD" -> Icons.Outlined.ShoppingCart
        "Savings YTD" -> Icons.Outlined.Savings
        "Travel" -> Icons.Outlined.BeachAccess
        "Cash" -> Icons.Outlined.AttachMoney
        "Retirement" -> Icons.Outlined.Elderly
        "Credit Cards" -> Icons.Outlined.CreditCard

        "Salary",
        "Salary MTD",
        -> Icons.Outlined.Payments

        "Taxes YTD" -> Icons.Outlined.AccountBalance
        else -> Icons.Outlined.BeachAccess
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: Period,
    periods: List<Period>,
    onPeriodSelected: (Period) -> Unit,
) {
    SingleChoiceSegmentedButtonRow {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                shape =
                    SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = periods.size,
                    ),
                onClick = { onPeriodSelected(period) },
                selected = period == selectedPeriod,
                label = {
                    Text(
                        text = period.formatted(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                icon = { },
            )
        }
    }
}

@Composable
private fun HomeViewState.NetWorth.formatted(): String {
    return stringResource(
        Res.string.value1_value2_new_line_formatted,
        monthYear.formatted(),
        netWorth.formatRounded(),
    )
}

@Composable
private fun MonthYear.formatted(withNewLine: Boolean = false): String {
    val month = stringArrayResource(Res.array.month_names_short)[month.number - 1]
    return if (withNewLine) {
        stringResource(Res.string.value1_value2_new_line_formatted, month, year.toString())
    } else {
        stringResource(Res.string.value1_value2_formatted, month, year.toString())
    }
}
