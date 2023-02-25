package com.ramitsuri.expensereports.android.ui.home

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.homeMonthYear
import com.ramitsuri.expensereports.android.utils.monthYear
import com.ramitsuri.expensereports.viewmodel.MainAccountBalance
import com.ramitsuri.expensereports.viewmodel.HomeViewModel
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
                graphColor = MaterialTheme.colorScheme.onBackground,
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
    graphColor: Color,
    modifier: Modifier = Modifier
) {
    val data = netWorth.map { it.balance.doubleValue(exactRequired = false) }
    if (data.size < 2) {
        return
    }

    val transparentGraphColor = remember(key1 = graphColor) {
        graphColor.copy(alpha = 0.5f)
    }

    val (lowerValue, upperValue) = remember(key1 = data) {
        Pair(
            data.min(),
            data.max()
        )
    }
    val animationProgress = remember {
        Animatable(0f)
    }
    LaunchedEffect(key1 = netWorth, block = {
        animationProgress.animateTo(1f, tween(3000))
    })
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth()
        ) {
            val spacePerHour = size.width / data.size
            var lastX = 0f
            var firstY = 0f
            val strokePath = Path().apply {
                val height = size.height
                for (i in data.indices) {
                    val info = data[i]
                    val nextInfo = data.getOrNull(i + 1) ?: data.last()
                    val leftRatio =
                        (info - lowerValue) / (upperValue - lowerValue)
                    val rightRatio =
                        (nextInfo - lowerValue) / (upperValue - lowerValue)

                    val x1 = i * spacePerHour
                    val y1 = height - (leftRatio * height).toFloat()

                    if (i == 0) {
                        firstY = y1
                    }

                    val x2 = (i + 1) * spacePerHour
                    val y2 = height - (rightRatio * height).toFloat()
                    if (i == 0) {
                        moveTo(x1, y1)
                    }
                    lastX = if (i == data.lastIndex) {
                        size.width
                    } else {
                        (x1 + x2) / 2f
                    }
                    quadraticBezierTo(
                        x1, y1, lastX, (y1 + y2) / 2f
                    )
                }
            }

            val fillPath = android.graphics.Path(strokePath.asAndroidPath())
                .asComposePath()
                .apply {
                    lineTo(lastX, size.height)
                    lineTo(0f, size.height)
                    close()
                }
            clipRect(
                top = -40f,
                bottom = size.height + 40f,
                right = size.width * animationProgress.value
            ) {
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            transparentGraphColor,
                            Color.Transparent
                        ),
                        endY = size.height
                    ),
                )
                drawPath(
                    path = strokePath,
                    color = graphColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
                val dottedPath = Path().apply {
                    moveTo(0f, firstY)
                    lineTo(lastX, firstY)
                }
                drawPath(
                    path = dottedPath,
                    color = graphColor.copy(alpha = .8f),
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
            Text(
                text = netWorth.last().date.monthYear(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}