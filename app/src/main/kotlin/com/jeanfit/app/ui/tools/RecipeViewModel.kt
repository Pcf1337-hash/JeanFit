package com.jeanfit.app.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.entities.Recipe
import com.jeanfit.app.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val selectedRecipe: Recipe? = null
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _state = MutableStateFlow(RecipeUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (recipeRepository.getAllRecipes().first().isEmpty()) {
                seedRecipes()
            }
        }
        viewModelScope.launch {
            _query.flatMapLatest { q ->
                if (q.isBlank()) recipeRepository.getAllRecipes()
                else recipeRepository.searchRecipes(q)
            }.collect { recipes ->
                _state.update { it.copy(recipes = recipes, isLoading = false) }
            }
        }
    }

    fun setQuery(q: String) {
        _query.value = q
        _state.update { it.copy(query = q) }
    }

    fun selectRecipe(id: String) {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipeById(id)
            _state.update { it.copy(selectedRecipe = recipe) }
        }
    }

    fun toggleFavorite(id: String, current: Boolean) {
        viewModelScope.launch { recipeRepository.toggleFavorite(id, !current) }
    }

    private suspend fun seedRecipes() {
        val recipes = listOf(
            Recipe(
                recipeId = "r1",
                title = "Overnight Oats mit Beeren",
                description = "Cremige Haferflocken mit frischen Beeren — ideal zum Mitnehmen",
                prepTimeMinutes = 5,
                cookTimeMinutes = 0,
                servings = 1,
                totalCaloriesPerServing = 380f,
                proteinPerServing = 14f,
                carbsPerServing = 62f,
                fatPerServing = 8f,
                greenPercent = 30f,
                yellowPercent = 60f,
                orangePercent = 10f,
                ingredientsJson = """[{"name":"Haferflocken","amount":"80","unit":"g"},{"name":"Milch (1,5%)","amount":"200","unit":"ml"},{"name":"Griechischer Joghurt","amount":"100","unit":"g"},{"name":"Gemischte Beeren","amount":"100","unit":"g"},{"name":"Honig","amount":"1","unit":"TL"}]""",
                stepsJson = """[{"step":1,"instruction":"Haferflocken, Milch und Joghurt in ein Glas schichten."},{"step":2,"instruction":"Über Nacht im Kühlschrank quellen lassen."},{"step":3,"instruction":"Am Morgen mit Beeren und Honig toppen."}]""",
                tags = "frühstück,schnell,vorbereitung"
            ),
            Recipe(
                recipeId = "r2",
                title = "Hähnchen-Gemüse-Bowl",
                description = "Protein-reiche Lunch Bowl mit buntem Gemüse",
                prepTimeMinutes = 10,
                cookTimeMinutes = 20,
                servings = 1,
                totalCaloriesPerServing = 450f,
                proteinPerServing = 38f,
                carbsPerServing = 35f,
                fatPerServing = 12f,
                greenPercent = 45f,
                yellowPercent = 45f,
                orangePercent = 10f,
                ingredientsJson = """[{"name":"Hähnchenbrust","amount":"150","unit":"g"},{"name":"Brokkoli","amount":"150","unit":"g"},{"name":"Paprika","amount":"100","unit":"g"},{"name":"Vollkorn-Reis","amount":"60","unit":"g (roh)"},{"name":"Olivenöl","amount":"1","unit":"EL"},{"name":"Gewürze","amount":"nach","unit":"Geschmack"}]""",
                stepsJson = """[{"step":1,"instruction":"Reis nach Packungsanweisung kochen."},{"step":2,"instruction":"Hähnchen in Würfel schneiden und in Öl anbraten (8-10 min)."},{"step":3,"instruction":"Gemüse hinzufügen und weitere 5-7 min garen."},{"step":4,"instruction":"Alles in einer Bowl anrichten."}]""",
                tags = "mittagessen,protein,bowl"
            ),
            Recipe(
                recipeId = "r3",
                title = "Gemüse-Linsen-Suppe",
                description = "Herzhafte Suppe mit viel Ballaststoffen und Protein",
                prepTimeMinutes = 10,
                cookTimeMinutes = 25,
                servings = 2,
                totalCaloriesPerServing = 320f,
                proteinPerServing = 18f,
                carbsPerServing = 48f,
                fatPerServing = 6f,
                greenPercent = 55f,
                yellowPercent = 40f,
                orangePercent = 5f,
                ingredientsJson = """[{"name":"Rote Linsen","amount":"150","unit":"g"},{"name":"Karotten","amount":"2","unit":"Stück"},{"name":"Sellerie","amount":"2","unit":"Stangen"},{"name":"Tomaten (dose)","amount":"400","unit":"g"},{"name":"Gemüsebrühe","amount":"800","unit":"ml"},{"name":"Knoblauch","amount":"2","unit":"Zehen"}]""",
                stepsJson = """[{"step":1,"instruction":"Gemüse würfeln und in einem Topf anschwitzen."},{"step":2,"instruction":"Linsen, Tomaten und Brühe hinzufügen."},{"step":3,"instruction":"25 Minuten köcheln lassen bis Linsen weich."},{"step":4,"instruction":"Mit Salz, Pfeffer und Kreuzkümmel abschmecken."}]""",
                tags = "abendessen,suppe,vegan,meal-prep"
            ),
            Recipe(
                recipeId = "r4",
                title = "Griechischer Salat",
                description = "Klassisch mediterran — leicht und sättigend",
                prepTimeMinutes = 10,
                cookTimeMinutes = 0,
                servings = 1,
                totalCaloriesPerServing = 250f,
                proteinPerServing = 8f,
                carbsPerServing = 15f,
                fatPerServing = 18f,
                greenPercent = 60f,
                yellowPercent = 15f,
                orangePercent = 25f,
                ingredientsJson = """[{"name":"Gurke","amount":"1/2","unit":"Stück"},{"name":"Tomaten","amount":"2","unit":"Stück"},{"name":"Feta-Käse","amount":"60","unit":"g"},{"name":"Oliven","amount":"50","unit":"g"},{"name":"Rote Zwiebel","amount":"1/4","unit":"Stück"},{"name":"Olivenöl","amount":"1","unit":"EL"}]""",
                stepsJson = """[{"step":1,"instruction":"Alle Zutaten in mundgerechte Stücke schneiden."},{"step":2,"instruction":"Feta zerbröckeln und Oliven hinzufügen."},{"step":3,"instruction":"Mit Olivenöl, Oregano und Pfeffer würzen."}]""",
                tags = "salat,mittagessen,schnell,mediterran"
            )
        )
        recipeRepository.seedRecipes(recipes)
    }
}
