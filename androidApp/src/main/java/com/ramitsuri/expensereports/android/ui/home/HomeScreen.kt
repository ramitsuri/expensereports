package com.ramitsuri.expensereports.android.ui.home


import android.graphics.PointF
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.formatRounded
import com.ramitsuri.expensereports.android.utils.monthYear
import com.ramitsuri.expensereports.viewmodel.AccountBalance
import com.ramitsuri.expensereports.viewmodel.Balance
import com.ramitsuri.expensereports.viewmodel.HomeViewModel
import com.ramitsuri.expensereports.viewmodel.MonthAccountBalance
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = getViewModel()
) {
    val viewState = viewModel.state.collectAsState().value

    HomeContent(
        netWorth = viewState.netWorth,
        expenseBalance = viewState.expenses,
        savingsBalance = viewState.savings,
        salary = viewState.monthSalary,
        liabilityAccountBalances = viewState.liabilityAccountBalances,
        assetAccountBalances = viewState.assetAccountBalances
    )
}

@Composable
private fun HomeContent(
    netWorth: List<MonthAccountBalance>,
    expenseBalance: Balance,
    savingsBalance: Balance,
    salary: BigDecimal,
    assetAccountBalances: List<AccountBalance>,
    liabilityAccountBalances: List<AccountBalance>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        NetWorthContent(netWorth, modifier = modifier.heightIn(min = 200.dp, max = 240.dp))
        Spacer(modifier = modifier.height(8.dp))
        MainAccountContent(
            titleRes = R.string.home_savings,
            monthRes = R.string.home_savings_this_month_format,
            annualRes = R.string.home_savings_this_year_format,
            balance = savingsBalance,
            modifier = modifier.heightIn(128.dp, max = 152.dp)
        )
        Spacer(modifier = modifier.height(8.dp))
        MainAccountContent(
            titleRes = R.string.home_expenses,
            monthRes = R.string.home_expense_this_month_format,
            annualRes = R.string.home_expense_this_year_format,
            balance = expenseBalance,
            modifier = modifier.heightIn(128.dp, max = 152.dp)
        )
        Spacer(modifier = modifier.height(8.dp))
        AccountBalancesContent(
            salary = salary,
            assetAccountBalances = assetAccountBalances,
            liabilityAccountBalances = liabilityAccountBalances,
            modifier = modifier.heightIn(120.dp, max = 144.dp)
        )
    }
}

@Composable
private fun NetWorthContent(
    netWorth: List<MonthAccountBalance>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.home_net_worth),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            LineChart(
                netWorth = netWorth,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
            )
        }

    }
}

@Composable
private fun MainAccountContent(
    @StringRes titleRes: Int,
    @StringRes monthRes: Int,
    @StringRes annualRes: Int,
    balance: Balance,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = titleRes),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Month
                ConsumedBar(
                    consumedText = balance.month.format(),
                    helpText = stringResource(id = monthRes, balance.monthMax.format()),
                    fillPercent = if (balance.monthMax != BigDecimal.ZERO) {
                        balance.month.divide(balance.monthMax, DecimalMode.US_CURRENCY)
                            .floatValue(exactRequired = false)
                    } else {
                        BigDecimal.ZERO.floatValue(exactRequired = false)
                    },
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Annual
                ConsumedBar(
                    consumedText = balance.annual.format(),
                    helpText = stringResource(id = annualRes, balance.annualMax.format()),
                    fillPercent = if (balance.annualMax != BigDecimal.ZERO) {
                        balance.annual.divide(balance.annualMax, DecimalMode.US_CURRENCY)
                            .floatValue(exactRequired = false)
                    } else {
                        BigDecimal.ZERO.floatValue(exactRequired = false)
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    segments = 12,
                    segmentColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AccountBalancesContent(
    salary: BigDecimal,
    assetAccountBalances: List<AccountBalance>,
    liabilityAccountBalances: List<AccountBalance>,
    modifier: Modifier = Modifier
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            AccountItem(
                name = stringResource(id = R.string.home_account_salary_name),
                balance = salary
            )
        }
        assetAccountBalances.forEach { account ->
            item {
                AccountItem(
                    name = account.name,
                    balance = account.balance
                )
            }
        }
        liabilityAccountBalances.forEach { account ->
            item {
                AccountItem(
                    name = account.name,
                    balance = account.balance
                )
            }
        }
    }
}

@Composable
private fun AccountItem(
    name: String,
    balance: BigDecimal,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = balance.format(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun ConsumedBar(
    consumedText: String,
    helpText: String,
    fillPercent: Float,
    modifier: Modifier = Modifier,
    barHeight: Dp = 8.dp,
    cornerRadius: Dp = 8.dp,
    color: Color,
    segments: Int = 0,
    segmentColor: Color = Color.White
) {
    val fill = if (fillPercent < 0f) {
        0f
    } else if (fillPercent > 1f) {
        1f
    } else {
        fillPercent
    }
    val animationProgress = remember {
        Animatable(0f)
    }
    LaunchedEffect(fill, block = {
        animationProgress.animateTo(fill, tween(3000))
    })
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
    ) {
        val textStyle = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Light
        )
        Canvas(
            modifier = Modifier
                .height(12.dp)
                .fillMaxWidth()
        ) {
            val measuredText = textMeasurer.measure(AnnotatedString(consumedText))
            val minOffset = 4.dp.toPx() // Left padding
            val maxOffset = size.width - measuredText.size.width - 4.dp.toPx()
            var offset = size.width * fill - (measuredText.size.width / 2)
            if (offset > maxOffset) {
                offset = maxOffset
            } else if (offset < minOffset) {
                offset = minOffset
            }
            drawText(
                textMeasurer,
                consumedText,
                style = textStyle,
                topLeft = Offset(
                    x = offset,
                    y = (size.height - measuredText.size.height) / 2
                )
            )
        }
        Canvas(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth()
        ) {
            val containerHeight = size.height
            val containerWidth = size.width
            val outlineStrokeWidth = 1.dp.toPx()
            drawRoundRect(
                color = color,
                topLeft = Offset(x = 0f, y = (containerHeight - barHeight.toPx()) / 2),
                size = size.copy(height = barHeight.toPx()),
                cornerRadius = CornerRadius(cornerRadius.toPx()),
                style = Stroke(width = outlineStrokeWidth)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x = 0f, y = (containerHeight - barHeight.toPx()) / 2),
                size = Size(
                    width = animationProgress.value * containerWidth,
                    height = barHeight.toPx()
                ),
                cornerRadius = CornerRadius(cornerRadius.toPx()),
            )
            if (segments > 0) {
                val segmentWidth = containerWidth / segments
                repeat(segments - 1) { segment ->
                    val xOffset = (segment + 1) * segmentWidth
                    drawLine(
                        color = segmentColor,
                        start = Offset(
                            x = xOffset,
                            y = (containerHeight - barHeight.toPx() + outlineStrokeWidth) / 2
                        ),
                        end = Offset(
                            x = xOffset,
                            y = (containerHeight + barHeight.toPx() - outlineStrokeWidth) / 2
                        ),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = helpText,
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun LineChart(
    netWorth: List<MonthAccountBalance>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val data = netWorth.map { it.balance.doubleValue(exactRequired = false) }
    if (data.size < 2) {
        return
    }
    val xValueToBalanceMap = remember(netWorth) {
        mutableMapOf<Float, MonthAccountBalance>()
    }
    var verticalLineXValue by remember(netWorth) {
        mutableStateOf<Float?>(null)
    }
    val transparentGraphColor = remember(color) {
        color.copy(alpha = 0.5f)
    }
    val animationProgress = remember {
        Animatable(0f)
    }
    LaunchedEffect(netWorth, block = {
        animationProgress.animateTo(1f, tween(3000))
    })

    val hapticFeedback = LocalHapticFeedback.current

    Column(modifier = modifier
        .pointerInput(Unit) {
            detectHorizontalDragGestures(onDragEnd = {
                verticalLineXValue = null
            }
            ) { change, _ ->
                change.consume()
                val gestureXPosition = change.position.x
                var matchingXValue: Float? = null
                for (xValueFromMap in xValueToBalanceMap.keys) {
                    if (matchingXValue == null) {
                        matchingXValue = xValueFromMap
                    }
                    if (xValueFromMap > gestureXPosition) {
                        break
                    } else {
                        matchingXValue = xValueFromMap
                    }
                }
                verticalLineXValue = matchingXValue
            }
        }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight(0.75f)
                .fillMaxWidth()
        ) {
            val mainPath = Path()
            val pathContainerHeight = size.height
            val pathContainerWidth = size.width

            val numberOfEntries = netWorth.size - 1
            val widthPerDataPoint = pathContainerWidth / numberOfEntries

            val maxValue = netWorth.maxBy { it.balance }.balance.doubleValue(false)
            val minValue = netWorth.minBy { it.balance }.balance.doubleValue(false)
            val range = maxValue - minValue

            val heightPxPerValuePoint = pathContainerHeight / range

            var previousX = 0f
            var previousY = pathContainerHeight
            netWorth.forEachIndexed { index, value ->
                val newX = index * widthPerDataPoint
                val newY = (pathContainerHeight - (value.balance.doubleValue(false) - minValue) *
                        heightPxPerValuePoint).toFloat()

                if (index == 0) {
                    mainPath.moveTo(0f, newY)
                }
                val controlPoint1 = PointF((newX + previousX) / 2f, previousY)
                val controlPoint2 = PointF((newX + previousX) / 2f, newY)
                mainPath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    newX, newY
                )
                xValueToBalanceMap[newX] = value
                previousX = newX
                previousY = newY
            }
            val gradientPath = android.graphics.Path(mainPath.asAndroidPath())
                .asComposePath()
                .apply {
                    lineTo(previousX, pathContainerHeight)
                    lineTo(0f, pathContainerHeight)
                    close()
                }
            clipRect(
                top = -(8.dp.toPx()),
                bottom = pathContainerHeight + 8.dp.toPx(),
                right = pathContainerWidth * animationProgress.value
            ) {
                drawPath(mainPath, color, style = Stroke(2.dp.toPx()))
                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            transparentGraphColor,
                            Color.Transparent
                        ),
                        endY = pathContainerHeight
                    ),
                )
            }
            val verticalX = verticalLineXValue
            if (verticalX != null) {
                val verticalPath = Path().apply {
                    moveTo(verticalX, 0f)
                    lineTo(verticalX, pathContainerHeight)
                }
                drawPath(
                    path = verticalPath,
                    color = color.copy(alpha = .8f),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f), 0f)
                    )
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = netWorth.first().date.monthYear(),
                style = MaterialTheme.typography.labelSmall
            )
            val verticalX = verticalLineXValue
            if (verticalX != null) {
                val value = xValueToBalanceMap[verticalX]
                if (value != null) {
                    Text(
                        text = "${value.date.monthYear()}: ${value.balance.formatRounded()}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
            Text(
                text = netWorth.last().date.monthYear(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}