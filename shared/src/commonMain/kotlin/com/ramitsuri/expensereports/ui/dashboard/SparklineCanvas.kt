package com.ramitsuri.expensereports.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramitsuri.expensereports.ui.theme.greenColor
import com.ramitsuri.expensereports.ui.theme.redColor
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.no_history_available
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@Composable
fun SparklineCanvas(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    positiveColor: Color = greenColor,
    negativeColor: Color = redColor,
    neutralColor: Color = MaterialTheme.colorScheme.outlineVariant,
    selectedPoint: TrendPoint? = null,
    onPointSelected: (TrendPoint?) -> Unit = {},
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.no_history_available),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(points.size) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val slotWidth = 36.dp
    val barWidth = 16.dp

    val maxAbs = points.maxOfOrNull { abs(it.value) }?.coerceAtLeast(50.0) ?: 100.0
    val hasNegative = points.any { it.value < 0 }
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        points.forEach { point ->
            val isSelected = selectedPoint == point

            Column(
                modifier =
                    Modifier
                        .width(slotWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (isSelected) {
                                onPointSelected(null)
                            } else {
                                onPointSelected(point)
                            }
                        },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val zeroY =
                            if (hasNegative) {
                                canvasHeight * 0.7f
                            } else {
                                canvasHeight - 2.dp.toPx()
                            }

                        // Baseline segment that connects seamlessly with adjacent cells
                        drawLine(
                            color = neutralColor,
                            start = Offset(0f, zeroY),
                            end = Offset(canvasWidth, zeroY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
                        )

                        val isPositive = point.value >= 0
                        val availableUp = (zeroY - 4.dp.toPx()).coerceAtLeast(2.dp.toPx())
                        val availableDown = (canvasHeight - zeroY - 4.dp.toPx()).coerceAtLeast(2.dp.toPx())

                        val maxAvailable = if (isPositive) availableUp else availableDown
                        val rawBarHeight = ((abs(point.value) / maxAbs) * maxAvailable).toFloat()
                        val barHeight = rawBarHeight.coerceAtLeast(2.dp.toPx())

                        val top = if (isPositive) zeroY - barHeight else zeroY
                        val barColor =
                            if (isPositive) {
                                if (isSelected) positiveColor else positiveColor.copy(alpha = 0.8f)
                            } else {
                                if (isSelected) negativeColor else negativeColor.copy(alpha = 0.8f)
                            }

                        val barLeft = (canvasWidth - barWidth.toPx()) / 2f

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(barLeft, top),
                            size = Size(barWidth.toPx(), barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        )

                        if (isSelected) {
                            drawRoundRect(
                                color = onSurfaceColor,
                                topLeft = Offset(barLeft - 1.dp.toPx(), top - 1.dp.toPx()),
                                size = Size(barWidth.toPx() + 2.dp.toPx(), barHeight + 2.dp.toPx()),
                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                                style = Stroke(width = 1.5.dp.toPx()),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = point.periodLabel,
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    textAlign = TextAlign.Center,
                )

                if (point.subLabel.isNotEmpty()) {
                    Text(
                        text = point.subLabel,
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
