package com.ramitsuri.expensereports.android.ui.components

import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.formatRounded
import java.math.BigDecimal

@Composable
fun LineChart(
    balances: List<LineChartValue>,
    color: Color,
    modifier: Modifier = Modifier,
    formatNumericValueRounded: Boolean = true,
) {
    val data = balances.map { it.numericValue.toDouble() }
    if (data.size < 2) {
        return
    }
    val xValueToBalanceMap = remember(balances) {
        mutableMapOf<Float, LineChartValue>()
    }
    var verticalLineXValue by remember(balances) {
        mutableStateOf<Float?>(null)
    }
    val transparentGraphColor = remember(color) {
        color.copy(alpha = 0.5f)
    }
    val animationProgress = remember {
        Animatable(0f)
    }
    LaunchedEffect(balances, block = {
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

            val numberOfEntries = balances.size - 1
            val widthPerDataPoint = pathContainerWidth / numberOfEntries

            val maxValue = balances.maxBy { it.numericValue }.numericValue.toDouble()
            val minValue = balances.minBy { it.numericValue }.numericValue.toDouble()
            val range = maxValue - minValue

            val heightPxPerValuePoint = pathContainerHeight / range

            var previousX = 0f
            var previousY = pathContainerHeight
            balances.forEachIndexed { index, value ->
                val newX = index * widthPerDataPoint
                val newY =
                    (pathContainerHeight - (value.numericValue.toDouble() - minValue) *
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
                text = balances.first().label,
                style = MaterialTheme.typography.labelSmall
            )
            val verticalX = verticalLineXValue
            if (verticalX != null) {
                val value = xValueToBalanceMap[verticalX]
                if (value != null) {
                    val formatted = if (formatNumericValueRounded) {
                        value.numericValue.formatRounded()
                    } else {
                        value.numericValue.format()
                    }
                    Text(
                        text = "${value.label}: $formatted",
                        style = MaterialTheme.typography.labelSmall
                    )
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
            Text(
                text = balances.last().label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

data class LineChartValue(val label: String, val numericValue: BigDecimal)