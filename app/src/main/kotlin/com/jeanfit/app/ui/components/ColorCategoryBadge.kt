package com.jeanfit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeanfit.app.ui.theme.FoodGreen
import com.jeanfit.app.ui.theme.FoodOrange
import com.jeanfit.app.ui.theme.FoodYellow

@Composable
fun ColorCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, emoji) = when (category) {
        "green"  -> Quadruple(FoodGreen.copy(alpha = 0.2f), FoodGreen, "Grün", "🟢")
        "yellow" -> Quadruple(FoodYellow.copy(alpha = 0.2f), FoodYellow, "Gelb", "🟡")
        "orange" -> Quadruple(FoodOrange.copy(alpha = 0.2f), FoodOrange, "Orange", "🟠")
        else     -> Quadruple(Color.Gray.copy(alpha = 0.2f), Color.Gray, "—", "⬜")
    }

    Text(
        text = "$emoji $label",
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
    )
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
