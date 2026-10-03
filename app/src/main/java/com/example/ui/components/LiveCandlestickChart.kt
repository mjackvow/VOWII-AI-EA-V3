package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkGridLine
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed

@Composable
fun LiveCandlestickChart(
    priceHistory: List<Double>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0C0F17))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw grid lines
            val rows = 4
            for (i in 1 until rows) {
                val y = height * (i.toFloat() / rows)
                drawLine(
                    color = DarkGridLine,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            if (priceHistory.size < 2) return@Canvas

            val minPrice = priceHistory.minOrNull() ?: 1.0
            val maxPrice = priceHistory.maxOrNull() ?: 2.0
            val priceRange = if (maxPrice == minPrice) 1.0 else maxPrice - minPrice

            val stepX = width / (priceHistory.size - 1)

            // Calculate point coordinates
            val points = priceHistory.mapIndexed { idx, price ->
                val x = idx * stepX
                val normalizedY = (price - minPrice) / priceRange
                val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
                Offset(x.toFloat(), y.toFloat())
            }

            // Draw Area Fill / Trend Line
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            val isBullish = priceHistory.last() >= priceHistory.first()
            val lineColor = if (isBullish) NeonGreen else NeonRed

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3f)
            )

            // Draw candlesticks / price nodes for recent ticks
            points.takeLast(12).forEachIndexed { index, pt ->
                val isUp = index % 2 == 0
                val barColor = if (isUp) NeonGreen else NeonRed
                drawRect(
                    color = barColor,
                    topLeft = Offset(pt.x - 3f, pt.y - 10f),
                    size = Size(6f, 20f)
                )
            }

            // EMA indicator line overlay
            val emaPath = Path()
            var ema = points.first().y
            emaPath.moveTo(points.first().x, ema)
            points.forEach { pt ->
                ema = (pt.y * 0.2f) + (ema * 0.8f)
                emaPath.lineTo(pt.x, ema)
            }

            drawPath(
                path = emaPath,
                color = NeonCyan.copy(alpha = 0.7f),
                style = Stroke(width = 1.5f)
            )
        }
    }
}
