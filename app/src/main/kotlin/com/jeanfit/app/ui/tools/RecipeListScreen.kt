package com.jeanfit.app.ui.tools

import androidx.compose.foundation.background
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

@Composable
fun RecipeListScreen(
    onBack: () -> Unit,
    onRecipeDetail: (String) -> Unit,
    viewModel: RecipeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rezepte", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Rezepte suchen...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SunsetOrange)
                }
            } else if (state.recipes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.MenuBook, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        Text("Keine Rezepte gefunden", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recipes) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeDetail(recipe.recipeId) },
                            onFavorite = { viewModel.toggleFavorite(recipe.recipeId, recipe.isFavorite) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit, onFavorite: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(recipe.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    recipe.description?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
                IconButton(onClick = onFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (recipe.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorit",
                        tint = if (recipe.isFavorite) SunsetOrange else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RecipeStat(Icons.Filled.LocalFireDepartment, "${recipe.totalCaloriesPerServing.toInt()} kcal")
                RecipeStat(Icons.Filled.Timer, "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min")
                RecipeStat(Icons.Filled.People, "${recipe.servings} Person${if (recipe.servings > 1) "en" else ""}")
            }
            Spacer(Modifier.height(8.dp))
            // Color bar
            Row(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                if (recipe.greenPercent > 0) Box(Modifier.weight(recipe.greenPercent).background(FoodGreen))
                if (recipe.yellowPercent > 0) Box(Modifier.weight(recipe.yellowPercent).background(FoodYellow))
                if (recipe.orangePercent > 0) Box(Modifier.weight(recipe.orangePercent).background(FoodOrange))
            }
        }
    }
}

@Composable
private fun RecipeStat(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = SunsetOrange)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeListPreview() {
    JeanFitTheme { RecipeListScreen(onBack = {}, onRecipeDetail = {}) }
}
