package com.ramitsuri.expensereports.ui.home

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramitsuri.expensereports.ui.components.AnimationMode
import com.ramitsuri.expensereports.ui.components.DividerProperties
import com.ramitsuri.expensereports.ui.components.DrawStyle
import com.ramitsuri.expensereports.ui.components.GridProperties
import com.ramitsuri.expensereports.ui.components.HorizontalIndicatorProperties
import com.ramitsuri.expensereports.ui.components.LabelHelperProperties
import com.ramitsuri.expensereports.ui.components.Line
import com.ramitsuri.expensereports.ui.components.LineChart
import com.ramitsuri.expensereports.ui.components.PopupProperties
import com.ramitsuri.expensereports.utils.formatRounded
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.month_names_short
import expensereports.shared.generated.resources.net_worth
import expensereports.shared.generated.resources.net_worth_in_month
import expensereports.shared.generated.resources.period_all
import expensereports.shared.generated.resources.period_last_three_years
import expensereports.shared.generated.resources.period_one_year
import expensereports.shared.generated.resources.period_this_year
import kotlinx.coroutines.delay
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    viewState: HomeViewState,
    onNetWorthPeriodSelected: (HomeViewState.Period) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .displayCutoutPadding(),
    ) {
        NetWorths(
            netWorths = viewState.netWorths,
            selectedPeriod = viewState.selectedNetWorthPeriod,
            periods = viewState.periods,
            onPeriodSelected = onNetWorthPeriodSelected,
        )
    }
}

@Composable
private fun NetWorths(
    netWorths: List<HomeViewState.NetWorth>,
    selectedPeriod: HomeViewState.Period,
    periods: List<HomeViewState.Period>,
    onPeriodSelected: (HomeViewState.Period) -> Unit,
) {
    val popupLabels = netWorths.map { it.formatted() }
    var periodSelectorVisible by remember(selectedPeriod) { mutableStateOf(false) }
    LaunchedEffect(selectedPeriod) {
        delay(1500)
        periodSelectorVisible = true
    }
    val data = remember(netWorths) {
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
        modifier = Modifier
            .height(320.dp)
            .fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.net_worth),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 8.dp),
            )
            LineChart(
                data = listOf(data),
                dividerProperties = DividerProperties(enabled = false),
                animationMode = AnimationMode.Together(delayBuilder = { it * 500L }),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                gridProperties = GridProperties(enabled = false),
                indicatorProperties = HorizontalIndicatorProperties(enabled = false),
                popupProperties = PopupProperties(
                    textStyle = TextStyle.Default.copy(
                        color = Color.White,
                        fontSize = 12.sp
                    ),
                    contentBuilder = { index ->
                        popupLabels[index]
                    }
                ),
            )
            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = periodSelectorVisible
            ) {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    periods = periods,
                    onPeriodSelected = onPeriodSelected,
                )
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: HomeViewState.Period,
    periods: List<HomeViewState.Period>,
    onPeriodSelected: (HomeViewState.Period) -> Unit,
) {
    SingleChoiceSegmentedButtonRow {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = periods.size
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
private fun HomeViewState.Period.formatted() = when (this) {
    is HomeViewState.Period.ThisYear -> stringResource(Res.string.period_this_year)
    is HomeViewState.Period.OneYear -> stringResource(Res.string.period_one_year)
    is HomeViewState.Period.LastThreeYears -> stringResource(Res.string.period_last_three_years)
    is HomeViewState.Period.AllTime -> stringResource(Res.string.period_all)
}

@Composable
private fun HomeViewState.NetWorth.formatted(): String {
    val month = stringArrayResource(Res.array.month_names_short)[monthYear.month.number - 1]
    return stringResource(
        Res.string.net_worth_in_month,
        month,
        monthYear.year.toString(),
        netWorth.formatRounded()
    )
}