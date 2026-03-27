package com.jeanfit.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object JeanFitGradients {
    val heroGradient = Brush.verticalGradient(
        listOf(Color(0xFF1565C0), Color(0xFF0D2B4E))
    )
    val cardGradient = Brush.linearGradient(
        listOf(Color(0xFF1A2E45), Color(0xFF0D2B4E))
    )
    val progressRing = Brush.sweepGradient(
        listOf(Color(0xFF42A5F5), Color(0xFF00BCD4), Color(0xFF1565C0))
    )
    val foodGreenGradient = Brush.linearGradient(
        listOf(Color(0xFF2ECC71), Color(0xFF27AE60))
    )
    val coinGradient = Brush.linearGradient(
        listOf(Color(0xFFFFD700), Color(0xFFFFA000))
    )
    val primaryGradient = Brush.linearGradient(
        listOf(Color(0xFF1565C0), Color(0xFF00BCD4))
    )
    val streakGradient = Brush.linearGradient(
        listOf(Color(0xFFFF6B35), Color(0xFFFF8C00))
    )
}
