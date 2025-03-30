package com.ramitsuri.expensereports.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

/**
 * Copied from https://github.com/ehsannarmani/ComposeCharts to be able to add additional
 * information to popup because right now it only shows the Double value that's used to draw the
 * chart
 */

fun Float.spaceBetween(
    itemCount: Int,
    index: Int,
): Float {
    if (itemCount == 1) return 0f
    val itemSize = this / (itemCount - 1)
    val positions = (0 until itemCount).map { it * itemSize }
    val result = positions[index]
    return result
}

sealed class StrokeStyle {
    data object Normal : StrokeStyle()

    data class Dashed(val intervals: FloatArray = floatArrayOf(10f, 10f), val phase: Float = 10f) :
        StrokeStyle() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            other as Dashed

            if (!intervals.contentEquals(other.intervals)) return false
            if (phase != other.phase) return false

            return true
        }

        override fun hashCode(): Int {
            var result = intervals.contentHashCode()
            result = 31 * result + phase.hashCode()
            return result
        }
    }

    val pathEffect: PathEffect?
        get() {
            return when (this) {
                is StrokeStyle.Normal -> {
                    null
                }

                is StrokeStyle.Dashed -> {
                    PathEffect.dashPathEffect(intervals = intervals, phase = phase)
                }
            }
        }
}

sealed class AnimationMode {
    data class Together(val delayBuilder: (index: Int) -> Long = { 0 }) : AnimationMode()

    data object OneByOne : AnimationMode()
}

data class DividerProperties(
    val enabled: Boolean = true,
    val xAxisProperties: LineProperties = LineProperties(),
    val yAxisProperties: LineProperties = LineProperties(),
)

data class LineProperties(
    val enabled: Boolean = true,
    val style: StrokeStyle = StrokeStyle.Normal,
    val color: Brush = SolidColor(Color.Gray),
    val thickness: Dp = (.5).dp,
)

data class GridProperties(
    val enabled: Boolean = true,
    val xAxisProperties: AxisProperties = AxisProperties(),
    val yAxisProperties: AxisProperties = AxisProperties(),
) {
    data class AxisProperties(
        val enabled: Boolean = true,
        val style: StrokeStyle = StrokeStyle.Normal,
        val color: Brush = SolidColor(Color.Gray),
        val thickness: Dp = (.5).dp,
        val lineCount: Int = 5,
    )
}

data class ZeroLineProperties(
    val enabled: Boolean = false,
    val style: StrokeStyle = StrokeStyle.Normal,
    val color: Brush = SolidColor(Color.Gray),
    val thickness: Dp = (.5).dp,
    val animationSpec: AnimationSpec<Float> = tween(durationMillis = 1000, delayMillis = 300),
    val zType: ZType = ZType.Under,
) {
    enum class ZType {
        Under,
        Above,
    }
}

sealed class IndicatorProperties(
    open val enabled: Boolean,
    open val textStyle: TextStyle,
    open val count: IndicatorCount,
    open val position: IndicatorPosition,
    open val padding: Dp,
    open val contentBuilder: (Double) -> String,
    open val indicators: List<Double> = emptyList(),
)

data class VerticalIndicatorProperties(
    override val enabled: Boolean = true,
    override val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp),
    override val count: IndicatorCount = IndicatorCount.CountBased(count = 5),
    override val position: IndicatorPosition.Vertical = IndicatorPosition.Vertical.Bottom,
    override val padding: Dp = 12.dp,
    override val contentBuilder: (Double) -> String = {
        it.toString()
    },
    override val indicators: List<Double> = emptyList(),
) : IndicatorProperties(
        enabled = enabled,
        textStyle = textStyle,
        count = count,
        position = position,
        contentBuilder = contentBuilder,
        padding = padding,
        indicators = indicators,
    )

data class HorizontalIndicatorProperties(
    override val enabled: Boolean = true,
    override val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp),
    override val count: IndicatorCount = IndicatorCount.CountBased(count = 5),
    override val position: IndicatorPosition.Horizontal = IndicatorPosition.Horizontal.Start,
    override val padding: Dp = 12.dp,
    override val contentBuilder: (Double) -> String = {
        it.toString()
    },
    override val indicators: List<Double> = emptyList(),
) : IndicatorProperties(
        enabled = enabled,
        textStyle = textStyle,
        count = count,
        position = position,
        contentBuilder = contentBuilder,
        padding = padding,
        indicators = indicators,
    )

sealed interface IndicatorPosition {
    enum class Vertical : IndicatorPosition {
        Top,
        Bottom,
    }

    enum class Horizontal : IndicatorPosition {
        Start,
        End,
    }
}

sealed class IndicatorCount {
    data class CountBased(val count: Int) : IndicatorCount()

    data class StepBased(val stepBy: Double) : IndicatorCount()
}

data class LabelHelperProperties(
    val enabled: Boolean = true,
    val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp),
)

data class PopupProperties(
    val enabled: Boolean = true,
    val animationSpec: AnimationSpec<Float> = tween(400),
    val duration: Long = 1500,
    val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp),
    val containerColor: Color = Color(0xff313131),
    val cornerRadius: Dp = 6.dp,
    val contentHorizontalPadding: Dp = 4.dp,
    val contentVerticalPadding: Dp = 4.dp,
    val mode: Mode = Mode.Normal,
    val contentBuilder: (index: Int) -> String = {
        it.toString()
    },
) {
    sealed class Mode {
        data object Normal : Mode()

        data class PointMode(val threshold: Dp = 16.dp) : Mode()
    }
}

data class DotProperties(
    val enabled: Boolean = false,
    val radius: Dp = 3.dp,
    val color: Brush = SolidColor(Color.Unspecified),
    val strokeWidth: Dp = 2.dp,
    val strokeColor: Brush = SolidColor(Color.Unspecified),
    val strokeStyle: StrokeStyle = StrokeStyle.Normal,
    val animationEnabled: Boolean = true,
    val animationSpec: AnimationSpec<Float> = tween(300),
)

data class LabelProperties(
    val enabled: Boolean,
    val textStyle: TextStyle = TextStyle.Default.copy(fontSize = 12.sp, textAlign = TextAlign.End),
    val padding: Dp = 12.dp,
    val labels: List<String> = listOf(),
    val builder: (@Composable (modifier: Modifier, label: String, shouldRotate: Boolean, index: Int) -> Unit)? = null,
    val rotation: Rotation = Rotation(),
) {
    data class Rotation(
        val mode: Mode = Mode.IfNecessary,
        val degree: Float = -45f,
        val padding: Dp? = null,
    ) {
        enum class Mode {
            Force,
            IfNecessary,
        }
    }
}

fun split(
    count: IndicatorCount,
    minValue: Double,
    maxValue: Double,
): List<Double> {
    return when (count) {
        is IndicatorCount.CountBased -> {
            val step = (maxValue - minValue) / (count.count - 1)
            val result = (0 until count.count).map { (maxValue - it * step) }
            result
        }

        is IndicatorCount.StepBased -> {
            val result = mutableListOf<Double>()
            var cache = maxValue
            while (cache > minValue) {
                result.add(cache.coerceAtLeast(minValue))
                cache -= count.stepBy
            }
            result
        }
    }
}

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    data: List<Line>,
    curvedEdges: Boolean = true,
    animationDelay: Long = 300,
    animationMode: AnimationMode = AnimationMode.Together(),
    dividerProperties: DividerProperties = DividerProperties(),
    gridProperties: GridProperties = GridProperties(),
    zeroLineProperties: ZeroLineProperties = ZeroLineProperties(),
    indicatorProperties: HorizontalIndicatorProperties =
        HorizontalIndicatorProperties(
            textStyle = TextStyle.Default,
            padding = 16.dp,
        ),
    labelHelperProperties: LabelHelperProperties = LabelHelperProperties(),
    labelHelperPadding: Dp = 26.dp,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    popupProperties: PopupProperties =
        PopupProperties(
            textStyle =
                TextStyle.Default.copy(
                    color = Color.White,
                    fontSize = 12.sp,
                ),
        ),
    dotsProperties: DotProperties = DotProperties(),
    labelProperties: LabelProperties = LabelProperties(enabled = false),
    maxValue: Double = data.maxOfOrNull { it.values.maxOfOrNull { it } ?: 0.0 } ?: 0.0,
    minValue: Double =
        if (data.any { it.values.any { it < 0.0 } }) {
            data.minOfOrNull {
                it.values.minOfOrNull { it } ?: 0.0
            } ?: 0.0
        } else {
            0.0
        },
) {
    if (data.isNotEmpty()) {
        require(minValue <= (data.minOfOrNull { it.values.minOfOrNull { it } ?: 0.0 } ?: 0.0)) {
            "Chart data must be at least $minValue (Specified Min Value)"
        }
        require(maxValue >= (data.maxOfOrNull { it.values.maxOfOrNull { it } ?: 0.0 } ?: 0.0)) {
            "Chart data must be at most $maxValue (Specified Max Value)"
        }
    }

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val pathMeasure =
        remember {
            PathMeasure()
        }

    val popupAnimation =
        remember {
            Animatable(0f)
        }

    val zeroLineAnimation =
        remember {
            Animatable(0f)
        }
    val chartWidth =
        remember {
            mutableFloatStateOf(0f)
        }

    val dotAnimators =
        remember {
            mutableStateListOf<List<Animatable<Float, AnimationVector1D>>>()
        }
    val popups =
        remember {
            mutableStateListOf<Popup>()
        }
    val popupsOffsetAnimators =
        remember {
            mutableStateListOf<Pair<Animatable<Float, AnimationVector1D>, Animatable<Float, AnimationVector1D>>>()
        }
    val linesPathData =
        remember {
            mutableStateListOf<com.ramitsuri.expensereports.ui.components.PathData>()
        }
    val indicators =
        remember(indicatorProperties.indicators, minValue, maxValue) {
            indicatorProperties.indicators.ifEmpty {
                split(
                    count = indicatorProperties.count,
                    minValue = minValue,
                    maxValue = maxValue,
                )
            }
        }
    val indicatorAreaWidth =
        remember {
            if (indicatorProperties.enabled) {
                indicators.maxOf {
                    textMeasurer.measure(indicatorProperties.contentBuilder(it)).size.width
                } + (indicatorProperties.padding.value * density.density)
            } else {
                0f
            }
        }

    val xPadding =
        remember {
            if (indicatorProperties.enabled && indicatorProperties.position == IndicatorPosition.Horizontal.Start) {
                indicatorAreaWidth
            } else {
                0f
            }
        }
    LaunchedEffect(Unit) {
        if (zeroLineProperties.enabled) {
            zeroLineAnimation.snapTo(0f)
            zeroLineAnimation.animateTo(1f, animationSpec = zeroLineProperties.animationSpec)
        }
    }

    // make animators
    LaunchedEffect(data) {
        dotAnimators.clear()
        launch {
            data.forEach {
                val animators = mutableListOf<Animatable<Float, AnimationVector1D>>()
                it.values.forEach {
                    animators.add(Animatable(0f))
                }
                dotAnimators.add(animators)
            }
        }
    }

    // animate
    LaunchedEffect(data) {
        delay(animationDelay)

        val animateStroke: suspend (Line) -> Unit = { line ->
            line.strokeProgress.animateTo(1f, animationSpec = line.strokeAnimationSpec)
        }
        val animateGradient: suspend (Line) -> Unit = { line ->
            delay(line.gradientAnimationDelay)
            line.gradientProgress.animateTo(1f, animationSpec = line.gradientAnimationSpec)
        }
        launch {
            data.forEachIndexed { index, line ->
                when (animationMode) {
                    is AnimationMode.OneByOne -> {
                        animateStroke(line)
                    }

                    is AnimationMode.Together -> {
                        launch {
                            delay(animationMode.delayBuilder(index))
                            animateStroke(line)
                        }
                    }
                }
            }
        }
        launch {
            data.forEachIndexed { index, line ->
                when (animationMode) {
                    is AnimationMode.OneByOne -> {
                        animateGradient(line)
                    }

                    is AnimationMode.Together -> {
                        launch {
                            delay(animationMode.delayBuilder(index))
                            animateGradient(line)
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(data, minValue, maxValue) {
        linesPathData.clear()
    }

    Column(modifier = modifier) {
        if (labelHelperProperties.enabled) {
            LabelHelper(
                data = data.map { it.label to it.color },
                textStyle = labelHelperProperties.textStyle,
            )
            Spacer(modifier = Modifier.height(labelHelperPadding))
        }
        Row(modifier = Modifier.fillMaxSize().weight(1f)) {
            if (indicatorProperties.enabled) {
                if (indicatorProperties.position == IndicatorPosition.Horizontal.Start) {
                    Indicators(
                        indicatorProperties = indicatorProperties,
                        indicators = indicators,
                    )
                    Spacer(modifier = Modifier.width(indicatorProperties.padding))
                }
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Canvas(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .pointerInput(data, minValue, maxValue, linesPathData) {
                                if (!popupProperties.enabled) return@pointerInput
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            popupAnimation.animateTo(0f, animationSpec = tween(500))
                                            popups.clear()
                                            popupsOffsetAnimators.clear()
                                        }
                                    },
                                    onHorizontalDrag = { change, amount ->
                                        val _size =
                                            size.toSize()
                                                .copy(height = (size.height).toFloat())
                                        popups.clear()
                                        data.forEachIndexed { index, line ->
                                            val properties = line.popupProperties ?: popupProperties

                                            val positionX =
                                                (change.position.x).coerceIn(
                                                    0f,
                                                    size.width.toFloat(),
                                                )
                                            val pathData = linesPathData[index]

                                            if (positionX >= pathData.xPositions[pathData.startIndex] && positionX <= pathData.xPositions[pathData.endIndex]) {
                                                val showOnPointsThreshold =
                                                    (
                                                        (properties.mode as? PopupProperties.Mode.PointMode)?.threshold
                                                            ?: 0.dp
                                                    ).toPx()
                                                val pointX =
                                                    pathData.xPositions.find { it in positionX - showOnPointsThreshold..positionX + showOnPointsThreshold }

                                                if (properties.mode !is PopupProperties.Mode.PointMode || pointX != null) {
                                                    val fraction =
                                                        (
                                                            (
                                                                if (properties.mode is PopupProperties.Mode.PointMode) {
                                                                    (
                                                                        pointX?.toFloat()
                                                                            ?: 0f
                                                                    )
                                                                } else {
                                                                    positionX
                                                                }
                                                            ) / size.width
                                                        )
                                                    val popupValue =
                                                        getPopupValue(
                                                            points = line.values,
                                                            fraction = fraction.toDouble(),
                                                            rounded = line.curvedEdges ?: curvedEdges,
                                                            size = _size,
                                                            minValue = minValue,
                                                            maxValue = maxValue,
                                                        )
                                                    popups.add(
                                                        Popup(
                                                            position = popupValue.offset,
                                                            index =
                                                                calculateValueIndex(
                                                                    fraction = fraction.toDouble(),
                                                                    values = line.values,
                                                                    pathData = pathData,
                                                                ),
                                                            properties = properties,
                                                        ),
                                                    )

                                                    if (popupsOffsetAnimators.count() < popups.count()) {
                                                        repeat(popups.count() - popupsOffsetAnimators.count()) {
                                                            popupsOffsetAnimators.add(
                                                                // add fixed position for popup when mode is point mode
                                                                if (properties.mode is PopupProperties.Mode.PointMode) {
                                                                    Animatable(popupValue.offset.x) to
                                                                        Animatable(
                                                                            popupValue.offset.y,
                                                                        )
                                                                } else {
                                                                    Animatable(0f) to Animatable(0f)
                                                                },
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        scope.launch {
                                            // animate popup (alpha)
                                            if (popupAnimation.value != 1f && !popupAnimation.isRunning) {
                                                popupAnimation.animateTo(1f, animationSpec = tween(500))
                                            }
                                        }
                                    },
                                )
                            },
                ) {
                    val chartAreaHeight = size.height
                    chartWidth.value = size.width
                    val drawZeroLine = {
                        val zeroY =
                            chartAreaHeight -
                                calculateOffset(
                                    minValue = minValue,
                                    maxValue = maxValue,
                                    total = chartAreaHeight,
                                    value = 0f,
                                ).toFloat()
                        drawLine(
                            brush = zeroLineProperties.color,
                            start = Offset(x = 0f, y = zeroY),
                            end = Offset(x = size.width * zeroLineAnimation.value, y = zeroY),
                            pathEffect = zeroLineProperties.style.pathEffect,
                            strokeWidth = zeroLineProperties.thickness.toPx(),
                        )
                    }
                    if (linesPathData.isEmpty() || linesPathData.count() != data.count()) {
                        data.map {
                            val startIndex =
                                if (it.viewRange.startIndex < 0 || it.viewRange.startIndex >= it.values.size - 1) 0 else it.viewRange.startIndex
                            val endIndex =
                                if (it.viewRange.endIndex < 0 || it.viewRange.endIndex <= it.viewRange.startIndex ||
                                    it.viewRange.endIndex > it.values.size - 1
                                ) {
                                    it.values.size - 1
                                } else {
                                    it.viewRange.endIndex
                                }

                            getLinePath(
                                dataPoints = it.values.map { it.toFloat() },
                                maxValue = maxValue.toFloat(),
                                minValue = minValue.toFloat(),
                                rounded = it.curvedEdges ?: curvedEdges,
                                size = size.copy(height = chartAreaHeight),
                                startIndex,
                                endIndex,
                            )
                        }.also {
                            linesPathData.addAll(it)
                        }
                    }

                    drawGridLines(
                        dividersProperties = dividerProperties,
                        indicatorPosition = indicatorProperties.position,
                        xAxisProperties = gridProperties.xAxisProperties,
                        yAxisProperties = gridProperties.yAxisProperties,
                        size = size.copy(height = chartAreaHeight),
                        gridEnabled = gridProperties.enabled,
                    )
                    if (zeroLineProperties.enabled && zeroLineProperties.zType == ZeroLineProperties.ZType.Under) {
                        drawZeroLine()
                    }
                    data.forEachIndexed { index, line ->
                        val pathData = linesPathData.getOrNull(index) ?: return@Canvas
                        val segmentedPath = Path()
                        pathMeasure.setPath(pathData.path, false)
                        pathMeasure.getSegment(
                            0f,
                            pathMeasure.length * line.strokeProgress.value,
                            segmentedPath,
                        )
                        var pathEffect: PathEffect? = null
                        val stroke: Float =
                            when (val drawStyle = line.drawStyle) {
                                is DrawStyle.Fill -> {
                                    0f
                                }

                                is DrawStyle.Stroke -> {
                                    pathEffect = drawStyle.strokeStyle.pathEffect
                                    drawStyle.width.toPx()
                                }
                            }
                        drawPath(
                            path = segmentedPath,
                            brush = line.color,
                            style = Stroke(width = stroke, pathEffect = pathEffect),
                        )

                        var startOffset = 0f
                        var endOffset = size.width
                        if (pathData.startIndex > 0) {
                            startOffset = pathData.xPositions[pathData.startIndex].toFloat()
                        }

                        if (pathData.endIndex < line.values.size - 1) {
                            endOffset = pathData.xPositions[pathData.endIndex].toFloat()
                        }

                        if (line.firstGradientFillColor != null && line.secondGradientFillColor != null) {
                            drawLineGradient(
                                path = pathData.path,
                                color1 = line.firstGradientFillColor,
                                color2 = line.secondGradientFillColor,
                                progress = line.gradientProgress.value,
                                size = size.copy(height = chartAreaHeight),
                                startOffset,
                                endOffset,
                            )
                        } else if (line.drawStyle is DrawStyle.Fill) {
                            var fillColor = Color.Unspecified
                            if (line.color is SolidColor) {
                                fillColor = line.color.value
                            }
                            drawLineGradient(
                                path = pathData.path,
                                color1 = fillColor,
                                color2 = fillColor,
                                progress = 1f,
                                size = size.copy(height = chartAreaHeight),
                                startOffset,
                                endOffset,
                            )
                        }

                        if ((line.dotProperties?.enabled ?: dotsProperties.enabled)) {
                            drawDots(
                                dataPoints =
                                    line.values.mapIndexed { mapIndex, value ->
                                        (
                                            dotAnimators.getOrNull(
                                                index,
                                            )?.getOrNull(mapIndex) ?: Animatable(0f)
                                        ) to value.toFloat()
                                    },
                                properties = line.dotProperties ?: dotsProperties,
                                linePath = segmentedPath,
                                maxValue = maxValue.toFloat(),
                                minValue = minValue.toFloat(),
                                pathMeasure = pathMeasure,
                                scope = scope,
                                size = size.copy(height = chartAreaHeight),
                                startIndex = pathData.startIndex,
                                endIndex = pathData.endIndex,
                            )
                        }
                    }
                    if (zeroLineProperties.enabled && zeroLineProperties.zType == ZeroLineProperties.ZType.Above) {
                        drawZeroLine()
                    }
                    popups.forEachIndexed { index, popup ->
                        drawPopup(
                            popup = popup,
                            nextPopup = popups.getOrNull(index + 1),
                            textMeasurer = textMeasurer,
                            scope = scope,
                            progress = popupAnimation.value,
                            offsetAnimator = popupsOffsetAnimators.getOrNull(index),
                        )
                    }
                }
            }
            if (indicatorProperties.enabled) {
                if (indicatorProperties.position == IndicatorPosition.Horizontal.End) {
                    Spacer(modifier = Modifier.width(indicatorProperties.padding))
                    Indicators(
                        indicatorProperties = indicatorProperties,
                        indicators = indicators,
                    )
                }
            }
        }
        HorizontalLabels(
            labelProperties = labelProperties,
            labels = labelProperties.labels,
            indicatorProperties = indicatorProperties,
            chartWidth = chartWidth.value,
            density = density,
            textMeasurer = textMeasurer,
            xPadding = xPadding,
        )
    }
}

private fun calculateValueIndex(
    fraction: Double,
    values: List<Double>,
    pathData: PathData,
): Int {
    val xPosition = (fraction * pathData.path.getBounds().width).toFloat()
    val closestXIndex =
        pathData.xPositions.indexOfFirst { x ->
            x >= xPosition
        }
    return if (closestXIndex >= 0) closestXIndex else values.size - 1
}

internal data class Value(
    val calculatedValue: Double,
    val offset: Offset,
)

internal fun getPopupValue(
    points: List<Double>,
    fraction: Double,
    rounded: Boolean = false,
    size: Size,
    minValue: Double,
    maxValue: Double,
): Value {
    val index = fraction * (points.count() - 1)
    val roundedIndex = floor(index).toInt()
    return if (fraction == 1.0) {
        val lastPoint = points.last()
        val offset =
            Offset(
                x = size.width,
                y =
                    size.height -
                        calculateOffset(
                            minValue = minValue,
                            maxValue = maxValue,
                            total = size.height,
                            value = lastPoint.toFloat(),
                        ).toFloat(),
            )
        Value(calculatedValue = points.last(), offset = offset)
    } else {
        if (rounded && points.count() > 1) {
            val calculateHeight = { value: Double ->
                calculateOffset(
                    maxValue = maxValue,
                    minValue = minValue,
                    total = size.height,
                    value = value.toFloat(),
                )
            }
            val x1 = (roundedIndex * (size.width / (points.size - 1)))
            val x2 = ((roundedIndex + 1) * (size.width / (points.size - 1)))
            val y1 = size.height - calculateHeight(points[roundedIndex])
            val y2 = size.height - calculateHeight(points[roundedIndex + 1])
            val cx = (x1 + x2) / 2f

            val areaFraction = roundedIndex.toDouble() / (points.size - 1)

            val t = (fraction - areaFraction) * (points.size - 1)

            val outputY =
                (
                    (1 - t).pow(3) * (y1) +
                        3 * t * (1 - t).pow(2) * (y1) +
                        3 * (1 - t) * t.pow(2) * (y2) +
                        t.pow(3) * y2
                ).toFloat()
            val outputX =
                (
                    (1 - t).pow(3) * (x1) +
                        3 * t * (1 - t).pow(2) * (cx) +
                        3 * (1 - t) * t.pow(2) * (cx) +
                        t.pow(3) * x2
                ).toFloat()
            val calculatedValue =
                calculateValue(
                    minValue = minValue,
                    maxValue = maxValue,
                    total = size.height,
                    offset = size.height - outputY,
                )

            Value(calculatedValue = calculatedValue, offset = Offset(x = outputX, y = outputY))
        } else {
            val p1 = points[roundedIndex]
            val p2 = points.getOrNull(roundedIndex + 1) ?: p1
            val calculatedValue = ((p2 - p1) * (index - roundedIndex) + p1)
            val offset =
                Offset(
                    x = if (points.count() > 1) (fraction * size.width).toFloat() else 0f,
                    y =
                        size.height -
                            calculateOffset(
                                minValue = minValue,
                                maxValue = maxValue,
                                total = size.height,
                                value = calculatedValue.toFloat(),
                            ).toFloat(),
                )
            Value(calculatedValue = calculatedValue, offset = offset)
        }
    }
}

fun DrawScope.drawGridLines(
    dividersProperties: DividerProperties,
    indicatorPosition: IndicatorPosition,
    gridEnabled: Boolean,
    xAxisProperties: GridProperties.AxisProperties,
    yAxisProperties: GridProperties.AxisProperties,
    size: Size? = null,
    xPadding: Float = 0f,
    yPadding: Float = 0f,
) {
    val _size = size ?: this.size

    val xAxisPathEffect = xAxisProperties.style.pathEffect
    val yAxisPathEffect = yAxisProperties.style.pathEffect

    if (xAxisProperties.enabled && gridEnabled) {
        for (i in 0 until xAxisProperties.lineCount) {
            val y = _size.height.spaceBetween(itemCount = xAxisProperties.lineCount, index = i)
            drawLine(
                brush = xAxisProperties.color,
                start = Offset(0f + xPadding, y + yPadding),
                end = Offset(_size.width + xPadding, y + yPadding),
                strokeWidth = xAxisProperties.thickness.toPx(),
                pathEffect = xAxisPathEffect,
            )
        }
    }
    if (yAxisProperties.enabled && gridEnabled) {
        for (i in 0 until yAxisProperties.lineCount) {
            val x = _size.width.spaceBetween(itemCount = yAxisProperties.lineCount, index = i)
            drawLine(
                brush = xAxisProperties.color,
                start = Offset(x + xPadding, 0f + yPadding),
                end = Offset(x + xPadding, _size.height + yPadding),
                strokeWidth = xAxisProperties.thickness.toPx(),
                pathEffect = yAxisPathEffect,
            )
        }
    }
    if (dividersProperties.xAxisProperties.enabled && dividersProperties.enabled) {
        val y = if (indicatorPosition == IndicatorPosition.Vertical.Top) 0f else _size.height
        drawLine(
            brush = dividersProperties.xAxisProperties.color,
            start = Offset(0f + xPadding, y + yPadding),
            end = Offset(_size.width + xPadding, y + yPadding),
            strokeWidth = dividersProperties.xAxisProperties.thickness.toPx(),
            pathEffect = dividersProperties.xAxisProperties.style.pathEffect,
        )
    }
    if (dividersProperties.yAxisProperties.enabled && dividersProperties.enabled) {
        val x = if (indicatorPosition == IndicatorPosition.Horizontal.End) _size.width else 0f
        drawLine(
            brush = dividersProperties.yAxisProperties.color,
            start = Offset(x + xPadding, 0f + yPadding),
            end = Offset(x + xPadding, _size.height + yPadding),
            strokeWidth = dividersProperties.yAxisProperties.thickness.toPx(),
            pathEffect = dividersProperties.yAxisProperties.style.pathEffect,
        )
    }
}

fun DrawScope.drawDots(
    dataPoints: List<Pair<Animatable<Float, AnimationVector1D>, Float>>,
    properties: DotProperties,
    linePath: Path,
    maxValue: Float,
    minValue: Float,
    pathMeasure: PathMeasure,
    scope: CoroutineScope,
    size: Size? = null,
    startIndex: Int,
    endIndex: Int,
) {
    val _size = size ?: this.size

    val pathEffect = properties.strokeStyle.pathEffect

    pathMeasure.setPath(linePath, false)
    val lastPosition = pathMeasure.getPosition(pathMeasure.length)
    dataPoints.forEachIndexed { valueIndex, value ->
        if (valueIndex in startIndex..endIndex) {
            val dotOffset =
                Offset(
                    x =
                        _size.width.spaceBetween(
                            itemCount = dataPoints.count(),
                            index = valueIndex,
                        ),
                    y =
                        (
                            _size.height -
                                calculateOffset(
                                    maxValue = maxValue.toDouble(),
                                    minValue = minValue.toDouble(),
                                    total = _size.height,
                                    value = value.second,
                                )
                        ).toFloat(),
                )
            if (lastPosition != Offset.Unspecified && lastPosition.x >= dotOffset.x - 20 || !properties.animationEnabled) {
                if (!value.first.isRunning && properties.animationEnabled && value.first.value != 1f) {
                    scope.launch {
                        value.first.animateTo(1f, animationSpec = properties.animationSpec)
                    }
                }

                val radius: Float
                val strokeRadius: Float
                if (properties.animationEnabled) {
                    radius =
                        (properties.radius.toPx() + properties.strokeWidth.toPx() / 2) * value.first.value
                    strokeRadius = properties.radius.toPx() * value.first.value
                } else {
                    radius = properties.radius.toPx() + properties.strokeWidth.toPx() / 2
                    strokeRadius = properties.radius.toPx()
                }
                drawCircle(
                    brush = properties.strokeColor,
                    radius = radius,
                    center = dotOffset,
                    style = Stroke(width = properties.strokeWidth.toPx(), pathEffect = pathEffect),
                )
                drawCircle(
                    brush = properties.color,
                    radius = strokeRadius,
                    center = dotOffset,
                )
            }
        }
    }
}

@Composable
fun HorizontalLabels(
    labelProperties: LabelProperties,
    labels: List<String>,
    indicatorProperties: HorizontalIndicatorProperties,
    chartWidth: Float,
    density: Density,
    textMeasurer: TextMeasurer,
    xPadding: Float,
) {
    if (labelProperties.enabled && labels.isNotEmpty()) {
        Spacer(modifier = Modifier.height(labelProperties.padding))

        val widthModifier =
            if (indicatorProperties.position == IndicatorPosition.Horizontal.End) {
                Modifier.width((chartWidth / density.density).dp)
            } else {
                Modifier.fillMaxWidth()
            }

        val labelMeasures =
            labels.map {
                textMeasurer.measure(
                    it,
                    style = labelProperties.textStyle,
                    maxLines = 1,
                )
            }
        val labelWidths = labelMeasures.map { it.size.width }
        val maxLabelWidth = labelWidths.max()
        val minLabelWidth = labelWidths.min()

        var textModifier: Modifier = Modifier
        var shouldRotate = labelProperties.rotation.mode == LabelProperties.Rotation.Mode.Force
        if ((maxLabelWidth / minLabelWidth.toDouble()) >= 1.5 && labelProperties.rotation.degree != 0f) {
            textModifier = textModifier.width((minLabelWidth / density.density).dp)
            shouldRotate = true
        }
        Row(
            modifier =
                widthModifier
                    .padding(
                        start = (xPadding / density.density).dp,
                    ),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            labels.forEachIndexed { index, label ->
                val modifier =
                    if (shouldRotate) {
                        textModifier.graphicsLayer {
                            rotationZ = labelProperties.rotation.degree
                            transformOrigin =
                                TransformOrigin(
                                    (labelMeasures[index].size.width / minLabelWidth.toFloat()),
                                    .5f,
                                )
                            translationX =
                                (-(labelMeasures[index].size.width - minLabelWidth.toFloat())) - (
                                    labelProperties.rotation.padding?.toPx()
                                        ?: (minLabelWidth / 2f)
                                )
                        }
                    } else {
                        textModifier
                    }
                if (labelProperties.builder != null) {
                    labelProperties.builder.invoke(modifier, label, shouldRotate, index)
                } else {
                    BasicText(
                        modifier = modifier,
                        text = label,
                        style = labelProperties.textStyle,
                        overflow = if (shouldRotate) TextOverflow.Visible else TextOverflow.Clip,
                        softWrap = !shouldRotate,
                    )
                }
            }
        }
    }
}

@Composable
fun LabelHelper(
    data: List<Pair<String, Brush>>,
    textStyle: TextStyle = TextStyle.Default.copy(fontSize = 13.sp),
) {
    val numberOfGridCells = min(data.size, 3)
    LazyVerticalGrid(columns = GridCells.Fixed(numberOfGridCells), modifier = Modifier) {
        items(data) { (label, color) ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color),
                )
                BasicText(
                    text = label,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

fun calculateValue(
    minValue: Double,
    maxValue: Double,
    total: Float,
    offset: Float,
): Double {
    val percentage = offset / total
    val range = maxValue - minValue
    val value = minValue + percentage * range
    return value
}

fun calculateOffset(
    maxValue: Double,
    minValue: Double,
    total: Float,
    value: Float,
): Double {
    val range = maxValue - minValue
    val percentage = (value - minValue) / range
    val offset = total * percentage
    return offset
}

internal fun DrawScope.getLinePath(
    dataPoints: List<Float>,
    maxValue: Float,
    minValue: Float,
    rounded: Boolean = true,
    size: Size? = null,
    startIndex: Int,
    endIndex: Int,
): PathData {
    val _size = size ?: this.size
    val path = Path()
    if (dataPoints.isEmpty()) {
        return PathData(
            path = path,
            xPositions = emptyList(),
            0,
            Int.MAX_VALUE,
        )
    }
    val calculateHeight = { value: Float ->
        calculateOffset(
            maxValue = maxValue.toDouble(),
            minValue = minValue.toDouble(),
            total = _size.height,
            value = value,
        )
    }

    if (startIndex == 0) {
        path.moveTo(0f, (_size.height - calculateHeight(dataPoints[0])).toFloat())
    } else {
        val x = (startIndex * (_size.width / (dataPoints.size - 1)))
        val y = _size.height - calculateHeight(dataPoints[startIndex]).toFloat()
        path.moveTo(x, y)
    }

    val xPositions = mutableListOf<Double>()
    for (i in 0 until dataPoints.size - 1) {
        val x1 = (i * (_size.width / (dataPoints.size - 1)))
        val y1 = _size.height - calculateHeight(dataPoints[i]).toFloat()
        val x2 = ((i + 1) * (_size.width / (dataPoints.size - 1)))
        val y2 = _size.height - calculateHeight(dataPoints[i + 1]).toFloat()

        if (i in startIndex..<endIndex) {
            if (rounded) {
                val cx = (x1 + x2) / 2f
                path.cubicTo(x1 = cx, y1 = y1, x2 = cx, y2 = y2, x3 = x2, y3 = y2)
            } else {
                path.cubicTo(x1, y1, x1, y1, (x1 + x2) / 2, (y1 + y2) / 2)
                path.cubicTo((x1 + x2) / 2, (y1 + y2) / 2, x2, y2, x2, y2)
            }
        }

        xPositions.add(x1.toDouble())
    }
    xPositions.add(_size.width.toDouble())
    return PathData(
        path = path,
        xPositions = xPositions,
        startIndex,
        endIndex,
    )
}

sealed class DrawStyle() {
    data class Stroke(val width: Dp = 2.dp, val strokeStyle: StrokeStyle = StrokeStyle.Normal) :
        DrawStyle()

    data object Fill : DrawStyle()

    fun getStyle(density: Float): androidx.compose.ui.graphics.drawscope.DrawStyle {
        return when (this) {
            is Stroke -> {
                return Stroke(
                    width = this.width.value * density,
                    pathEffect = this.strokeStyle.pathEffect,
                )
            }

            is Fill -> {
                return androidx.compose.ui.graphics.drawscope.Fill
            }
        }
    }
}

data class ViewRange(
    val startIndex: Int = 0,
    val endIndex: Int = Int.MAX_VALUE,
)

data class Line(
    val label: String,
    val values: List<Double>,
    val color: Brush,
    val firstGradientFillColor: Color? = null,
    val secondGradientFillColor: Color? = null,
    val drawStyle: DrawStyle = DrawStyle.Stroke(2.dp),
    val strokeAnimationSpec: AnimationSpec<Float> = tween(2000),
    val gradientAnimationSpec: AnimationSpec<Float> = tween(2000),
    val gradientAnimationDelay: Long = 1000,
    val dotProperties: DotProperties? = null,
    val popupProperties: PopupProperties? = null,
    val curvedEdges: Boolean? = null,
    val strokeProgress: Animatable<Float, AnimationVector1D> = Animatable(0f),
    val gradientProgress: Animatable<Float, AnimationVector1D> = Animatable(0f),
    val viewRange: ViewRange = ViewRange(),
)

internal fun DrawScope.drawLineGradient(
    path: Path,
    color1: Color,
    color2: Color,
    progress: Float,
    size: Size? = null,
    startOffset: Float,
    endOffset: Float,
) {
    val _size = size ?: this.size
    drawIntoCanvas {
        val p = Path()
        p.addPath(path)
        p.lineTo(endOffset, _size.height)
        p.lineTo(startOffset, _size.height)
        p.close()
        val paint = Paint()
        paint.shader =
            LinearGradientShader(
                Offset(0f, 0f),
                Offset(0f, _size.height),
                listOf(
                    color1.copy(alpha = color1.alpha * progress),
                    color2,
                ),
                tileMode = TileMode.Mirror,
            )
        it.drawPath(p, paint)
    }
}

private fun DrawScope.drawPopup(
    popup: Popup,
    nextPopup: Popup?,
    textMeasurer: TextMeasurer,
    scope: CoroutineScope,
    progress: Float,
    offsetAnimator: Pair<Animatable<Float, AnimationVector1D>, Animatable<Float, AnimationVector1D>>? = null,
) {
    val offset = popup.position
    val popupProperties = popup.properties
    val measureResult =
        textMeasurer.measure(
            popupProperties.contentBuilder(popup.index),
            style =
                popupProperties.textStyle.copy(
                    color =
                        popupProperties.textStyle.color.copy(
                            alpha = 1f * progress,
                        ),
                ),
        )
    var rectSize = measureResult.size.toSize()
    rectSize =
        rectSize.copy(
            width = (rectSize.width + (popupProperties.contentHorizontalPadding.toPx() * 2)),
            height = (rectSize.height + (popupProperties.contentVerticalPadding.toPx() * 2)),
        )

    val conflictDetected =
        ((nextPopup != null) && offset.y in nextPopup.position.y - rectSize.height..nextPopup.position.y + rectSize.height) ||
            (offset.x + rectSize.width) > size.width

    val rectOffset =
        if (conflictDetected) {
            offset.copy(x = offset.x - rectSize.width)
        } else {
            offset
        }
    offsetAnimator?.also { (x, y) ->
        if (x.value == 0f || y.value == 0f || popupProperties.mode is PopupProperties.Mode.PointMode) {
            scope.launch {
                x.snapTo(rectOffset.x)
                y.snapTo(rectOffset.y)
            }
        } else {
            scope.launch {
                x.animateTo(rectOffset.x)
            }
            scope.launch {
                y.animateTo(rectOffset.y)
            }
        }
    }
    if (offsetAnimator != null) {
        val animatedOffset =
            if (popup.properties.mode is PopupProperties.Mode.PointMode) {
                rectOffset
            } else {
                Offset(
                    x = offsetAnimator.first.value,
                    y = offsetAnimator.second.value,
                )
            }
        val rect =
            Rect(
                offset = animatedOffset,
                size = rectSize,
            )
        drawPath(
            path =
                Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect =
                                rect.copy(
                                    top = rect.top,
                                    left = rect.left,
                                ),
                            topLeft =
                                CornerRadius(
                                    if (conflictDetected) popupProperties.cornerRadius.toPx() else 0f,
                                    if (conflictDetected) popupProperties.cornerRadius.toPx() else 0f,
                                ),
                            topRight =
                                CornerRadius(
                                    if (!conflictDetected) popupProperties.cornerRadius.toPx() else 0f,
                                    if (!conflictDetected) popupProperties.cornerRadius.toPx() else 0f,
                                ),
                            bottomRight =
                                CornerRadius(
                                    popupProperties.cornerRadius.toPx(),
                                    popupProperties.cornerRadius.toPx(),
                                ),
                            bottomLeft =
                                CornerRadius(
                                    popupProperties.cornerRadius.toPx(),
                                    popupProperties.cornerRadius.toPx(),
                                ),
                        ),
                    )
                },
            color = popupProperties.containerColor,
            alpha = 1f * progress,
        )
        drawText(
            textLayoutResult = measureResult,
            topLeft =
                animatedOffset.copy(
                    x = animatedOffset.x + popupProperties.contentHorizontalPadding.toPx(),
                    y = animatedOffset.y + popupProperties.contentVerticalPadding.toPx(),
                ),
        )
    }
}

@Composable
private fun Indicators(
    modifier: Modifier = Modifier,
    indicators: List<Double>,
    indicatorProperties: HorizontalIndicatorProperties,
) {
    Column(
        modifier =
            modifier
                .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        indicators.forEach {
            BasicText(
                text = indicatorProperties.contentBuilder(it),
                style = indicatorProperties.textStyle,
            )
        }
    }
}

private data class Popup(
    val properties: PopupProperties,
    val position: Offset,
    val index: Int,
)

internal data class PathData(
    val path: Path,
    val xPositions: List<Double>,
    val startIndex: Int,
    val endIndex: Int,
)
