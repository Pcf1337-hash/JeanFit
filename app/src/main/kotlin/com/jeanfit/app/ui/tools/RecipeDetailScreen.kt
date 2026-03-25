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
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.*
import org.json.JSONArray

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    LaunchedEffect(recipeId) { viewModel.selectRecipe(recipeId) }
    val state by viewModel.state.collectAsState()
    val recipe = state.selectedRecipe

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "Rezept", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") } },
                actions = {
                    recipe?.let {
                        IconButton(onClick = { viewModel.toggleFavorite(it.recipeId, it.isFavorite) }) {
                            Icon(
                                if (it.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                "Favorit",
                                tint = if (it.isFavorite) SunsetOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (recipe == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SunsetOrange)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        recipe.description?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            NutrientColumn("Kalorien", "${recipe.totalCaloriesPerServing.toInt()}", "kcal")
                            NutrientColumn("Protein", "${recipe.proteinPerServing.toInt()}", "g")
                            NutrientColumn("Kohlenhydrate", "${recipe.carbsPerServing.toInt()}", "g")
                            NutrientColumn("Fett", "${recipe.fatPerServing.toInt()}", "g")
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                            if (recipe.greenPercent > 0) Box(Modifier.weight(recipe.greenPercent).background(FoodGreen))
                            if (recipe.yellowPercent > 0) Box(Modifier.weight(recipe.yellowPercent).background(FoodYellow))
                            if (recipe.orangePercent > 0) Box(Modifier.weight(recipe.orangePercent).background(FoodOrange))
                        }
                    }
                }
            }

            item {
                // Timing
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoChip(Icons.Filled.Timer, "Vorbereitung: ${recipe.prepTimeMinutes} min")
                    InfoChip(Icons.Filled.LocalFireDepartment, "Kochen: ${recipe.cookTimeMinutes} min")
                }
            }

            item {
                // Ingredients
                Text("Zutaten (${recipe.servings} Person${if (recipe.servings > 1) "en" else ""})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        parseIngredients(recipe.ingredientsJson).forEach { (name, amount, unit) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                                Text("$amount $unit", style = MaterialTheme.typography.bodyMedium, color = SunsetOrange, fontWeight = FontWeight.Medium)
                            }
                            if (name != parseIngredients(recipe.ingredientsJson).last().first) {
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            item {
                Text("Zubereitung", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        parseSteps(recipe.stepsJson).forEach { (step, instruction) ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = SunsetOrange,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$step", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(instruction, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientColumn(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SunsetOrange)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = SunsetOrange)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun parseIngredients(json: String): List<Triple<String, String, String>> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Triple(obj.getString("name"), obj.getString("amount"), obj.getString("unit"))
        }
    } catch (e: Exception) { emptyList() }
}

private fun parseSteps(json: String): List<Pair<Int, String>> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            Pair(obj.getInt("step"), obj.getString("instruction"))
        }
    } catch (e: Exception) { emptyList() }
}

@Preview(showBackground = true)
@Composable
private fun RecipeDetailPreview() {
    JeanFitTheme { RecipeDetailScreen(recipeId = "r1", onBack = {}) }
}
