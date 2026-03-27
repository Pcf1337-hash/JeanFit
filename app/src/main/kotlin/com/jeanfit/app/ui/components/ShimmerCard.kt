package com.jeanfit.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jeanfit.app.ui.theme.DarkSurface
import com.jeanfit.app.ui.theme.DarkSurfaceVariant

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        DarkSurface,
        DarkSurfaceVariant,
        DarkSurface
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.fillMaxWidth(0.6f).height(16.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)))
        Box(Modifier.fillMaxWidth().height(12.dp).background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(4.dp)))
        Box(Modifier.fillMaxWidth(0.8f).height(12.dp).background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(4.dp)))
    }
}
