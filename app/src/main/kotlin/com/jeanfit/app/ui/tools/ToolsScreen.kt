package com.jeanfit.app.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jeanfit.app.ui.theme.*

@Composable
fun ToolsScreen(
    onNavigateRecipes: () -> Unit,
    onNavigateMealPlanner: () -> Unit,
    onLogFood: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tools", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Ernährungs-Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                ToolCard(
                    icon = Icons.Filled.Search,
                    title = "Lebensmittel suchen",
                    subtitle = "Nährstoffe nachschlagen und loggen",
                    color = SunsetOrange,
                    onClick = onLogFood
                )
            }
            item {
                ToolCard(
                    icon = Icons.Filled.MenuBook,
                    title = "Rezeptbuch",
                    subtitle = "Gesunde JeanFit-Rezepte entdecken",
                    color = FoodGreen,
                    onClick = onNavigateRecipes
                )
            }
            item {
                ToolCard(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Mahlzeitenplaner",
                    subtitle = "Plane deine Woche im Voraus",
                    color = BlueDianne,
                    onClick = onNavigateMealPlanner
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Farbsystem verstehen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                ColorSystemCard()
            }
        }
    }
}

@Composable
private fun ToolCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ColorSystemCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Das Ampelsystem", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            ColorSystemRow(
                color = FoodGreen,
                label = "Grün",
                description = "≤ 100 kcal/100g • Unbegrenzt essen",
                examples = "Gemüse, Obst, Hülsenfrüchte"
            )
            ColorSystemRow(
                color = FoodYellow,
                label = "Gelb",
                description = "101–240 kcal/100g • Maßvoll genießen",
                examples = "Vollkorn, Hühnchen, Fisch, Milch"
            )
            ColorSystemRow(
                color = FoodOrange,
                label = "Orange",
                description = "> 240 kcal/100g • Bewusst einsetzen",
                examples = "Nüsse, Öle, Käse, Süßes"
            )
        }
    }
}

@Composable
private fun ColorSystemRow(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    description: String,
    examples: String
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, MaterialTheme.shapes.extraSmall)
                .padding(top = 2.dp)
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = color)
            Text(description, style = MaterialTheme.typography.bodySmall)
            Text(examples, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolsPreview() {
    JeanFitTheme { ToolsScreen(onNavigateRecipes = {}, onNavigateMealPlanner = {}, onLogFood = {}) }
}
