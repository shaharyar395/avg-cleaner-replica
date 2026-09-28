package com.replica.cleaner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.replica.cleaner.ui.theme.LocalCleanerColors

data class Segment(val value: Float, val color: Color, val label: String = "")

/**
 * The thin multi-colour bar under "Free space" on Home. Values are raw sizes;
 * the bar normalises them and keeps a visible sliver for anything non-zero.
 */
@Composable
fun SegmentedBar(
    segments: List<Segment>,
    modifier: Modifier = Modifier,
    trackColor: Color = LocalCleanerColors.current.cardHigh,
    height: androidx.compose.ui.unit.Dp = 8.dp
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        if (total <= 0f) return@Row
        // Only draw positive segments. Applying a minimum weight to zeros created
        // the stray coloured dashes under "Free space" on Home.
        segments.filter { it.value > 0f }.forEach { segment ->
            val weight = (segment.value / total).coerceAtLeast(0.02f)
            Box(
                Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .background(segment.color)
            )
        }
    }
}

/**
 * Donut used by Media Overview and the "Unnecessary data" tip. Draws from the
 * top, clockwise, with a 2-degree gap between slices.
 */
@Composable
fun DonutChart(
    segments: List<Segment>,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 18.dp,
    trackColor: Color = LocalCleanerColors.current.cardHigh,
    center: @Composable (() -> Unit)? = null
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    val sweepFactor by animateFloatAsState(
        targetValue = if (total > 0f) 1f else 0f,
        animationSpec = tween(700),
        label = "donut"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxWidth().fillMaxHeight()) {
            val stroke = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - stroke
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            if (total <= 0f) return@Canvas

            var start = -90f
            segments.forEach { segment ->
                val sweep = (segment.value / total) * 360f * sweepFactor
                if (sweep > 0.5f) {
                    drawArc(
                        color = segment.color,
                        startAngle = start + 1f,
                        sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                start += sweep
            }
        }
        center?.invoke()
    }
}

/** The big percentage ring on the scanning screen. */
@Composable
fun RingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 6.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = LocalCleanerColors.current.cardHigh,
    center: @Composable (() -> Unit)? = null
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "ring"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxWidth().fillMaxHeight()) {
            val stroke = strokeWidth.toPx()
            val diameter = minOf(size.width, size.height) - stroke
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        center?.invoke()
    }
}

/** Weekly screen-time bars on Apps Overview. */
@Composable
fun WeekBarChart(
    values: List<Pair<String, Long>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val max = values.maxOfOrNull { it.second } ?: 0L
    val colors = LocalCleanerColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEach { (label, value) ->
            val fraction = if (max == 0L) 0f else (value.toFloat() / max)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .width(8.dp)
                        .height((4 + 84 * fraction).dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (value == 0L) colors.divider else barColor)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

/** Two-tone meter used on the System Info screen for RAM and storage. */
@Composable
fun MeterBar(
    usedFraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val colors = LocalCleanerColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.divider)
    ) {
        Box(
            Modifier
                .fillMaxWidth(usedFraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

/** The square stat tiles on Apps Overview (86 Installed / 27 System / 46%). */
@Composable
fun StatTile(
    value: String,
    caption: String,
    background: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = Color(0xFF0E2119),
    captionColor: Color = Color(0xFF0E2119),
    secondary: String? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            color = valueColor
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium,
            color = captionColor
        )
        if (secondary != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = secondary,
                style = MaterialTheme.typography.headlineSmall,
                color = valueColor
            )
        }
    }
}

@Composable
fun Dot(color: Color, size: androidx.compose.ui.unit.Dp = 8.dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .background(color)
    )
}
