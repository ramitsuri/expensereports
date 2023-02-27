package com.ramitsuri.expensereports.android.ui.home


import android.graphics.PointF
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.formatRounded
import com.ramitsuri.expensereports.android.utils.homeMonthYear
import com.ramitsuri.expensereports.android.utils.monthYear
import com.ramitsuri.expensereports.viewmodel.HomeViewModel
import com.ramitsuri.expensereports.viewmodel.MainAccountBalance
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = getViewModel()
) {
    val viewState = viewModel.state.collectAsState().value

    HomeContent(
        netWorth = viewState.netWorth,
        savings = viewState.savings,
        expenses = viewState.expenses,
        incomes = viewState.incomes
    )
}

@Composable
private fun HomeContent(
    netWorth: List<MainAccountBalance>,
    savings: List<MainAccountBalance>,
    expenses: List<MainAccountBalance>,
    incomes: List<MainAccountBalance>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        NetWorthContent(netWorth, modifier = modifier.weight(0.4F))
        Spacer(modifier = modifier.height(8.dp))
        MainAccountContent(
            titleRes = R.string.home_savings,
            accountBalances = savings,
            modifier = modifier.weight(0.2F)
        )
        Spacer(modifier = modifier.height(8.dp))
        MainAccountContent(
            titleRes = R.string.home_expenses,
            accountBalances = expenses,
            modifier = modifier.weight(0.2F)
        )
        Spacer(modifier = modifier.height(8.dp))
        MainAccountContent(
            titleRes = R.string.home_incomes,
            accountBalances = incomes,
            modifier = modifier.weight(0.2F)
        )
    }
}

@Composable
private fun NetWorthContent(
    netWorth: List<MainAccountBalance>,
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
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainAccountContent(
    @StringRes titleRes: Int,
    accountBalances: List<MainAccountBalance>,
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
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            if (accountBalances.isNotEmpty()) {
                val pagerState = rememberPagerState()
                PagerWithIndicator(
                    count = accountBalances.size,
                    pagerState = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = accountBalances[page].balance.format(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = accountBalances[page].date.homeMonthYear(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ConsumedBar(
    consumedText: String,
    helpText: String,
    fillPercent: Float,
    modifier: Modifier = Modifier,
    barHeight: Dp = 12.dp,
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

    Column(modifier = modifier.padding(8.dp)) {
        val textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light)
        Canvas(
            modifier = Modifier
                .fillMaxHeight(0.2f)
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
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
        ) {
            val containerHeight = size.height
            val containerWidth = size.width
            val outlineStrokeWidth = 2.dp.toPx()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerWithIndicator(
    count: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageContent: @Composable (page: Int) -> Unit
) {
    HorizontalPager(
        pageCount = count,
        state = pagerState,
        modifier = modifier
    ) { page ->
        pageContent(page)
    }
    PageIndicator(
        numberOfPages = count,
        modifier = Modifier.padding(8.dp),
        selectedPage = pagerState.currentPage,
        defaultRadius = 8.dp,
        selectedLength = 12.dp,
        space = 8.dp,
        animationDurationInMillis = 300,
    )
}

@Composable
private fun PageIndicator(
    numberOfPages: Int,
    modifier: Modifier = Modifier,
    selectedPage: Int = 0,
    selectedColor: Color = MaterialTheme.colorScheme.onBackground,
    defaultColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    defaultRadius: Dp = 20.dp,
    selectedLength: Dp = 60.dp,
    space: Dp = 30.dp,
    animationDurationInMillis: Int = 300,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space),
        modifier = modifier,
    ) {
        for (i in 0 until numberOfPages) {
            val isSelected = i == selectedPage
            val color: Color by animateColorAsState(
                targetValue = if (isSelected) {
                    selectedColor
                } else {
                    defaultColor
                },
                animationSpec = tween(
                    durationMillis = animationDurationInMillis,
                )
            )
            val width: Dp by animateDpAsState(
                targetValue = if (isSelected) {
                    selectedLength
                } else {
                    defaultRadius
                },
                animationSpec = tween(
                    durationMillis = animationDurationInMillis,
                )
            )
            Canvas(
                modifier = Modifier
                    .size(
                        width = width,
                        height = defaultRadius,
                    ),
            ) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset.Zero,
                    size = Size(
                        width = width.toPx(),
                        height = defaultRadius.toPx(),
                    ),
                    cornerRadius = CornerRadius(
                        x = defaultRadius.toPx(),
                        y = defaultRadius.toPx(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    netWorth: List<MainAccountBalance>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val data = netWorth.map { it.balance.doubleValue(exactRequired = false) }
    if (data.size < 2) {
        return
    }
    val xValueToBalanceMap = remember(netWorth) {
        mutableMapOf<Float, MainAccountBalance>()
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
                .fillMaxHeight(0.8f)
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
                }
            }
            Text(
                text = netWorth.last().date.monthYear(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}