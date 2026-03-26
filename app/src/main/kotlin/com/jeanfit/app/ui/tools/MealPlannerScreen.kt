package com.jeanfit.app.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.data.db.entities.Recipe
import com.jeanfit.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MealPlannerScreen(
    onBack: () -> Unit,
    viewModel: MealPlannerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("EEE, dd.MM")

    // Rezept-Picker Dialog
    if (state.isPickerOpen) {
        RecipePickerDialog(
            recipes = state.allRecipes,
            mealType = state.pickerMealType,
            onDismiss = viewModel::closePicker,
            onSelect = viewModel::assignRecipe
        )
    }

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
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Tagesnavigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::prevDay) {
                    Icon(Icons.Filled.ChevronLeft, "Vorheriger Tag")
                }
                Text(
                    when (state.selectedDate) {
                        LocalDate.now() -> "Heute"
                        LocalDate.now().plusDays(1) -> "Morgen"
                        LocalDate.now().minusDays(1) -> "Gestern"
                        else -> state.selectedDate.format(formatter)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = viewModel::nextDay) {
                    Icon(Icons.Filled.ChevronRight, "Nächster Tag")
                }
            }

            // Mahlzeiten-Slots
            val mealTypes = listOf(
                "breakfast" to "🌅 Frühstück",
                "lunch" to "☀️ Mittagessen",
                "dinner" to "🌙 Abendessen",
                "snack" to "🍎 Snack"
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mealTypes) { (type, label) ->
                    val plan = state.mealPlans.find { it.mealType == type }
                    val recipe = plan?.recipeId?.let { id -> state.allRecipes.find { it.recipeId == id } }

                    MealPlanSlot(
                        label = label,
                        recipe = recipe,
                        customName = plan?.customMealName,
                        onAdd = { viewModel.openPicker(type) },
                        onRemove = { viewModel.removeMealPlan(type) }
                    )
                }

                // Tagessumme
                item {
                    val totalCal = state.mealPlans.sumOf { plan ->
                        state.allRecipes.find { it.recipeId == plan.recipeId }?.totalCaloriesPerServing?.toDouble() ?: 0.0
                    }.toInt()
                    if (totalCal > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SunsetOrange.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Geplante Kalorien", fontWeight = FontWeight.SemiBold)
                                Text("$totalCal kcal", fontWeight = FontWeight.Bold, color = SunsetOrange)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanSlot(
    label: String,
    recipe: Recipe?,
    customName: String?,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (recipe != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(recipe.title, style = MaterialTheme.typography.bodyMedium, color = SunsetOrange, fontWeight = FontWeight.Medium)
                    Text("${recipe.totalCaloriesPerServing.toInt()} kcal · ${recipe.prepTimeMinutes + recipe.cookTimeMinutes} Min.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (!customName.isNullOrBlank()) {
                    Text(customName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Noch nicht geplant", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (recipe != null || !customName.isNullOrBlank()) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Entfernen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                IconButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, contentDescription = "Rezept hinzufügen", tint = SunsetOrange)
                }
            }
        }
    }
}

@Composable
private fun RecipePickerDialog(
    recipes: List<Recipe>,
    mealType: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = recipes.filter {
        searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) ||
                it.tags.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Rezept auswählen", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Suchen...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                )
            }
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                if (filtered.isEmpty()) {
                    item {
                        Text("Keine Rezepte gefunden", color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp))
                    }
                }
                items(filtered) { recipe ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(recipe.recipeId) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("${recipe.totalCaloriesPerServing.toInt()} kcal · ${recipe.tags.split(",").firstOrNull() ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun MealPlannerPreview() {
    JeanFitTheme { MealPlannerScreen(onBack = {}) }
}
