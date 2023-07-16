package com.ramitsuri.expensereports.android.ui.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PieChart(values: List<Value>) {
    val totalAnimationDuration = 3000L
    val animateFloats = values.map { Animatable(0f) }
    LaunchedEffect(animateFloats, values) {
        values.forEachIndexed { index, value ->
            val animationDuration = (totalAnimationDuration * value.sharePercent) / 100
            animateFloats[index].animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = animationDuration.toInt(),
                    easing = LinearEasing
                )
            )
        }
    }
    val sweepAngles = values.map { value ->
        (360 * value.sharePercent) / 100
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radius = size.width / 3
        var startAngle = 0f
        values.forEachIndexed { index, value ->
            drawArc(
                color = value.color,
                startAngle = startAngle,
                sweepAngle = sweepAngles[index] * animateFloats[index].value,
                useCenter = false,
                topLeft = Offset((size.width - 2 * radius) / 2, (size.height - 2 * radius) / 2),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
            )
            startAngle += sweepAngles[index]
        }
    }
}

data class Value(val color: Color, val sharePercent: Float)