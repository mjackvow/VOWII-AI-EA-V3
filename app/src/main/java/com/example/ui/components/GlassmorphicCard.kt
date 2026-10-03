package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SurfaceCardBorder

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SurfaceCardBorder,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x1AFFFFFF), // white/10
            Color(0x05FFFFFF), // white/2
            Color(0x02000000)
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF0B0E17).copy(alpha = 0.85f))
            .background(backgroundBrush)
            .border(borderWidth, borderColor, shape)
            .padding(18.dp),
        content = content
    )
}

