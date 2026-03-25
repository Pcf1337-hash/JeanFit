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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jeanfit.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MealPlannerScreen(onBack: () -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val formatter = DateTimeFormatter.ofPattern("EEE, dd.MM")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mahlzeitenplaner", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)
        ) {
            // Week navigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedDate = selectedDate.minusDays(1) }) {
                    Icon(Icons.Filled.ChevronLeft, "Vorheriger Tag")
                }
                Text(
                    if (selectedDate == LocalDate.now()) "Heute" else selectedDate.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { selectedDate = selectedDate.plusDays(1) }) {
                    Icon(Icons.Filled.ChevronRight, "Nächster Tag")
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val mealTypes = listOf(
                    "breakfast" to "Frühstück",
                    "lunch" to "Mittagessen",
                    "dinner" to "Abendessen",
                    "snack" to "Snack"
                )
                items(mealTypes.size) { index ->
                    val (type, label) = mealTypes[index]
                    MealPlanCard(mealType = type, label = label)
                }
            }
        }
    }
}

@Composable
private fun MealPlanCard(mealType: String, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Noch nicht geplant", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Add, contentDescription = "Rezept hinzufügen", tint = SunsetOrange)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealPlannerPreview() {
    JeanFitTheme { MealPlannerScreen(onBack = {}) }
}
