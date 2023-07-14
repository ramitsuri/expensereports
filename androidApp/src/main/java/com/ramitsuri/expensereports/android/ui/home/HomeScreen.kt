package com.ramitsuri.expensereports.android.ui.home


import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.ui.views.PieChart
import com.ramitsuri.expensereports.android.ui.views.Value
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.formatRounded
import com.ramitsuri.expensereports.android.utils.monthYear
import com.ramitsuri.expensereports.data.AccountBalance
import com.ramitsuri.expensereports.viewmodel.ExpenseSavingsShare
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
        expenseSavingsShare = viewState.expenseSavingsShare,
        onIncludeDeductionsChanged = viewModel::onIncludeDeductionsChanged,
        accountBalances = viewState.accountBalances,
        transactionGroups = viewState.transactionGroups,
    )
}


@Composable
private fun HomeContent(
    netWorth: List<MonthAccountBalance>,
    expenseSavingsShare: ExpenseSavingsShare,
    onIncludeDeductionsChanged: () -> Unit,
    accountBalances: List<AccountBalance>,
    transactionGroups: List<AccountBalance>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        NetWorthContent(netWorth, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(8.dp))
        SavingsExpenseIncomeContent(
            expenseSavingsShare = expenseSavingsShare,
            onIncludeDeductionsChanged = onIncludeDeductionsChanged,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        AccountBalancesContent(
            accountBalances = accountBalances,
            transactionGroups = transactionGroups,
            modifier = Modifier
                .weight(1f)
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
            modifier = Modifier
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
private fun SavingsExpenseIncomeContent(
    expenseSavingsShare: ExpenseSavingsShare,
    onIncludeDeductionsChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val values = buildList {
        add(
            Value(
                color = Color(0xff47B39C),
                sharePercent = expenseSavingsShare.expensesSharePercent
            )
        )
        add(
            Value(
                color = Color(0xffb12c21),
                sharePercent = expenseSavingsShare.savingsSharePercent
            )
        )
        if (expenseSavingsShare.includeDeductions) {
            add(
                Value(
                    color = Color(0xff00658f),
                    sharePercent = expenseSavingsShare.deductionsSharePercent
                )
            )
        }
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.home_expenses_and_savings),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.5f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PieChart(values)
                }
                Divider(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .width(0.5.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.5f)
                        .padding(top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SimpleRow(
                        text = stringResource(id = R.string.home_expenses_share),
                        value = expenseSavingsShare.expensesSharePercent.formatPercent(),
                        color = values[0].color,
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SimpleRow(
                        text = stringResource(id = R.string.home_savings_share),
                        value = expenseSavingsShare.savingsSharePercent.formatPercent(),
                        color = values[1].color,
                        modifier = Modifier
                    )
                    if (expenseSavingsShare.includeDeductions) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SimpleRow(
                            text = stringResource(id = R.string.home_deductions_share),
                            value = expenseSavingsShare.deductionsSharePercent.formatPercent(),
                            color = values[2].color,
                            modifier = Modifier
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .toggleable(
                        value = expenseSavingsShare.includeDeductions,
                        onValueChange = { onIncludeDeductionsChanged() },
                        role = Role.Checkbox
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(id = R.string.home_include_deductions),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Checkbox(
                    modifier = Modifier
                        .size(16.dp),
                    checked = expenseSavingsShare.includeDeductions,
                    onCheckedChange = null
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleRow(text: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            modifier = Modifier
                .basicMarquee(iterations = 10)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier
                .basicMarquee(iterations = 10)
        )
    }
}

@Composable
private fun AccountBalancesContent(
    accountBalances: List<AccountBalance>,
    transactionGroups: List<AccountBalance>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        val itemsInRow = 3
        val accounts = accountBalances
            .plus(transactionGroups)
        val accountChunks = accounts.chunked(itemsInRow)
        accountChunks.forEachIndexed { index, chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunk.forEach { account ->
                    AccountItem(
                        name = account.name,
                        balance = account.balance,
                        modifier = Modifier
                            .width(0.dp)
                            .weight(1 / itemsInRow.toFloat())
                    )
                }
                repeat(itemsInRow - chunk.size) {
                    Spacer(
                        modifier = Modifier
                            .width(0.dp)
                            .weight(1 / itemsInRow.toFloat())
                    )
                }
            }
            if (index != accountChunks.lastIndex) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee(iterations = 10)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = balance.format(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee(iterations = 10)
                )
            }
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
                val newY =
                    (pathContainerHeight - (value.balance.doubleValue(false) - minValue) *
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

private fun Float.formatPercent(): String = "$this%"