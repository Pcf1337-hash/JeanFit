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
            // Englische MealDB-Rezepte aus vorherigen Versionen entfernen
            recipeRepository.deleteEnglishMealDbRecipes()
            val cached = recipeRepository.getAllRecipes().first()
            if (cached.isEmpty()) {
                seedRecipes()
            }
        }
        viewModelScope.launch {
            _query.flatMapLatest { q ->
                if (q.isBlank()) recipeRepository.getAllRecipes()
                else {
                    // Remote-Suche parallel starten
                    viewModelScope.launch { recipeRepository.searchRemote(q) }
                    recipeRepository.searchRecipes(q)
                }
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
            // ── FRÜHSTÜCK ────────────────────────────────────────────────────────────
            Recipe(
                recipeId = "r1",
                title = "Overnight Oats mit Beeren",
                description = "Cremige Haferflocken mit frischen Beeren — ideal zum Mitnehmen",
                prepTimeMinutes = 5, cookTimeMinutes = 0, servings = 1,
                totalCaloriesPerServing = 380f, proteinPerServing = 14f, carbsPerServing = 62f, fatPerServing = 8f,
                greenPercent = 30f, yellowPercent = 60f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Haferflocken","amount":"80","unit":"g"},{"name":"Milch (1,5%)","amount":"200","unit":"ml"},{"name":"Griechischer Joghurt","amount":"100","unit":"g"},{"name":"Gemischte Beeren","amount":"100","unit":"g"},{"name":"Honig","amount":"1","unit":"TL"}]""",
                stepsJson = """[{"step":1,"instruction":"Haferflocken, Milch und Joghurt in ein Glas schichten."},{"step":2,"instruction":"Über Nacht im Kühlschrank quellen lassen."},{"step":3,"instruction":"Am Morgen mit Beeren und Honig toppen."}]""",
                tags = "frühstück,schnell,vorbereitung"
            ),
            Recipe(
                recipeId = "r5",
                title = "Rührei mit Spinat und Tomaten",
                description = "Proteinreiches Frühstück in unter 10 Minuten",
                prepTimeMinutes = 5, cookTimeMinutes = 8, servings = 1,
                totalCaloriesPerServing = 290f, proteinPerServing = 22f, carbsPerServing = 8f, fatPerServing = 18f,
                greenPercent = 65f, yellowPercent = 20f, orangePercent = 15f,
                ingredientsJson = """[{"name":"Eier","amount":"3","unit":"Stück"},{"name":"Babyspinat","amount":"80","unit":"g"},{"name":"Kirschtomaten","amount":"100","unit":"g"},{"name":"Olivenöl","amount":"1","unit":"TL"},{"name":"Salz und Pfeffer","amount":"nach","unit":"Geschmack"}]""",
                stepsJson = """[{"step":1,"instruction":"Eier in einer Schüssel verquirlen."},{"step":2,"instruction":"Öl in der Pfanne erhitzen, Tomaten kurz anbraten."},{"step":3,"instruction":"Spinat hinzufügen und zusammenfallen lassen."},{"step":4,"instruction":"Eier eingießen, langsam stocken lassen und würzen."}]""",
                tags = "frühstück,protein,low-carb,schnell"
            ),
            Recipe(
                recipeId = "r6",
                title = "Vollkorn-Pfannkuchen mit Apfel",
                description = "Fluffige Pfannkuchen aus Vollkornmehl mit Apfelscheiben",
                prepTimeMinutes = 10, cookTimeMinutes = 15, servings = 2,
                totalCaloriesPerServing = 360f, proteinPerServing = 12f, carbsPerServing = 58f, fatPerServing = 9f,
                greenPercent = 20f, yellowPercent = 65f, orangePercent = 15f,
                ingredientsJson = """[{"name":"Vollkornmehl","amount":"150","unit":"g"},{"name":"Ei","amount":"2","unit":"Stück"},{"name":"Milch","amount":"200","unit":"ml"},{"name":"Apfel","amount":"1","unit":"Stück"},{"name":"Backpulver","amount":"1","unit":"TL"},{"name":"Zimt","amount":"1","unit":"Prise"}]""",
                stepsJson = """[{"step":1,"instruction":"Mehl, Ei, Milch und Backpulver zu einem Teig verrühren."},{"step":2,"instruction":"Apfel in dünne Scheiben schneiden."},{"step":3,"instruction":"Pfannkuchen in einer beschichteten Pfanne ausbacken."},{"step":4,"instruction":"Mit Apfelscheiben und Zimt servieren."}]""",
                tags = "frühstück,vollkorn,wochenende"
            ),
            Recipe(
                recipeId = "r7",
                title = "Quark mit Leinsamen und Banane",
                description = "Schnelles High-Protein-Frühstück — satt bis Mittag",
                prepTimeMinutes = 5, cookTimeMinutes = 0, servings = 1,
                totalCaloriesPerServing = 310f, proteinPerServing = 26f, carbsPerServing = 34f, fatPerServing = 6f,
                greenPercent = 40f, yellowPercent = 50f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Magerquark","amount":"250","unit":"g"},{"name":"Banane","amount":"1","unit":"Stück"},{"name":"Leinsamen (gemahlen)","amount":"1","unit":"EL"},{"name":"Vanilleextrakt","amount":"1","unit":"Spritzer"}]""",
                stepsJson = """[{"step":1,"instruction":"Quark in eine Schüssel geben."},{"step":2,"instruction":"Banane in Scheiben schneiden und dazulegen."},{"step":3,"instruction":"Leinsamen und Vanille unterrühren."}]""",
                tags = "frühstück,protein,schnell,ohne-kochen"
            ),
            // ── MITTAGESSEN ──────────────────────────────────────────────────────────
            Recipe(
                recipeId = "r2",
                title = "Hähnchen-Gemüse-Bowl",
                description = "Protein-reiche Lunch Bowl mit buntem Gemüse",
                prepTimeMinutes = 10, cookTimeMinutes = 20, servings = 1,
                totalCaloriesPerServing = 450f, proteinPerServing = 38f, carbsPerServing = 35f, fatPerServing = 12f,
                greenPercent = 45f, yellowPercent = 45f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Hähnchenbrust","amount":"150","unit":"g"},{"name":"Brokkoli","amount":"150","unit":"g"},{"name":"Paprika","amount":"100","unit":"g"},{"name":"Vollkorn-Reis","amount":"60","unit":"g (roh)"},{"name":"Olivenöl","amount":"1","unit":"EL"},{"name":"Gewürze","amount":"nach","unit":"Geschmack"}]""",
                stepsJson = """[{"step":1,"instruction":"Reis nach Packungsanweisung kochen."},{"step":2,"instruction":"Hähnchen in Würfel schneiden und in Öl anbraten (8-10 min)."},{"step":3,"instruction":"Gemüse hinzufügen und weitere 5-7 min garen."},{"step":4,"instruction":"Alles in einer Bowl anrichten."}]""",
                tags = "mittagessen,protein,bowl"
            ),
            Recipe(
                recipeId = "r4",
                title = "Griechischer Salat",
                description = "Klassisch mediterran — leicht und sättigend",
                prepTimeMinutes = 10, cookTimeMinutes = 0, servings = 1,
                totalCaloriesPerServing = 250f, proteinPerServing = 8f, carbsPerServing = 15f, fatPerServing = 18f,
                greenPercent = 60f, yellowPercent = 15f, orangePercent = 25f,
                ingredientsJson = """[{"name":"Gurke","amount":"1/2","unit":"Stück"},{"name":"Tomaten","amount":"2","unit":"Stück"},{"name":"Feta-Käse","amount":"60","unit":"g"},{"name":"Oliven","amount":"50","unit":"g"},{"name":"Rote Zwiebel","amount":"1/4","unit":"Stück"},{"name":"Olivenöl","amount":"1","unit":"EL"}]""",
                stepsJson = """[{"step":1,"instruction":"Alle Zutaten in mundgerechte Stücke schneiden."},{"step":2,"instruction":"Feta zerbröckeln und Oliven hinzufügen."},{"step":3,"instruction":"Mit Olivenöl, Oregano und Pfeffer würzen."}]""",
                tags = "salat,mittagessen,schnell,mediterran"
            ),
            Recipe(
                recipeId = "r8",
                title = "Thunfisch-Wrap mit Avocado",
                description = "Schneller Wrap mit gesunden Fetten und viel Protein",
                prepTimeMinutes = 10, cookTimeMinutes = 0, servings = 1,
                totalCaloriesPerServing = 420f, proteinPerServing = 30f, carbsPerServing = 38f, fatPerServing = 16f,
                greenPercent = 40f, yellowPercent = 40f, orangePercent = 20f,
                ingredientsJson = """[{"name":"Vollkorn-Tortilla","amount":"1","unit":"Stück"},{"name":"Thunfisch (Dose, in Wasser)","amount":"1","unit":"Dose (150g)"},{"name":"Avocado","amount":"1/2","unit":"Stück"},{"name":"Cherrytomaten","amount":"5","unit":"Stück"},{"name":"Salatblätter","amount":"2","unit":"Blätter"},{"name":"Zitronensaft","amount":"1","unit":"Spritzer"}]""",
                stepsJson = """[{"step":1,"instruction":"Thunfisch abtropfen lassen, Avocado zerdrücken."},{"step":2,"instruction":"Tortilla mit Avocado bestreichen."},{"step":3,"instruction":"Thunfisch, Tomaten und Salat auflegen."},{"step":4,"instruction":"Mit Zitronensaft beträufeln und einrollen."}]""",
                tags = "mittagessen,wrap,protein,schnell"
            ),
            Recipe(
                recipeId = "r9",
                title = "Zucchini-Nudeln mit Pesto",
                description = "Leichte Low-Carb-Alternative zu klassischer Pasta",
                prepTimeMinutes = 10, cookTimeMinutes = 5, servings = 1,
                totalCaloriesPerServing = 280f, proteinPerServing = 8f, carbsPerServing = 12f, fatPerServing = 22f,
                greenPercent = 70f, yellowPercent = 20f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Zucchini","amount":"2","unit":"Stück"},{"name":"Basilikum-Pesto","amount":"2","unit":"EL"},{"name":"Kirschtomaten","amount":"100","unit":"g"},{"name":"Parmesan","amount":"20","unit":"g"},{"name":"Pinienkerne","amount":"1","unit":"EL"}]""",
                stepsJson = """[{"step":1,"instruction":"Zucchini mit einem Spiralschneider zu Nudeln verarbeiten."},{"step":2,"instruction":"In einer Pfanne kurz (2-3 min) anbraten."},{"step":3,"instruction":"Pesto unterrühren, Tomaten hinzufügen."},{"step":4,"instruction":"Mit Parmesan und Pinienkernen servieren."}]""",
                tags = "mittagessen,low-carb,vegetarisch,schnell"
            ),
            Recipe(
                recipeId = "r10",
                title = "Linsen-Bolognese mit Spaghetti",
                description = "Vegane Bolognese — genauso herzhaft wie das Original",
                prepTimeMinutes = 10, cookTimeMinutes = 30, servings = 2,
                totalCaloriesPerServing = 490f, proteinPerServing = 24f, carbsPerServing = 78f, fatPerServing = 8f,
                greenPercent = 30f, yellowPercent = 60f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Vollkorn-Spaghetti","amount":"160","unit":"g"},{"name":"Rote Linsen","amount":"150","unit":"g"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Karotte","amount":"1","unit":"Stück"},{"name":"Knoblauch","amount":"2","unit":"Zehen"},{"name":"Olivenöl","amount":"1","unit":"EL"},{"name":"Tomatenmark","amount":"2","unit":"EL"}]""",
                stepsJson = """[{"step":1,"instruction":"Zwiebel und Knoblauch fein hacken, in Öl anschwitzen."},{"step":2,"instruction":"Karotte würfeln und mitdünsten."},{"step":3,"instruction":"Tomatenmark kurz anrösten."},{"step":4,"instruction":"Linsen, Tomaten und 200ml Wasser hinzufügen, 25 min köcheln."},{"step":5,"instruction":"Mit Spaghetti servieren."}]""",
                tags = "mittagessen,abendessen,pasta,vegan,meal-prep"
            ),
            // ── ABENDESSEN ───────────────────────────────────────────────────────────
            Recipe(
                recipeId = "r3",
                title = "Gemüse-Linsen-Suppe",
                description = "Herzhafte Suppe mit viel Ballaststoffen und Protein",
                prepTimeMinutes = 10, cookTimeMinutes = 25, servings = 2,
                totalCaloriesPerServing = 320f, proteinPerServing = 18f, carbsPerServing = 48f, fatPerServing = 6f,
                greenPercent = 55f, yellowPercent = 40f, orangePercent = 5f,
                ingredientsJson = """[{"name":"Rote Linsen","amount":"150","unit":"g"},{"name":"Karotten","amount":"2","unit":"Stück"},{"name":"Sellerie","amount":"2","unit":"Stangen"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Gemüsebrühe","amount":"800","unit":"ml"},{"name":"Knoblauch","amount":"2","unit":"Zehen"}]""",
                stepsJson = """[{"step":1,"instruction":"Gemüse würfeln und in einem Topf anschwitzen."},{"step":2,"instruction":"Linsen, Tomaten und Brühe hinzufügen."},{"step":3,"instruction":"25 Minuten köcheln lassen bis Linsen weich."},{"step":4,"instruction":"Mit Salz, Pfeffer und Kreuzkümmel abschmecken."}]""",
                tags = "abendessen,suppe,vegan,meal-prep"
            ),
            Recipe(
                recipeId = "r11",
                title = "Ofengemüse mit Kichererbsen",
                description = "Knusprig geröstetes Gemüse mit proteinreichen Kichererbsen",
                prepTimeMinutes = 15, cookTimeMinutes = 30, servings = 2,
                totalCaloriesPerServing = 380f, proteinPerServing = 16f, carbsPerServing = 52f, fatPerServing = 12f,
                greenPercent = 55f, yellowPercent = 35f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Kichererbsen (Dose)","amount":"1","unit":"Dose (400g)"},{"name":"Süßkartoffel","amount":"1","unit":"Stück"},{"name":"Paprika","amount":"2","unit":"Stück"},{"name":"Zucchini","amount":"1","unit":"Stück"},{"name":"Olivenöl","amount":"2","unit":"EL"},{"name":"Kreuzkümmel und Paprikapulver","amount":"je 1","unit":"TL"}]""",
                stepsJson = """[{"step":1,"instruction":"Ofen auf 200°C vorheizen."},{"step":2,"instruction":"Gemüse und Kichererbsen in Stücke schneiden."},{"step":3,"instruction":"Mit Öl und Gewürzen mischen, auf Blech verteilen."},{"step":4,"instruction":"30 Minuten backen, bis alles goldbraun ist."}]""",
                tags = "abendessen,vegan,ofenrezept,meal-prep"
            ),
            Recipe(
                recipeId = "r12",
                title = "Lachs mit Süßkartoffel-Püree",
                description = "Omega-3-reicher Lachs mit cremigem Süßkartoffelpüree",
                prepTimeMinutes = 10, cookTimeMinutes = 25, servings = 2,
                totalCaloriesPerServing = 520f, proteinPerServing = 35f, carbsPerServing = 42f, fatPerServing = 20f,
                greenPercent = 35f, yellowPercent = 45f, orangePercent = 20f,
                ingredientsJson = """[{"name":"Lachsfilet","amount":"300","unit":"g"},{"name":"Süßkartoffeln","amount":"400","unit":"g"},{"name":"Butter","amount":"1","unit":"EL"},{"name":"Milch","amount":"50","unit":"ml"},{"name":"Zitrone","amount":"1/2","unit":"Stück"},{"name":"Dill","amount":"nach","unit":"Geschmack"}]""",
                stepsJson = """[{"step":1,"instruction":"Süßkartoffeln schälen, würfeln und 20 min kochen."},{"step":2,"instruction":"Lachs mit Zitronensaft und Dill würzen."},{"step":3,"instruction":"Lachs in der Pfanne 4 min pro Seite braten."},{"step":4,"instruction":"Süßkartoffeln stampfen, mit Butter und Milch cremig rühren."}]""",
                tags = "abendessen,fisch,omega3,protein"
            ),
            Recipe(
                recipeId = "r13",
                title = "Türkische Linsen-Köfte",
                description = "Vegane Linsen-Bällchen mit Bulgur — traditionell und lecker",
                prepTimeMinutes = 20, cookTimeMinutes = 20, servings = 4,
                totalCaloriesPerServing = 310f, proteinPerServing = 14f, carbsPerServing = 50f, fatPerServing = 6f,
                greenPercent = 50f, yellowPercent = 45f, orangePercent = 5f,
                ingredientsJson = """[{"name":"Rote Linsen","amount":"200","unit":"g"},{"name":"Feinkörniger Bulgur","amount":"150","unit":"g"},{"name":"Zwiebel","amount":"2","unit":"Stück"},{"name":"Tomatenmark","amount":"2","unit":"EL"},{"name":"Paprikapaste","amount":"1","unit":"EL"},{"name":"Petersilie","amount":"1","unit":"Bund"},{"name":"Kreuzkümmel","amount":"1","unit":"TL"}]""",
                stepsJson = """[{"step":1,"instruction":"Linsen in Wasser 15 min kochen, Bulgur einrühren und quellen lassen."},{"step":2,"instruction":"Zwiebeln fein hacken und glasig dünsten."},{"step":3,"instruction":"Alle Zutaten vermengen, mit den Händen Köfte formen."},{"step":4,"instruction":"Mit Salat und Zitronensaft servieren."}]""",
                tags = "abendessen,vegan,meal-prep,fingerfood"
            ),
            Recipe(
                recipeId = "r14",
                title = "Hähnchen-Tikka-Masala (light)",
                description = "Cremiges Curry — weniger Fett, voller Geschmack",
                prepTimeMinutes = 15, cookTimeMinutes = 25, servings = 2,
                totalCaloriesPerServing = 430f, proteinPerServing = 40f, carbsPerServing = 30f, fatPerServing = 14f,
                greenPercent = 30f, yellowPercent = 50f, orangePercent = 20f,
                ingredientsJson = """[{"name":"Hähnchenbrust","amount":"300","unit":"g"},{"name":"Joghurt (fettarm)","amount":"150","unit":"g"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Knoblauch","amount":"3","unit":"Zehen"},{"name":"Ingwer","amount":"2","unit":"cm"},{"name":"Tikka-Masala-Gewürz","amount":"2","unit":"EL"},{"name":"Basmati-Reis","amount":"120","unit":"g"}]""",
                stepsJson = """[{"step":1,"instruction":"Hähnchen in Joghurt und Gewürzen marinieren (mind. 30 min)."},{"step":2,"instruction":"Zwiebeln, Knoblauch und Ingwer anbraten."},{"step":3,"instruction":"Hähnchen scharf anbraten, dann Tomaten hinzufügen."},{"step":4,"instruction":"20 min köcheln lassen. Mit Reis servieren."}]""",
                tags = "abendessen,hähnchen,curry,meal-prep"
            ),
            Recipe(
                recipeId = "r15",
                title = "Brokkoli-Cheddar-Frittata",
                description = "Eiweißreiche Ofeneier-Torte — warm oder kalt genießen",
                prepTimeMinutes = 10, cookTimeMinutes = 20, servings = 2,
                totalCaloriesPerServing = 340f, proteinPerServing = 28f, carbsPerServing = 8f, fatPerServing = 22f,
                greenPercent = 55f, yellowPercent = 25f, orangePercent = 20f,
                ingredientsJson = """[{"name":"Eier","amount":"6","unit":"Stück"},{"name":"Brokkoli","amount":"200","unit":"g"},{"name":"Cheddar (gerieben)","amount":"60","unit":"g"},{"name":"Milch","amount":"3","unit":"EL"},{"name":"Salz und Pfeffer","amount":"nach","unit":"Geschmack"}]""",
                stepsJson = """[{"step":1,"instruction":"Ofen auf 180°C vorheizen."},{"step":2,"instruction":"Brokkoli in Röschen teilen und 3 min blanchieren."},{"step":3,"instruction":"Eier mit Milch verquirlen, Cheddar einrühren."},{"step":4,"instruction":"Brokkoli in ofenfeste Pfanne geben, Eimasse drüber."},{"step":5,"instruction":"20 min backen bis fest und goldbraun."}]""",
                tags = "abendessen,low-carb,protein,meal-prep"
            ),
            // ── SNACKS & SALATE ──────────────────────────────────────────────────────
            Recipe(
                recipeId = "r16",
                title = "Hummus mit Gemüsesticks",
                description = "Selbstgemachter Hummus mit knackigem Rohkostgemüse",
                prepTimeMinutes = 10, cookTimeMinutes = 0, servings = 2,
                totalCaloriesPerServing = 220f, proteinPerServing = 9f, carbsPerServing = 28f, fatPerServing = 9f,
                greenPercent = 65f, yellowPercent = 30f, orangePercent = 5f,
                ingredientsJson = """[{"name":"Kichererbsen (Dose)","amount":"1","unit":"Dose (400g)"},{"name":"Tahini","amount":"2","unit":"EL"},{"name":"Zitronensaft","amount":"1","unit":"EL"},{"name":"Knoblauch","amount":"1","unit":"Zehe"},{"name":"Karotten","amount":"2","unit":"Stück"},{"name":"Paprika","amount":"1","unit":"Stück"},{"name":"Gurke","amount":"1/2","unit":"Stück"}]""",
                stepsJson = """[{"step":1,"instruction":"Kichererbsen, Tahini, Zitrone und Knoblauch im Mixer pürieren."},{"step":2,"instruction":"Mit Wasser auf gewünschte Konsistenz verdünnen."},{"step":3,"instruction":"Gemüse in Sticks schneiden und servieren."}]""",
                tags = "snack,vegan,dip,gesund"
            ),
            Recipe(
                recipeId = "r17",
                title = "Protein-Smoothie-Bowl",
                description = "Dicker Smoothie als Topping-Bowl — macht lange satt",
                prepTimeMinutes = 10, cookTimeMinutes = 0, servings = 1,
                totalCaloriesPerServing = 390f, proteinPerServing = 22f, carbsPerServing = 55f, fatPerServing = 9f,
                greenPercent = 35f, yellowPercent = 55f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Gefrorene Banane","amount":"1","unit":"Stück"},{"name":"Gefrorene Beeren","amount":"100","unit":"g"},{"name":"Magerquark","amount":"150","unit":"g"},{"name":"Mandelmilch","amount":"50","unit":"ml"},{"name":"Granola","amount":"30","unit":"g"},{"name":"Frisches Obst zum Toppen","amount":"nach","unit":"Wahl"}]""",
                stepsJson = """[{"step":1,"instruction":"Banane, Beeren, Quark und Milch dickflüssig mixen."},{"step":2,"instruction":"In einer Schüssel anrichten."},{"step":3,"instruction":"Mit Granola und frischem Obst toppen."}]""",
                tags = "frühstück,snack,smoothiebowl,protein"
            ),
            Recipe(
                recipeId = "r18",
                title = "Quinoa-Salat mit Feta",
                description = "Vollständige Mahlzeit im Salat — mit allen Makros",
                prepTimeMinutes = 10, cookTimeMinutes = 15, servings = 2,
                totalCaloriesPerServing = 420f, proteinPerServing = 18f, carbsPerServing = 52f, fatPerServing = 16f,
                greenPercent = 50f, yellowPercent = 35f, orangePercent = 15f,
                ingredientsJson = """[{"name":"Quinoa","amount":"150","unit":"g (roh)"},{"name":"Feta-Käse","amount":"80","unit":"g"},{"name":"Gurke","amount":"1","unit":"Stück"},{"name":"Cherrytomaten","amount":"150","unit":"g"},{"name":"Rote Zwiebel","amount":"1/2","unit":"Stück"},{"name":"Petersilie","amount":"1/2","unit":"Bund"},{"name":"Zitrone und Olivenöl","amount":"je 1","unit":"EL"}]""",
                stepsJson = """[{"step":1,"instruction":"Quinoa nach Packungsanleitung kochen und abkühlen lassen."},{"step":2,"instruction":"Gemüse würfeln, Petersilie hacken."},{"step":3,"instruction":"Alles mischen, mit Zitrone und Öl anmachen."},{"step":4,"instruction":"Feta darüber krümeln."}]""",
                tags = "salat,mittagessen,meal-prep,vegetarisch"
            ),
            Recipe(
                recipeId = "r19",
                title = "Erbsen-Minz-Suppe",
                description = "Samtige grüne Suppe — schnell und kalorienarm",
                prepTimeMinutes = 5, cookTimeMinutes = 10, servings = 2,
                totalCaloriesPerServing = 210f, proteinPerServing = 12f, carbsPerServing = 28f, fatPerServing = 5f,
                greenPercent = 75f, yellowPercent = 20f, orangePercent = 5f,
                ingredientsJson = """[{"name":"Tiefkühl-Erbsen","amount":"400","unit":"g"},{"name":"Gemüsebrühe","amount":"600","unit":"ml"},{"name":"Frische Minze","amount":"10","unit":"Blätter"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Joghurt (zum Servieren)","amount":"2","unit":"EL"}]""",
                stepsJson = """[{"step":1,"instruction":"Zwiebel in Brühe weich kochen."},{"step":2,"instruction":"Erbsen hinzufügen und 5 min kochen."},{"step":3,"instruction":"Mit Minze pürieren bis samtig glatt."},{"step":4,"instruction":"Mit einem Klecks Joghurt servieren."}]""",
                tags = "suppe,vegan,schnell,low-calorie"
            ),
            Recipe(
                recipeId = "r20",
                title = "Gebackener Blumenkohl mit Joghurt-Dip",
                description = "Knuspriger Blumenkohl als Hauptgericht oder Beilage",
                prepTimeMinutes = 10, cookTimeMinutes = 25, servings = 2,
                totalCaloriesPerServing = 230f, proteinPerServing = 10f, carbsPerServing = 22f, fatPerServing = 12f,
                greenPercent = 65f, yellowPercent = 25f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Blumenkohl","amount":"1","unit":"Kopf"},{"name":"Olivenöl","amount":"2","unit":"EL"},{"name":"Paprikapulver (geräuchert)","amount":"1","unit":"TL"},{"name":"Joghurt","amount":"150","unit":"g"},{"name":"Knoblauch","amount":"1","unit":"Zehe"},{"name":"Zitronensaft","amount":"1","unit":"EL"}]""",
                stepsJson = """[{"step":1,"instruction":"Ofen auf 200°C vorheizen."},{"step":2,"instruction":"Blumenkohl in Röschen teilen, mit Öl und Paprika mischen."},{"step":3,"instruction":"25 min backen bis goldbraun."},{"step":4,"instruction":"Joghurt mit Knoblauch und Zitrone verrühren."},{"step":5,"instruction":"Blumenkohl mit Joghurt-Dip servieren."}]""",
                tags = "abendessen,vegan,ofenrezept,low-calorie"
            ),
            Recipe(
                recipeId = "r21",
                title = "Hüttenkäse-Paprika-Teller",
                description = "Einfacher High-Protein-Snack für zwischendurch",
                prepTimeMinutes = 5, cookTimeMinutes = 0, servings = 1,
                totalCaloriesPerServing = 180f, proteinPerServing = 20f, carbsPerServing = 10f, fatPerServing = 6f,
                greenPercent = 70f, yellowPercent = 25f, orangePercent = 5f,
                ingredientsJson = """[{"name":"Hüttenkäse","amount":"200","unit":"g"},{"name":"Rote Paprika","amount":"1","unit":"Stück"},{"name":"Gurke","amount":"1/2","unit":"Stück"},{"name":"Salz und Pfeffer","amount":"nach","unit":"Geschmack"},{"name":"Schnittlauch","amount":"nach","unit":"Geschmack"}]""",
                stepsJson = """[{"step":1,"instruction":"Paprika und Gurke in Stücke schneiden."},{"step":2,"instruction":"Hüttenkäse würzen und mit Schnittlauch bestreuen."},{"step":3,"instruction":"Gemüse dazu anrichten und genießen."}]""",
                tags = "snack,protein,low-carb,schnell,ohne-kochen"
            ),
            Recipe(
                recipeId = "r22",
                title = "Süßkartoffel-Schwarze-Bohnen-Burrito",
                description = "Sättigender veganer Burrito mit viel Nährstoffen",
                prepTimeMinutes = 15, cookTimeMinutes = 20, servings = 2,
                totalCaloriesPerServing = 480f, proteinPerServing = 18f, carbsPerServing = 72f, fatPerServing = 14f,
                greenPercent = 35f, yellowPercent = 55f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Vollkorn-Tortilla","amount":"2","unit":"Stück"},{"name":"Süßkartoffel","amount":"300","unit":"g"},{"name":"Schwarze Bohnen (Dose)","amount":"1","unit":"Dose (400g)"},{"name":"Mais (Dose)","amount":"100","unit":"g"},{"name":"Paprika","amount":"1","unit":"Stück"},{"name":"Guacamole oder Avocado","amount":"1/2","unit":"Stück"},{"name":"Chili-Pulver","amount":"1","unit":"TL"}]""",
                stepsJson = """[{"step":1,"instruction":"Süßkartoffel würfeln und 15 min im Ofen rösten."},{"step":2,"instruction":"Bohnen und Mais abtropfen, mit Paprika anschwitzen."},{"step":3,"instruction":"Chili-Pulver einrühren."},{"step":4,"instruction":"Tortilla füllen, Guacamole drauf und einrollen."}]""",
                tags = "mittagessen,abendessen,vegan,burrito"
            ),
            Recipe(
                recipeId = "r23",
                title = "Tomaten-Mozzarella mit Basilikum",
                description = "Caprese-Salat — einfach, frisch, italiano",
                prepTimeMinutes = 8, cookTimeMinutes = 0, servings = 2,
                totalCaloriesPerServing = 270f, proteinPerServing = 14f, carbsPerServing = 6f, fatPerServing = 20f,
                greenPercent = 50f, yellowPercent = 20f, orangePercent = 30f,
                ingredientsJson = """[{"name":"Tomaten (groß)","amount":"3","unit":"Stück"},{"name":"Mozzarella (light)","amount":"125","unit":"g"},{"name":"Frisches Basilikum","amount":"1","unit":"Handvoll"},{"name":"Olivenöl (extra vergine)","amount":"2","unit":"EL"},{"name":"Balsamico-Creme","amount":"1","unit":"TL"},{"name":"Salz und Pfeffer","amount":"nach","unit":"Geschmack"}]""",
                stepsJson = """[{"step":1,"instruction":"Tomaten und Mozzarella in gleichmäßige Scheiben schneiden."},{"step":2,"instruction":"Abwechselnd auf einem Teller anrichten."},{"step":3,"instruction":"Mit Basilikum, Öl, Balsamico und Gewürzen vollenden."}]""",
                tags = "salat,snack,vegetarisch,schnell,mediterran"
            ),
            Recipe(
                recipeId = "r24",
                title = "Proteinreicher Gemüseeintopf",
                description = "Wärmender Eintopf mit Hähnchen und viel Gemüse",
                prepTimeMinutes = 15, cookTimeMinutes = 35, servings = 4,
                totalCaloriesPerServing = 350f, proteinPerServing = 32f, carbsPerServing = 30f, fatPerServing = 10f,
                greenPercent = 50f, yellowPercent = 40f, orangePercent = 10f,
                ingredientsJson = """[{"name":"Hähnchenbrust","amount":"400","unit":"g"},{"name":"Kartoffeln","amount":"300","unit":"g"},{"name":"Karotten","amount":"3","unit":"Stück"},{"name":"Lauch","amount":"1","unit":"Stange"},{"name":"Sellerie","amount":"2","unit":"Stangen"},{"name":"Hühnerbrühe","amount":"1L","unit":""},{"name":"Petersilie","amount":"1/2","unit":"Bund"},{"name":"Lorbeerblatt","amount":"2","unit":"Stück"}]""",
                stepsJson = """[{"step":1,"instruction":"Hähnchen in die Brühe geben und 20 min kochen."},{"step":2,"instruction":"Hähnchen herausnehmen, in Stücke zupfen."},{"step":3,"instruction":"Gemüse in die Brühe geben und 15 min kochen."},{"step":4,"instruction":"Hähnchen wieder hinzufügen, mit Petersilie servieren."}]""",
                tags = "abendessen,suppe,meal-prep,hähnchen,herbst"
            ),
            Recipe(
                recipeId = "r25",
                title = "Mandel-Bananen-Energy-Balls",
                description = "Selbstgemachte Snack-Kugeln ohne Zucker — perfekt für unterwegs",
                prepTimeMinutes = 15, cookTimeMinutes = 0, servings = 12,
                totalCaloriesPerServing = 95f, proteinPerServing = 3f, carbsPerServing = 12f, fatPerServing = 4f,
                greenPercent = 25f, yellowPercent = 60f, orangePercent = 15f,
                ingredientsJson = """[{"name":"Reife Bananen","amount":"2","unit":"Stück"},{"name":"Haferflocken","amount":"100","unit":"g"},{"name":"Mandelbutter","amount":"2","unit":"EL"},{"name":"Datteln (entkernt)","amount":"6","unit":"Stück"},{"name":"Kakaopulver (ungesüßt)","amount":"1","unit":"EL"},{"name":"Chiasamen","amount":"1","unit":"EL"}]""",
                stepsJson = """[{"step":1,"instruction":"Alle Zutaten im Mixer zu einer klebrigen Masse verarbeiten."},{"step":2,"instruction":"Mit feuchten Händen kleine Kugeln formen."},{"step":3,"instruction":"2 Stunden im Kühlschrank fest werden lassen."},{"step":4,"instruction":"Im Kühlschrank bis zu 5 Tage haltbar."}]""",
                tags = "snack,vorbereitung,ohne-kochen,energie"
            )
        )
        recipeRepository.seedRecipes(recipes)

        // ── Alle 100 Rezepte aus erweiterung.md ──────────────────────────────────
        val erweiterungRecipes = listOf(
            // FRÜHSTÜCK 1-20
            Recipe("r_001","Griechischer Joghurt Bowl","High-Protein-Frühstück mit Erdbeeren und Granola",null,5,0,1,280f,20f,32f,6f,60f,30f,10f,
                """[{"name":"Griechischer Joghurt (0%)","amount":"200","unit":"g"},{"name":"Erdbeeren","amount":"100","unit":"g"},{"name":"Granola","amount":"30","unit":"g"},{"name":"Mandeln (gehackt)","amount":"15","unit":"g"},{"name":"Honig","amount":"10","unit":"g"}]""",
                """[{"step":1,"instruction":"Joghurt in eine Schüssel geben."},{"step":2,"instruction":"Erdbeeren waschen, halbieren und darauflegen."},{"step":3,"instruction":"Granola und Mandeln darüberstreuen."},{"step":4,"instruction":"Mit Honig beträufeln und sofort servieren."}]""","high-protein,schnell,vegetarisch,frühstück"),
            Recipe("r_002","Erdbeer-Joghurt-Bowl","Cremige Bowl mit frischen Früchten",null,5,0,1,280f,20f,32f,6f,60f,30f,10f,
                """[{"name":"Griechischer Joghurt","amount":"200","unit":"g"},{"name":"Erdbeeren","amount":"100","unit":"g"},{"name":"Granola","amount":"30","unit":"g"}]""",
                """[{"step":1,"instruction":"Joghurt in Schüssel füllen."},{"step":2,"instruction":"Erdbeeren darauflegen."},{"step":3,"instruction":"Mit Granola toppen."}]""","frühstück,schnell,vegetarisch"),
            Recipe("r_003","Protein-Pancakes (Banane & Ei)","Fluffige Pancakes ohne Mehl",null,5,10,2,220f,18f,28f,5f,40f,55f,5f,
                """[{"name":"Reife Banane","amount":"1","unit":"Stück"},{"name":"Eier","amount":"2","unit":"Stück"},{"name":"Haferflocken","amount":"40","unit":"g"},{"name":"Zimt","amount":"1","unit":"TL"},{"name":"Backpulver","amount":"0.5","unit":"TL"}]""",
                """[{"step":1,"instruction":"Banane zerdrücken, mit Eiern verquirlen."},{"step":2,"instruction":"Haferflocken, Zimt und Backpulver untermischen."},{"step":3,"instruction":"Pfanne mit Kokosöl erhitzen."},{"step":4,"instruction":"Kleine Pancakes ausbacken, je 2 Min. pro Seite."},{"step":5,"instruction":"Mit Beeren oder Joghurt servieren."}]""","high-protein,vegetarisch,frühstück"),
            Recipe("r_004","Avocado-Toast mit Ei","Klassiker mit pochiertem Ei",null,5,8,1,380f,16f,30f,22f,40f,40f,20f,
                """[{"name":"Vollkornbrot","amount":"2","unit":"Scheiben"},{"name":"Avocado (reif)","amount":"0.5","unit":"Stück"},{"name":"Ei","amount":"1","unit":"Stück"},{"name":"Zitronensaft","amount":"5","unit":"ml"},{"name":"Chiliflocken","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Brot toasten."},{"step":2,"instruction":"Avocado zerdrücken, mit Zitronensaft würzen."},{"step":3,"instruction":"Ei pochieren (4 Min. im siedenden Wasser)."},{"step":4,"instruction":"Avocado auf Toast streichen, Ei darauflegen."},{"step":5,"instruction":"Mit Chiliflocken toppen."}]""","vegetarisch,frühstück,sättigend"),
            Recipe("r_005","Grüner Detox-Smoothie","Vollgepackt mit Nährstoffen",null,5,0,1,180f,5f,38f,3f,75f,20f,5f,
                """[{"name":"Babyspinat","amount":"60","unit":"g"},{"name":"Banane (gefroren)","amount":"1","unit":"Stück"},{"name":"Apfel","amount":"0.5","unit":"Stück"},{"name":"Ingwer","amount":"5","unit":"g"},{"name":"Kokoswasser","amount":"200","unit":"ml"},{"name":"Zitronensaft","amount":"15","unit":"ml"}]""",
                """[{"step":1,"instruction":"Alle Zutaten in den Mixer geben."},{"step":2,"instruction":"60 Sekunden auf höchster Stufe mixen."},{"step":3,"instruction":"Konsistenz prüfen, ggf. Flüssigkeit hinzufügen."},{"step":4,"instruction":"Sofort trinken."}]""","vegan,schnell,detox,frühstück"),
            Recipe("r_006","Rührei mit Gemüse","Schnelles High-Protein-Frühstück",null,5,8,1,260f,20f,8f,16f,65f,25f,10f,
                """[{"name":"Eier","amount":"3","unit":"Stück"},{"name":"Paprika","amount":"50","unit":"g"},{"name":"Spinat","amount":"40","unit":"g"},{"name":"Tomaten","amount":"50","unit":"g"},{"name":"Olivenöl","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Eier verquirlen und würzen."},{"step":2,"instruction":"Gemüse kurz anbraten."},{"step":3,"instruction":"Eier dazugeben und stocken lassen."}]""","high-protein,schnell,vegetarisch,low-carb,frühstück"),
            Recipe("r_007","Chia Pudding mit Mango","Overnight-Frühstück mit Mango",null,5,0,1,290f,8f,40f,12f,60f,35f,5f,
                """[{"name":"Chiasamen","amount":"3","unit":"EL"},{"name":"Kokos-/Mandelmilch","amount":"200","unit":"ml"},{"name":"Mango","amount":"100","unit":"g"},{"name":"Vanille","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Chiasamen mit Milch mischen."},{"step":2,"instruction":"Über Nacht kühl stellen."},{"step":3,"instruction":"Mit frischer Mango servieren."}]""","vegan,meal-prep,frühstück"),
            Recipe("r_008","Müsli Bowl mit Nüssen","Knuspriges Müsli mit Nüssen",null,3,0,1,420f,14f,52f,18f,30f,55f,15f,
                """[{"name":"Hafermüsli","amount":"80","unit":"g"},{"name":"Gemischte Nüsse","amount":"20","unit":"g"},{"name":"Beeren","amount":"50","unit":"g"},{"name":"Milch oder Joghurt","amount":"150","unit":"ml"}]""",
                """[{"step":1,"instruction":"Müsli in Schüssel geben."},{"step":2,"instruction":"Milch oder Joghurt dazugeben."},{"step":3,"instruction":"Mit Nüssen und Beeren toppen."}]""","vegetarisch,schnell,frühstück"),
            Recipe("r_009","Vollkorn-Waffeln mit Himbeeren","Saftige Waffeln zum Wochenende",null,10,15,2,310f,12f,48f,8f,30f,60f,10f,
                """[{"name":"Vollkornmehl","amount":"150","unit":"g"},{"name":"Eier","amount":"2","unit":"Stück"},{"name":"Milch","amount":"200","unit":"ml"},{"name":"Backpulver","amount":"1","unit":"TL"},{"name":"Himbeeren","amount":"100","unit":"g"}]""",
                """[{"step":1,"instruction":"Teig aus Mehl, Eiern, Milch und Backpulver rühren."},{"step":2,"instruction":"Waffeleisen erhitzen und einfetten."},{"step":3,"instruction":"Waffeln backen bis goldbraun."},{"step":4,"instruction":"Mit Himbeeren servieren."}]""","vegetarisch,wochenende,frühstück"),
            Recipe("r_010","Quark mit Leinsamen & Beeren","Einfaches High-Protein-Frühstück",null,3,0,1,220f,22f,18f,4f,50f,45f,5f,
                """[{"name":"Magerquark","amount":"250","unit":"g"},{"name":"Gemischte Beeren","amount":"80","unit":"g"},{"name":"Leinsamen (gemahlen)","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Quark in Schüssel geben."},{"step":2,"instruction":"Leinsamen einrühren."},{"step":3,"instruction":"Mit Beeren toppen."}]""","high-protein,schnell,vegetarisch,frühstück"),
            Recipe("r_011","Smoothie Bowl Açaí","Lila Bowl mit Açaí-Basis",null,10,0,1,320f,8f,52f,10f,55f,40f,5f,
                """[{"name":"Açaí-Pulver","amount":"1","unit":"EL"},{"name":"Gefrorene Beeren","amount":"150","unit":"g"},{"name":"Banane","amount":"1","unit":"Stück"},{"name":"Mandelmilch","amount":"80","unit":"ml"},{"name":"Topping: Granola + Früchte","amount":"40","unit":"g"}]""",
                """[{"step":1,"instruction":"Açaí, Beeren, Banane und Milch im Mixer zu einer dicken Masse pürieren."},{"step":2,"instruction":"In Schüssel geben."},{"step":3,"instruction":"Mit Granola und frischen Früchten toppen."}]""","vegan,frühstück"),
            Recipe("r_012","Haferbrei mit Apfel-Zimt","Warmer Porridge",null,5,8,1,310f,9f,55f,6f,50f,45f,5f,
                """[{"name":"Haferflocken","amount":"80","unit":"g"},{"name":"Wasser oder Milch","amount":"250","unit":"ml"},{"name":"Apfel","amount":"0.5","unit":"Stück"},{"name":"Zimt","amount":"1","unit":"TL"},{"name":"Honig","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Haferflocken mit Flüssigkeit aufkochen, 5 Min. quellen."},{"step":2,"instruction":"Apfel reiben oder würfeln und dazugeben."},{"step":3,"instruction":"Mit Zimt und Honig servieren."}]""","vegan,schnell,frühstück"),
            Recipe("r_013","Frittata mit Spinat","Warme Eierspezialität vom Blech",null,10,20,2,240f,18f,6f,15f,60f,25f,15f,
                """[{"name":"Eier","amount":"6","unit":"Stück"},{"name":"Babyspinat","amount":"150","unit":"g"},{"name":"Zwiebel","amount":"0.5","unit":"Stück"},{"name":"Käse (gerieben)","amount":"40","unit":"g"},{"name":"Olivenöl","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Ofen auf 180°C vorheizen."},{"step":2,"instruction":"Spinat und Zwiebeln anbraten."},{"step":3,"instruction":"Eier verquirlen, Käse einrühren."},{"step":4,"instruction":"Eimasse über Gemüse gießen, 20 Min. backen."}]""","low-carb,vegetarisch,frühstück"),
            Recipe("r_014","Bananenbrot (gesund)","Feuchtes Bananenbrot ohne Zucker",null,15,50,8,280f,6f,48f,8f,30f,60f,10f,
                """[{"name":"Reife Bananen","amount":"3","unit":"Stück"},{"name":"Vollkornmehl","amount":"200","unit":"g"},{"name":"Ei","amount":"2","unit":"Stück"},{"name":"Backpulver","amount":"1","unit":"TL"},{"name":"Zimt","amount":"1","unit":"TL"},{"name":"Nussbutter","amount":"2","unit":"EL"}]""",
                """[{"step":1,"instruction":"Ofen auf 175°C vorheizen."},{"step":2,"instruction":"Bananen zerdrücken, Eier und Nussbutter einrühren."},{"step":3,"instruction":"Mehl, Backpulver und Zimt unterheben."},{"step":4,"instruction":"In Kastenform 50 Min. backen."}]""","vegan,backen,frühstück"),
            Recipe("r_015","Ei-Muffins mit Paprika","Proteinreiche Mini-Omelettes",null,10,20,6,180f,14f,4f,11f,60f,30f,10f,
                """[{"name":"Eier","amount":"6","unit":"Stück"},{"name":"Rote Paprika","amount":"1","unit":"Stück"},{"name":"Schinken","amount":"50","unit":"g"},{"name":"Käse (gerieben)","amount":"30","unit":"g"}]""",
                """[{"step":1,"instruction":"Ofen auf 180°C vorheizen, Muffinform einfetten."},{"step":2,"instruction":"Paprika und Schinken würfeln."},{"step":3,"instruction":"Eier verquirlen, Gemüse und Käse einrühren."},{"step":4,"instruction":"In Muffinformen füllen, 20 Min. backen."}]""","meal-prep,low-carb,frühstück"),
            Recipe("r_016","Toast mit Hüttenkäse & Tomaten","Schnelles Protein-Frühstück",null,5,3,1,240f,18f,24f,6f,50f,40f,10f,
                """[{"name":"Vollkorntoast","amount":"2","unit":"Scheiben"},{"name":"Hüttenkäse","amount":"150","unit":"g"},{"name":"Tomaten","amount":"100","unit":"g"},{"name":"Schnittlauch","amount":"nach","unit":"Geschmack"}]""",
                """[{"step":1,"instruction":"Toast toasten."},{"step":2,"instruction":"Hüttenkäse daraufstreichen."},{"step":3,"instruction":"Tomaten in Scheiben legen, mit Schnittlauch toppen."}]""","high-protein,schnell,frühstück"),
            Recipe("r_017","Kokos-Haferflocken","Cremige Haferflocken mit Kokosgeschmack",null,5,0,1,350f,8f,50f,12f,40f,55f,5f,
                """[{"name":"Haferflocken","amount":"80","unit":"g"},{"name":"Kokosmilch (light)","amount":"200","unit":"ml"},{"name":"Rosinen","amount":"20","unit":"g"},{"name":"Kokosraspeln","amount":"10","unit":"g"}]""",
                """[{"step":1,"instruction":"Haferflocken mit Kokosmilch erhitzen."},{"step":2,"instruction":"Rosinen einrühren."},{"step":3,"instruction":"Mit Kokosraspeln toppen."}]""","vegan,frühstück"),
            Recipe("r_018","Matcha Latte Smoothie","Energie-Smoothie mit Matcha",null,5,0,1,160f,6f,28f,3f,65f,30f,5f,
                """[{"name":"Banane","amount":"1","unit":"Stück"},{"name":"Mandelmilch","amount":"250","unit":"ml"},{"name":"Matcha-Pulver","amount":"1","unit":"TL"},{"name":"Honig","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Alle Zutaten in Mixer geben."},{"step":2,"instruction":"Cremig mixen."},{"step":3,"instruction":"Im Glas servieren."}]""","vegan,schnell,frühstück"),
            Recipe("r_019","Rote-Bete Smoothie","Detox-Smoothie mit Rote Bete",null,5,0,1,170f,4f,36f,2f,70f,25f,5f,
                """[{"name":"Rote Bete (gekocht)","amount":"100","unit":"g"},{"name":"Apfel","amount":"1","unit":"Stück"},{"name":"Ingwer","amount":"5","unit":"g"},{"name":"Zitronensaft","amount":"20","unit":"ml"},{"name":"Wasser","amount":"150","unit":"ml"}]""",
                """[{"step":1,"instruction":"Alle Zutaten in Mixer geben."},{"step":2,"instruction":"60 Sekunden mixen."},{"step":3,"instruction":"Sofort trinken."}]""","vegan,detox,frühstück"),
            Recipe("r_020","Kichererbsen-Omelette","Veganes Omelette aus Kichererbsenmehl",null,10,8,1,290f,16f,32f,9f,50f,40f,10f,
                """[{"name":"Kichererbsenmehl","amount":"100","unit":"g"},{"name":"Wasser","amount":"150","unit":"ml"},{"name":"Paprika","amount":"50","unit":"g"},{"name":"Zwiebel","amount":"0.5","unit":"Stück"},{"name":"Kurkuma","amount":"0.5","unit":"TL"}]""",
                """[{"step":1,"instruction":"Kichererbsenmehl mit Wasser zu Teig rühren, würzen."},{"step":2,"instruction":"Gemüse kurz anbraten."},{"step":3,"instruction":"Teig in Pfanne geben, von beiden Seiten braten."}]""","vegan,high-protein,frühstück"),

            // MITTAGESSEN 21-45
            Recipe("r_021","Buddha Bowl mit Hähnchen","Vollständige Nährstoff-Bowl",null,10,20,1,480f,38f,45f,14f,40f,45f,15f,
                """[{"name":"Hähnchenbrust","amount":"150","unit":"g"},{"name":"Quinoa (gekocht)","amount":"100","unit":"g"},{"name":"Babyspinat","amount":"60","unit":"g"},{"name":"Süßkartoffel","amount":"100","unit":"g"},{"name":"Kichererbsen","amount":"80","unit":"g"},{"name":"Tahini-Dressing","amount":"30","unit":"ml"}]""",
                """[{"step":1,"instruction":"Süßkartoffel würfeln, bei 200°C 20 Min. rösten."},{"step":2,"instruction":"Hähnchen anbraten."},{"step":3,"instruction":"Quinoa kochen."},{"step":4,"instruction":"Alle Zutaten in Bowl anrichten, mit Dressing beträufeln."}]""","high-protein,meal-prep,mittag"),
            Recipe("r_022","Linsensuppe mit Kurkuma","Entzündungshemmende Suppe",null,10,25,2,320f,16f,52f,5f,65f,30f,5f,
                """[{"name":"Rote Linsen","amount":"150","unit":"g"},{"name":"Karotten","amount":"2","unit":"Stück"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Gemüsebrühe","amount":"800","unit":"ml"},{"name":"Kurkuma","amount":"1","unit":"TL"},{"name":"Kreuzkümmel","amount":"1","unit":"TL"},{"name":"Zitronensaft","amount":"20","unit":"ml"}]""",
                """[{"step":1,"instruction":"Zwiebel und Karotten anschwitzen."},{"step":2,"instruction":"Gewürze rösten."},{"step":3,"instruction":"Linsen und Brühe dazugeben, 25 Min. kochen."},{"step":4,"instruction":"Hälfte pürieren, mit Zitrone abschmecken."}]""","vegan,meal-prep,suppe,mittag,high-fiber"),
            Recipe("r_023","Lachs-Salat mit Avocado","Omega-3 Powersalat",null,10,12,1,420f,32f,12f,28f,50f,30f,20f,
                """[{"name":"Lachsfilet","amount":"150","unit":"g"},{"name":"Avocado","amount":"0.5","unit":"Stück"},{"name":"Rucola","amount":"60","unit":"g"},{"name":"Kirschtomaten","amount":"100","unit":"g"},{"name":"Gurke","amount":"0.5","unit":"Stück"},{"name":"Olivenöl","amount":"15","unit":"ml"}]""",
                """[{"step":1,"instruction":"Lachs würzen und braten (5-6 Min. pro Seite)."},{"step":2,"instruction":"Rucola, Tomaten und Gurke mischen."},{"step":3,"instruction":"Avocado würfeln, dazugeben."},{"step":4,"instruction":"Mit Öl und Zitrone anmachen, Lachs drauflegen."}]""","high-protein,low-carb,omega3,mittag"),
            Recipe("r_024","Vollkorn-Wrap mit Truthahn","Schneller Protein-Wrap",null,10,0,1,380f,28f,38f,10f,40f,45f,15f,
                """[{"name":"Vollkorn-Tortilla","amount":"1","unit":"Stück"},{"name":"Putenbrust","amount":"100","unit":"g"},{"name":"Salatblätter","amount":"3","unit":"Stück"},{"name":"Tomaten","amount":"50","unit":"g"},{"name":"Senf","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Tortilla ausbreiten."},{"step":2,"instruction":"Alle Zutaten auflegen."},{"step":3,"instruction":"Einrollen und diagonal halbieren."}]""","high-protein,schnell,mittag"),
            Recipe("r_025","Gazpacho","Kalte spanische Tomatensuppe",null,10,0,2,120f,4f,22f,3f,80f,18f,2f,
                """[{"name":"Tomaten","amount":"500","unit":"g"},{"name":"Gurke","amount":"0.5","unit":"Stück"},{"name":"Paprika","amount":"0.5","unit":"Stück"},{"name":"Knoblauch","amount":"1","unit":"Zehe"},{"name":"Olivenöl","amount":"2","unit":"EL"},{"name":"Essig","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Alle Zutaten grob hacken."},{"step":2,"instruction":"Im Mixer pürieren bis glatt."},{"step":3,"instruction":"Mit Salz, Pfeffer und Essig abschmecken."},{"step":4,"instruction":"Mindestens 1 Stunde kühlen."}]""","vegan,sommergericht,leicht,mittag"),
            Recipe("r_026","Hühnchen-Gemüse-Wok","Schneller Wok in 15 Minuten",null,10,15,1,390f,36f,32f,11f,45f,40f,15f,
                """[{"name":"Hähnchenbrust","amount":"150","unit":"g"},{"name":"Brokkoli","amount":"100","unit":"g"},{"name":"Paprika","amount":"100","unit":"g"},{"name":"Karotte","amount":"1","unit":"Stück"},{"name":"Sojasoße","amount":"2","unit":"EL"},{"name":"Sesamöl","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Hähnchen in Streifen schneiden."},{"step":2,"instruction":"Gemüse in Stücke schneiden."},{"step":3,"instruction":"Hähnchen scharf anbraten."},{"step":4,"instruction":"Gemüse dazugeben, mit Sojasoße und Sesam würzen."}]""","high-protein,schnell,mittag"),
            Recipe("r_027","Vegane Bowl mit Tempeh","Fermentiertes Sojaprotein Bowl",null,15,15,1,440f,24f,48f,16f,40f,48f,12f,
                """[{"name":"Tempeh","amount":"150","unit":"g"},{"name":"Quinoa","amount":"80","unit":"g"},{"name":"Edamame","amount":"80","unit":"g"},{"name":"Avocado","amount":"0.5","unit":"Stück"},{"name":"Sojasoße","amount":"2","unit":"EL"}]""",
                """[{"step":1,"instruction":"Tempeh in Scheiben schneiden, in Sojasoße marinieren."},{"step":2,"instruction":"Quinoa kochen."},{"step":3,"instruction":"Tempeh anbraten."},{"step":4,"instruction":"Alles in Bowl anrichten."}]""","vegan,meal-prep,mittag"),
            Recipe("r_028","Tomatensuppe mit Basilikum","Klassische Tomatensuppe",null,10,20,2,160f,5f,28f,4f,70f,25f,5f,
                """[{"name":"Tomaten (Dose)","amount":"800","unit":"g"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Knoblauch","amount":"2","unit":"Zehen"},{"name":"Gemüsebrühe","amount":"300","unit":"ml"},{"name":"Basilikum","amount":"1","unit":"Handvoll"}]""",
                """[{"step":1,"instruction":"Zwiebel und Knoblauch anschwitzen."},{"step":2,"instruction":"Tomaten und Brühe dazugeben, 15 Min. köcheln."},{"step":3,"instruction":"Pürieren, mit Basilikum servieren."}]""","vegan,suppe,mittag"),
            Recipe("r_029","Quinoa-Salat mediterran","Proteinreicher Salat mit Quinoa",null,10,15,2,360f,14f,50f,12f,50f,38f,12f,
                """[{"name":"Quinoa","amount":"150","unit":"g"},{"name":"Gurke","amount":"1","unit":"Stück"},{"name":"Tomaten","amount":"150","unit":"g"},{"name":"Oliven","amount":"50","unit":"g"},{"name":"Feta","amount":"60","unit":"g"},{"name":"Olivenöl","amount":"2","unit":"EL"}]""",
                """[{"step":1,"instruction":"Quinoa kochen und abkühlen lassen."},{"step":2,"instruction":"Gemüse würfeln."},{"step":3,"instruction":"Alles mischen, mit Olivenöl anmachen."},{"step":4,"instruction":"Feta darüber krümeln."}]""","vegan,meal-prep,mittag"),
            Recipe("r_030","Zucchini-Nudeln mit Pesto","Low-Carb Pasta-Alternative",null,10,5,1,280f,8f,18f,20f,65f,25f,10f,
                """[{"name":"Zucchini","amount":"2","unit":"Stück"},{"name":"Pesto","amount":"2","unit":"EL"},{"name":"Kirschtomaten","amount":"100","unit":"g"},{"name":"Parmesan","amount":"20","unit":"g"}]""",
                """[{"step":1,"instruction":"Zucchini zu Spiralen drehen oder hobeln."},{"step":2,"instruction":"Kurz in Pfanne wärmen (2 Min.)."},{"step":3,"instruction":"Pesto unterrühren."},{"step":4,"instruction":"Mit Tomaten und Parmesan servieren."}]""","low-carb,vegetarisch,schnell,mittag"),
            Recipe("r_031","Griechischer Salat mit Feta","Frischer mediterraner Salat",null,10,0,1,260f,10f,14f,18f,65f,20f,15f,
                """[{"name":"Tomaten","amount":"200","unit":"g"},{"name":"Gurke","amount":"0.5","unit":"Stück"},{"name":"Feta","amount":"60","unit":"g"},{"name":"Oliven","amount":"40","unit":"g"},{"name":"Rote Zwiebel","amount":"0.25","unit":"Stück"},{"name":"Olivenöl","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Gemüse in Stücke schneiden."},{"step":2,"instruction":"Feta würfeln."},{"step":3,"instruction":"Alles mischen, mit Öl und Oregano würzen."}]""","vegetarisch,leicht,mittag"),
            Recipe("r_032","Hähnchen-Caesar-Salat","Klassiker mit gegrilltem Hähnchen",null,10,15,1,370f,34f,14f,20f,45f,35f,20f,
                """[{"name":"Hähnchenbrust","amount":"150","unit":"g"},{"name":"Römersalat","amount":"100","unit":"g"},{"name":"Caesar-Dressing","amount":"30","unit":"ml"},{"name":"Croutons","amount":"20","unit":"g"},{"name":"Parmesan","amount":"15","unit":"g"}]""",
                """[{"step":1,"instruction":"Hähnchen grillen oder braten."},{"step":2,"instruction":"Salat waschen und zupfen."},{"step":3,"instruction":"Alles mischen, mit Dressing anmachen."}]""","high-protein,mittag"),
            Recipe("r_033","Minestrone Gemüsesuppe","Italienische Gemüsesuppe",null,15,30,4,220f,9f,38f,4f,65f,30f,5f,
                """[{"name":"Zucchini","amount":"1","unit":"Stück"},{"name":"Karotten","amount":"2","unit":"Stück"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Bohnen","amount":"200","unit":"g"},{"name":"Nudeln","amount":"80","unit":"g"},{"name":"Gemüsebrühe","amount":"1","unit":"L"}]""",
                """[{"step":1,"instruction":"Gemüse würfeln und anschwitzen."},{"step":2,"instruction":"Brühe und Tomaten dazugeben."},{"step":3,"instruction":"20 Min. kochen, Nudeln und Bohnen dazugeben."},{"step":4,"instruction":"Weitere 10 Min. kochen."}]""","vegan,meal-prep,suppe,mittag"),
            Recipe("r_034","Tofu-Erdnuss-Bowl","Asiatische Tofu-Bowl",null,15,15,1,460f,22f,44f,22f,40f,45f,15f,
                """[{"name":"Tofu (fest)","amount":"150","unit":"g"},{"name":"Reis","amount":"80","unit":"g"},{"name":"Erdnussbutter","amount":"2","unit":"EL"},{"name":"Sojasoße","amount":"2","unit":"EL"},{"name":"Limette","amount":"0.5","unit":"Stück"},{"name":"Chili","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Tofu pressen, würfeln und anbraten."},{"step":2,"instruction":"Reis kochen."},{"step":3,"instruction":"Erdnusssauce aus Butter, Sojasoße und Limette rühren."},{"step":4,"instruction":"Alles in Bowl anrichten."}]""","vegan,mittag"),
            Recipe("r_035","Türkisches Linsen-Tabbouleh","Erfrischender Bulgur-Salat",null,10,20,2,290f,12f,46f,7f,60f,35f,5f,
                """[{"name":"Rote Linsen","amount":"100","unit":"g"},{"name":"Bulgur","amount":"100","unit":"g"},{"name":"Tomaten","amount":"150","unit":"g"},{"name":"Petersilie","amount":"1","unit":"Bund"},{"name":"Zitronensaft","amount":"30","unit":"ml"},{"name":"Olivenöl","amount":"2","unit":"EL"}]""",
                """[{"step":1,"instruction":"Linsen kochen, Bulgur quellen lassen."},{"step":2,"instruction":"Tomaten würfeln, Petersilie hacken."},{"step":3,"instruction":"Alles mischen, mit Zitrone und Öl anmachen."}]""","vegan,meal-prep,mittag"),
            Recipe("r_036","Gurken-Dill-Suppe","Kalte Sommersuppe",null,5,0,2,110f,5f,12f,4f,75f,20f,5f,
                """[{"name":"Gurken","amount":"2","unit":"Stück"},{"name":"Joghurt","amount":"200","unit":"g"},{"name":"Dill","amount":"1","unit":"Bund"},{"name":"Knoblauch","amount":"1","unit":"Zehe"},{"name":"Zitronensaft","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Gurken grob hacken."},{"step":2,"instruction":"Mit Joghurt, Dill und Knoblauch pürieren."},{"step":3,"instruction":"Mit Zitronensaft abschmecken, kalt servieren."}]""","vegetarisch,leicht,suppe,mittag"),
            Recipe("r_037","Soba-Nudeln mit Edamame","Japanisch inspiriertes Nudelgericht",null,10,10,1,360f,18f,54f,8f,45f,45f,10f,
                """[{"name":"Soba-Nudeln","amount":"100","unit":"g"},{"name":"Edamame","amount":"100","unit":"g"},{"name":"Sojasoße","amount":"2","unit":"EL"},{"name":"Sesamöl","amount":"1","unit":"TL"},{"name":"Sesam","amount":"1","unit":"EL"},{"name":"Frühlingszwiebeln","amount":"2","unit":"Stück"}]""",
                """[{"step":1,"instruction":"Soba-Nudeln 4 Min. kochen."},{"step":2,"instruction":"Abgießen und abschrecken."},{"step":3,"instruction":"Mit Edamame und allen anderen Zutaten mischen."}]""","vegan,mittag"),
            Recipe("r_038","Chicken Shawarma Wrap","Würziger Hähnchen-Wrap",null,15,15,1,420f,32f,40f,12f,40f,45f,15f,
                """[{"name":"Hähnchenbrust","amount":"150","unit":"g"},{"name":"Vollkorn-Tortilla","amount":"1","unit":"Stück"},{"name":"Shawarma-Gewürz","amount":"1","unit":"EL"},{"name":"Joghurt","amount":"50","unit":"g"},{"name":"Tomate","amount":"1","unit":"Stück"},{"name":"Salat","amount":"2","unit":"Blätter"}]""",
                """[{"step":1,"instruction":"Hähnchen mit Gewürz marinieren und braten."},{"step":2,"instruction":"Tortilla mit Joghurt bestreichen."},{"step":3,"instruction":"Hähnchen, Tomate und Salat einrollen."}]""","high-protein,mittag"),
            Recipe("r_039","Kohl-Sellerie-Salat","Knackiger Salat mit Sellerie",null,10,0,2,140f,4f,22f,4f,75f,20f,5f,
                """[{"name":"Weißkohl","amount":"200","unit":"g"},{"name":"Sellerie","amount":"2","unit":"Stangen"},{"name":"Karotten","amount":"2","unit":"Stück"},{"name":"Apfel","amount":"0.5","unit":"Stück"},{"name":"Olivenöl","amount":"1","unit":"EL"},{"name":"Apfelessig","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Alle Zutaten fein raspeln oder hobeln."},{"step":2,"instruction":"Mit Öl und Essig mischen."},{"step":3,"instruction":"30 Min. ziehen lassen."}]""","vegan,leicht,mittag"),
            Recipe("r_040","Thunfisch-Avocado-Bowl","High-Protein Omega-3 Bowl",null,10,0,1,390f,34f,16f,22f,50f,30f,20f,
                """[{"name":"Thunfisch (Dose)","amount":"1","unit":"Dose (150g)"},{"name":"Avocado","amount":"0.5","unit":"Stück"},{"name":"Edamame","amount":"80","unit":"g"},{"name":"Rucola","amount":"60","unit":"g"},{"name":"Sojasoße","amount":"1","unit":"EL"},{"name":"Sesam","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Thunfisch abtropfen."},{"step":2,"instruction":"Avocado würfeln."},{"step":3,"instruction":"Alles in Bowl anrichten."},{"step":4,"instruction":"Mit Sojasoße und Sesam würzen."}]""","high-protein,low-carb,mittag"),
            Recipe("r_041","Blumenkohl-Curry","Mildes Gemüsecurry",null,15,20,2,280f,10f,34f,12f,55f,35f,10f,
                """[{"name":"Blumenkohl","amount":"400","unit":"g"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Kokosmilch (light)","amount":"200","unit":"ml"},{"name":"Curry-Paste","amount":"1","unit":"EL"},{"name":"Zwiebel","amount":"1","unit":"Stück"}]""",
                """[{"step":1,"instruction":"Zwiebeln anbraten."},{"step":2,"instruction":"Curry-Paste dazugeben, rösten."},{"step":3,"instruction":"Blumenkohl, Tomaten und Kokosmilch dazugeben."},{"step":4,"instruction":"20 Min. köcheln."}]""","vegan,mittag"),
            Recipe("r_042","Süßkartoffel-Kichererbsen-Salat","Sättigender Salat",null,15,20,2,340f,12f,56f,8f,55f,38f,7f,
                """[{"name":"Süßkartoffeln","amount":"300","unit":"g"},{"name":"Kichererbsen (Dose)","amount":"1","unit":"Dose"},{"name":"Babyspinat","amount":"80","unit":"g"},{"name":"Rote Zwiebel","amount":"0.5","unit":"Stück"},{"name":"Olivenöl","amount":"2","unit":"EL"},{"name":"Paprikapulver","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Süßkartoffeln würfeln, bei 200°C 20 Min. rösten."},{"step":2,"instruction":"Kichererbsen abtropfen."},{"step":3,"instruction":"Spinat und Zwiebel mischen."},{"step":4,"instruction":"Alles mit Öl anmachen."}]""","vegan,meal-prep,mittag"),
            Recipe("r_043","Lachs-Quinoa-Salat","Premium-Salat mit Lachs",null,10,15,1,460f,36f,38f,16f,40f,42f,18f,
                """[{"name":"Lachsfilet","amount":"150","unit":"g"},{"name":"Quinoa","amount":"80","unit":"g"},{"name":"Gurke","amount":"0.5","unit":"Stück"},{"name":"Kirschtomaten","amount":"100","unit":"g"},{"name":"Dill","amount":"nach","unit":"Geschmack"}]""",
                """[{"step":1,"instruction":"Quinoa kochen."},{"step":2,"instruction":"Lachs anbraten."},{"step":3,"instruction":"Gemüse würfeln."},{"step":4,"instruction":"Alles mischen und mit Dill garnieren."}]""","high-protein,omega3,mittag"),
            Recipe("r_044","Brokkoli-Cheddar-Suppe","Cremige Gemüsesuppe",null,10,20,2,260f,12f,22f,14f,50f,35f,15f,
                """[{"name":"Brokkoli","amount":"400","unit":"g"},{"name":"Cheddar (gerieben)","amount":"60","unit":"g"},{"name":"Gemüsebrühe","amount":"600","unit":"ml"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Milch","amount":"100","unit":"ml"}]""",
                """[{"step":1,"instruction":"Brokkoli und Zwiebel kochen."},{"step":2,"instruction":"Pürieren."},{"step":3,"instruction":"Milch und Cheddar einrühren, nicht mehr kochen."}]""","vegetarisch,suppe,mittag"),
            Recipe("r_045","Mexikanische Black-Bean-Bowl","Würzige Bohnen-Bowl",null,15,15,1,400f,18f,60f,9f,45f,45f,10f,
                """[{"name":"Schwarze Bohnen (Dose)","amount":"1","unit":"Dose"},{"name":"Reis","amount":"80","unit":"g"},{"name":"Mais","amount":"80","unit":"g"},{"name":"Avocado","amount":"0.5","unit":"Stück"},{"name":"Limette","amount":"0.5","unit":"Stück"},{"name":"Koriander","amount":"nach","unit":"Geschmack"}]""",
                """[{"step":1,"instruction":"Reis kochen."},{"step":2,"instruction":"Bohnen und Mais erwärmen."},{"step":3,"instruction":"Alles in Bowl anrichten."},{"step":4,"instruction":"Mit Avocado, Limette und Koriander toppen."}]""","vegan,high-fiber,mittag"),

            // ABENDESSEN 46-70
            Recipe("r_046","Ofenhähnchen mit Gemüse","Einfaches Ofenrezept",null,10,35,2,380f,42f,22f,14f,40f,45f,15f,
                """[{"name":"Hähnchenschenkel","amount":"300","unit":"g"},{"name":"Karotten","amount":"2","unit":"Stück"},{"name":"Zucchini","amount":"1","unit":"Stück"},{"name":"Paprika","amount":"1","unit":"Stück"},{"name":"Olivenöl","amount":"20","unit":"ml"},{"name":"Kräuter der Provence","amount":"2","unit":"TL"}]""",
                """[{"step":1,"instruction":"Ofen auf 200°C vorheizen."},{"step":2,"instruction":"Gemüse grob würfeln."},{"step":3,"instruction":"Hähnchen mit Öl und Kräutern marinieren."},{"step":4,"instruction":"Auf Blech legen, 35 Min. backen."}]""","high-protein,meal-prep,abend"),
            Recipe("r_047","Gebratener Lachs mit Brokkoli","Schnelles gesundes Abendessen",null,5,15,1,440f,38f,14f,26f,40f,40f,20f,
                """[{"name":"Lachsfilet","amount":"200","unit":"g"},{"name":"Brokkoli","amount":"200","unit":"g"},{"name":"Zitrone","amount":"0.5","unit":"Stück"},{"name":"Knoblauch","amount":"2","unit":"Zehen"},{"name":"Olivenöl","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Brokkoli 5 Min. dämpfen."},{"step":2,"instruction":"Lachs mit Zitrone und Knoblauch würzen."},{"step":3,"instruction":"In Olivenöl 4 Min. pro Seite braten."},{"step":4,"instruction":"Mit Brokkoli servieren."}]""","high-protein,low-carb,omega3,abend"),
            Recipe("r_048","Vegane Bolognese (Linsen)","Herzhafte Pasta ohne Fleisch",null,10,30,2,380f,18f,56f,8f,35f,55f,10f,
                """[{"name":"Rote Linsen","amount":"150","unit":"g"},{"name":"Spaghetti","amount":"160","unit":"g"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Knoblauch","amount":"2","unit":"Zehen"},{"name":"Tomatenmark","amount":"2","unit":"EL"}]""",
                """[{"step":1,"instruction":"Zwiebeln und Knoblauch anschwitzen."},{"step":2,"instruction":"Tomatenmark rösten."},{"step":3,"instruction":"Linsen und Tomaten dazugeben, 25 Min. köcheln."},{"step":4,"instruction":"Mit Spaghetti servieren."}]""","vegan,pasta,abend"),
            Recipe("r_049","Hackfleisch-Paprika-Pfanne","Schnelle Pfanne mit Hackfleisch",null,10,15,2,420f,32f,24f,20f,40f,42f,18f,
                """[{"name":"Hackfleisch (gemischt)","amount":"300","unit":"g"},{"name":"Paprika","amount":"2","unit":"Stück"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Knoblauch","amount":"2","unit":"Zehen"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"}]""",
                """[{"step":1,"instruction":"Hackfleisch scharf anbraten."},{"step":2,"instruction":"Zwiebeln und Knoblauch dazugeben."},{"step":3,"instruction":"Paprika und Tomaten einrühren."},{"step":4,"instruction":"15 Min. köcheln lassen."}]""","high-protein,schnell,abend"),
            Recipe("r_050","Zitronenhähnchen mit Quinoa","Frisches Hähnchenrezept",null,10,25,2,460f,40f,42f,12f,35f,48f,17f,
                """[{"name":"Hähnchenbrust","amount":"300","unit":"g"},{"name":"Quinoa","amount":"150","unit":"g"},{"name":"Zitrone","amount":"1","unit":"Stück"},{"name":"Knoblauch","amount":"3","unit":"Zehen"},{"name":"Rosmarin","amount":"2","unit":"Zweige"},{"name":"Olivenöl","amount":"2","unit":"EL"}]""",
                """[{"step":1,"instruction":"Hähnchen mit Zitrone, Knoblauch und Rosmarin marinieren."},{"step":2,"instruction":"Hähnchen 20 Min. bei 200°C backen oder braten."},{"step":3,"instruction":"Quinoa kochen."},{"step":4,"instruction":"Mit Salat servieren."}]""","high-protein,abend"),
            Recipe("r_051","Gebratener Tofu mit Brokkoli","Veganes Wok-Gericht",null,10,15,1,310f,20f,22f,16f,55f,35f,10f,
                """[{"name":"Tofu (fest)","amount":"200","unit":"g"},{"name":"Brokkoli","amount":"200","unit":"g"},{"name":"Sojasoße","amount":"3","unit":"EL"},{"name":"Knoblauch","amount":"2","unit":"Zehen"},{"name":"Sesamöl","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Tofu pressen und würfeln."},{"step":2,"instruction":"Scharf anbraten."},{"step":3,"instruction":"Brokkoli und Knoblauch dazugeben."},{"step":4,"instruction":"Mit Sojasoße und Sesam würzen."}]""","vegan,low-carb,abend"),
            Recipe("r_052","Lachs-Curry mit Kokosmilch","Cremiges Fisch-Curry",null,15,20,2,480f,34f,24f,28f,30f,45f,25f,
                """[{"name":"Lachsfilet","amount":"300","unit":"g"},{"name":"Kokosmilch","amount":"400","unit":"ml"},{"name":"Curry-Paste","amount":"2","unit":"EL"},{"name":"Tomaten","amount":"200","unit":"g"},{"name":"Spinat","amount":"100","unit":"g"},{"name":"Reis","amount":"150","unit":"g"}]""",
                """[{"step":1,"instruction":"Curry-Paste anrösten."},{"step":2,"instruction":"Kokosmilch und Tomaten dazugeben."},{"step":3,"instruction":"Lachs einlegen, 10 Min. garen."},{"step":4,"instruction":"Spinat einrühren, mit Reis servieren."}]""","high-protein,abend"),
            Recipe("r_053","Miso-Suppe mit Tofu","Leichte japanische Suppe",null,5,10,2,160f,12f,14f,6f,70f,25f,5f,
                """[{"name":"Miso-Paste","amount":"3","unit":"EL"},{"name":"Tofu (seidig)","amount":"200","unit":"g"},{"name":"Wakame-Algen","amount":"10","unit":"g"},{"name":"Frühlingszwiebeln","amount":"2","unit":"Stück"},{"name":"Wasser","amount":"800","unit":"ml"}]""",
                """[{"step":1,"instruction":"Wasser erhitzen (nicht kochen)."},{"step":2,"instruction":"Miso darin auflösen."},{"step":3,"instruction":"Tofu würfeln, Algen einweichen."},{"step":4,"instruction":"Alles in Brühe geben, mit Frühlingszwiebeln servieren."}]""","vegan,leicht,abend"),
            Recipe("r_054","Gefüllte Paprika mit Hackfleisch","Klassiker der deutschen Küche",null,20,40,2,360f,28f,26f,16f,40f,42f,18f,
                """[{"name":"Paprika","amount":"4","unit":"Stück"},{"name":"Hackfleisch","amount":"250","unit":"g"},{"name":"Reis","amount":"80","unit":"g"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Zwiebel","amount":"1","unit":"Stück"}]""",
                """[{"step":1,"instruction":"Paprika Deckel abschneiden, Kerne entfernen."},{"step":2,"instruction":"Hackfleisch mit Reis, Zwiebeln würzen."},{"step":3,"instruction":"Paprika füllen."},{"step":4,"instruction":"Im Ofen bei 180°C 40 Min. backen."}]""","abend"),
            Recipe("r_055","Gemüsecurry mit Kichererbsen","Veganes mildes Curry",null,15,25,2,340f,14f,52f,9f,55f,38f,7f,
                """[{"name":"Kichererbsen (Dose)","amount":"1","unit":"Dose"},{"name":"Blumenkohl","amount":"300","unit":"g"},{"name":"Kokosmilch (light)","amount":"400","unit":"ml"},{"name":"Curry-Pulver","amount":"2","unit":"EL"},{"name":"Tomaten","amount":"200","unit":"g"}]""",
                """[{"step":1,"instruction":"Blumenkohl in Röschen teilen."},{"step":2,"instruction":"Curry-Pulver anrösten."},{"step":3,"instruction":"Alle anderen Zutaten dazugeben."},{"step":4,"instruction":"25 Min. köcheln."}]""","vegan,abend"),
            Recipe("r_056","Hähnchenstreifen mit Süßkartoffel","Meal-Prep Klassiker",null,15,25,2,440f,36f,46f,10f,40f,45f,15f,
                """[{"name":"Hähnchenbrust","amount":"300","unit":"g"},{"name":"Süßkartoffeln","amount":"400","unit":"g"},{"name":"Olivenöl","amount":"2","unit":"EL"},{"name":"Paprikapulver","amount":"1","unit":"TL"},{"name":"Knoblauch","amount":"2","unit":"Zehen"}]""",
                """[{"step":1,"instruction":"Süßkartoffeln würfeln, bei 200°C 20 Min. rösten."},{"step":2,"instruction":"Hähnchen in Streifen schneiden, würzen."},{"step":3,"instruction":"Hähnchen anbraten."},{"step":4,"instruction":"Zusammen servieren."}]""","high-protein,meal-prep,abend"),
            Recipe("r_057","Forelle aus dem Ofen","Zarter Fisch im Ofen",null,10,20,1,320f,36f,8f,16f,50f,35f,15f,
                """[{"name":"Forelle","amount":"1","unit":"Stück (350g)"},{"name":"Zitrone","amount":"1","unit":"Stück"},{"name":"Dill","amount":"1","unit":"Bund"},{"name":"Knoblauch","amount":"2","unit":"Zehen"},{"name":"Olivenöl","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Forelle waschen und trocken tupfen."},{"step":2,"instruction":"Mit Zitrone, Dill und Knoblauch füllen."},{"step":3,"instruction":"Mit Öl einreiben."},{"step":4,"instruction":"Bei 180°C 20 Min. backen."}]""","high-protein,low-carb,abend"),
            Recipe("r_058","Vegane Tacos mit Black Beans","Bunte Tacos",null,15,15,2,380f,16f,58f,9f,40f,50f,10f,
                """[{"name":"Schwarze Bohnen (Dose)","amount":"1","unit":"Dose"},{"name":"Mais","amount":"80","unit":"g"},{"name":"Paprika","amount":"1","unit":"Stück"},{"name":"Taco-Schalen","amount":"8","unit":"Stück"},{"name":"Avocado","amount":"1","unit":"Stück"},{"name":"Limette","amount":"1","unit":"Stück"}]""",
                """[{"step":1,"instruction":"Bohnen mit Mais und Paprika erwärmen."},{"step":2,"instruction":"Mit Gewürzen abschmecken."},{"step":3,"instruction":"In Taco-Schalen füllen."},{"step":4,"instruction":"Mit Avocado und Limette toppen."}]""","vegan,abend"),
            Recipe("r_059","Hähnchen-Brokkoli-Auflauf","Käsiger Auflauf",null,15,30,2,400f,38f,18f,20f,40f,40f,20f,
                """[{"name":"Hähnchenbrust","amount":"300","unit":"g"},{"name":"Brokkoli","amount":"300","unit":"g"},{"name":"Sahne (Kochsahne)","amount":"150","unit":"ml"},{"name":"Käse (gerieben)","amount":"80","unit":"g"},{"name":"Knoblauch","amount":"2","unit":"Zehen"}]""",
                """[{"step":1,"instruction":"Hähnchen würfeln, anbraten."},{"step":2,"instruction":"Brokkoli blanchieren."},{"step":3,"instruction":"Alles in Auflaufform legen."},{"step":4,"instruction":"Sahne darübergießen, Käse drauf, 30 Min. backen."}]""","high-protein,abend"),
            Recipe("r_060","Grain Bowl mit geröstetem Gemüse","Vollwert Grain Bowl",null,20,30,2,380f,14f,58f,10f,50f,42f,8f,
                """[{"name":"Gemischte Körner (Farro, Quinoa)","amount":"150","unit":"g"},{"name":"Rote Bete","amount":"200","unit":"g"},{"name":"Süßkartoffel","amount":"200","unit":"g"},{"name":"Kürbiskerne","amount":"30","unit":"g"},{"name":"Tahini","amount":"2","unit":"EL"}]""",
                """[{"step":1,"instruction":"Körner kochen."},{"step":2,"instruction":"Gemüse würfeln und bei 200°C rösten."},{"step":3,"instruction":"Alles in Bowl anrichten."},{"step":4,"instruction":"Mit Tahini beträufeln."}]""","vegan,abend"),
            Recipe("r_061","Spaghetti Carbonara (leicht)","Cremige Pasta ohne Sahne",null,10,15,2,460f,24f,58f,14f,25f,58f,17f,
                """[{"name":"Spaghetti","amount":"200","unit":"g"},{"name":"Eier","amount":"3","unit":"Stück"},{"name":"Parmesan","amount":"50","unit":"g"},{"name":"Speck (Pancetta)","amount":"80","unit":"g"},{"name":"Schwarzer Pfeffer","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Spaghetti al dente kochen."},{"step":2,"instruction":"Speck kross braten."},{"step":3,"instruction":"Eier mit Parmesan verquirlen."},{"step":4,"instruction":"Heiße Nudeln von der Flamme mit Ei-Mischung rühren."}]""","vegetarisch,abend"),
            Recipe("r_062","Kabeljau-Filet mit Salsa","Leichter Fisch mit Tomaten-Salsa",null,10,15,2,280f,32f,12f,10f,65f,28f,7f,
                """[{"name":"Kabeljaufilet","amount":"300","unit":"g"},{"name":"Tomaten","amount":"200","unit":"g"},{"name":"Paprika","amount":"1","unit":"Stück"},{"name":"Rote Zwiebel","amount":"0.5","unit":"Stück"},{"name":"Koriander","amount":"nach","unit":"Geschmack"},{"name":"Limette","amount":"1","unit":"Stück"}]""",
                """[{"step":1,"instruction":"Salsa aus Tomaten, Paprika, Zwiebel und Koriander mischen."},{"step":2,"instruction":"Fisch würzen und braten."},{"step":3,"instruction":"Mit Salsa und Limette servieren."}]""","high-protein,leicht,abend"),
            Recipe("r_063","Glasnudeln mit Gemüse","Asiatische Glasnudeln",null,10,10,1,310f,10f,52f,7f,50f,43f,7f,
                """[{"name":"Glasnudeln","amount":"100","unit":"g"},{"name":"Brokkoli","amount":"150","unit":"g"},{"name":"Karotten","amount":"1","unit":"Stück"},{"name":"Paprika","amount":"1","unit":"Stück"},{"name":"Sojasoße","amount":"3","unit":"EL"},{"name":"Sesamöl","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Glasnudeln in heißem Wasser einweichen."},{"step":2,"instruction":"Gemüse wokken."},{"step":3,"instruction":"Nudeln dazugeben."},{"step":4,"instruction":"Mit Sojasoße und Sesam würzen."}]""","vegan,abend"),
            Recipe("r_064","Pute mit Pilzsoße","Mageres Fleisch mit Soße",null,10,25,2,370f,40f,14f,16f,40f,40f,20f,
                """[{"name":"Putenbrust","amount":"300","unit":"g"},{"name":"Champignons","amount":"250","unit":"g"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Sahne (Kochsahne)","amount":"150","unit":"ml"},{"name":"Thymian","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Pute anbraten."},{"step":2,"instruction":"Pilze und Zwiebeln dazugeben."},{"step":3,"instruction":"Sahne und Thymian einrühren."},{"step":4,"instruction":"20 Min. köcheln."}]""","high-protein,abend"),
            Recipe("r_065","Schwarze Bohnen Enchiladas","Mexikanischer Klassiker",null,20,25,2,420f,18f,60f,12f,35f,52f,13f,
                """[{"name":"Tortillas","amount":"4","unit":"Stück"},{"name":"Schwarze Bohnen","amount":"1","unit":"Dose"},{"name":"Käse (gerieben)","amount":"80","unit":"g"},{"name":"Enchilada-Soße","amount":"300","unit":"ml"},{"name":"Mais","amount":"100","unit":"g"}]""",
                """[{"step":1,"instruction":"Bohnen mit Mais würzen."},{"step":2,"instruction":"Tortillas füllen und rollen."},{"step":3,"instruction":"In Auflaufform legen."},{"step":4,"instruction":"Mit Soße und Käse 25 Min. backen."}]""","vegan,abend"),
            Recipe("r_066","Spinat-Ricotta-Cannelloni","Gefüllte Nudelrollen",null,20,35,2,390f,20f,44f,14f,30f,50f,20f,
                """[{"name":"Cannelloni","amount":"12","unit":"Stück"},{"name":"Ricotta","amount":"250","unit":"g"},{"name":"Blattspinat","amount":"300","unit":"g"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Parmesan","amount":"40","unit":"g"}]""",
                """[{"step":1,"instruction":"Spinat dünsten und hacken."},{"step":2,"instruction":"Mit Ricotta mischen, würzen."},{"step":3,"instruction":"Cannelloni füllen."},{"step":4,"instruction":"In Tomatensoße legen, 35 Min. bei 180°C backen."}]""","vegetarisch,abend"),
            Recipe("r_067","Hähnchen-Shawarma Teller","Naher Osten Stil",null,20,20,2,450f,38f,38f,14f,35f,48f,17f,
                """[{"name":"Hähnchenbrust","amount":"300","unit":"g"},{"name":"Joghurt","amount":"150","unit":"g"},{"name":"Knoblauch","amount":"3","unit":"Zehen"},{"name":"Shawarma-Gewürzmischung","amount":"2","unit":"EL"},{"name":"Fladenbrot","amount":"2","unit":"Stück"}]""",
                """[{"step":1,"instruction":"Hähnchen in Joghurt und Gewürzen marinieren."},{"step":2,"instruction":"Braten oder im Ofen garen."},{"step":3,"instruction":"Mit Fladenbrot und Salat servieren."}]""","high-protein,abend"),
            Recipe("r_068","Kürbiscremesuppe","Samtene Herbstsuppe",null,15,25,3,200f,6f,32f,6f,70f,25f,5f,
                """[{"name":"Hokkaido-Kürbis","amount":"600","unit":"g"},{"name":"Karotte","amount":"1","unit":"Stück"},{"name":"Gemüsebrühe","amount":"600","unit":"ml"},{"name":"Kokosmilch","amount":"100","unit":"ml"},{"name":"Ingwer","amount":"2","unit":"cm"}]""",
                """[{"step":1,"instruction":"Kürbis und Karotte würfeln."},{"step":2,"instruction":"Mit Ingwer in Brühe 20 Min. kochen."},{"step":3,"instruction":"Pürieren, Kokosmilch einrühren."},{"step":4,"instruction":"Abschmecken."}]""","vegan,suppe,abend"),
            Recipe("r_069","Zucchini-Hackfleisch-Pfanne","Low-Carb Pfanne",null,10,15,2,380f,30f,14f,22f,50f,30f,20f,
                """[{"name":"Hackfleisch","amount":"250","unit":"g"},{"name":"Zucchini","amount":"2","unit":"Stück"},{"name":"Tomaten (Dose)","amount":"400","unit":"g"},{"name":"Zwiebel","amount":"1","unit":"Stück"},{"name":"Knoblauch","amount":"2","unit":"Zehen"}]""",
                """[{"step":1,"instruction":"Hackfleisch anbraten."},{"step":2,"instruction":"Zucchini, Zwiebeln und Knoblauch dazugeben."},{"step":3,"instruction":"Tomaten einrühren."},{"step":4,"instruction":"15 Min. köcheln."}]""","low-carb,abend"),
            Recipe("r_070","Jakobsmuscheln mit Erbsenpüree","Elegantes Seafood-Gericht",null,15,20,1,290f,24f,24f,10f,50f,38f,12f,
                """[{"name":"Jakobsmuscheln","amount":"8","unit":"Stück"},{"name":"Tiefkühl-Erbsen","amount":"300","unit":"g"},{"name":"Butter","amount":"2","unit":"EL"},{"name":"Minze","amount":"5","unit":"Blätter"},{"name":"Olivenöl","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Erbsen kochen, mit Butter und Minze pürieren."},{"step":2,"instruction":"Jakobsmuscheln trocken tupfen."},{"step":3,"instruction":"In Öl 90 Sek. pro Seite scharf braten."},{"step":4,"instruction":"Auf Erbsenpüree servieren."}]""","seafood,abend"),

            // SNACKS 71-85
            Recipe("r_071","Energy Balls (Dattel & Kakao)","Gesunde Energiebälle",null,15,0,12,120f,4f,16f,5f,30f,55f,15f,
                """[{"name":"Datteln","amount":"100","unit":"g"},{"name":"Haferflocken","amount":"80","unit":"g"},{"name":"Kakaopulver","amount":"2","unit":"EL"},{"name":"Mandelbutter","amount":"2","unit":"EL"},{"name":"Kokosflocken","amount":"20","unit":"g"}]""",
                """[{"step":1,"instruction":"Alle Zutaten im Mixer pürieren."},{"step":2,"instruction":"Zu Bällchen rollen."},{"step":3,"instruction":"In Kokosflocken wälzen."},{"step":4,"instruction":"2h kühlen."}]""","vegan,snack,meal-prep"),
            Recipe("r_072","Hummus mit Gemüsesticks","Selbstgemachter Hummus",null,10,0,2,150f,6f,18f,6f,65f,30f,5f,
                """[{"name":"Kichererbsen (Dose)","amount":"1","unit":"Dose"},{"name":"Tahini","amount":"2","unit":"EL"},{"name":"Zitronensaft","amount":"1","unit":"EL"},{"name":"Knoblauch","amount":"1","unit":"Zehe"},{"name":"Gemüse zum Dippen","amount":"200","unit":"g"}]""",
                """[{"step":1,"instruction":"Kichererbsen, Tahini, Zitrone und Knoblauch mixen."},{"step":2,"instruction":"Mit Wasser verdünnen."},{"step":3,"instruction":"Gemüse als Sticks schneiden."}]""","vegan,snack"),
            Recipe("r_073","Protein-Riegel (Haferflocken)","Hausgemachte Protein-Riegel",null,15,20,8,180f,10f,22f,6f,30f,58f,12f,
                """[{"name":"Haferflocken","amount":"200","unit":"g"},{"name":"Proteinpulver","amount":"60","unit":"g"},{"name":"Honig","amount":"3","unit":"EL"},{"name":"Nussbutter","amount":"4","unit":"EL"},{"name":"Dunkle Schokolade","amount":"50","unit":"g"}]""",
                """[{"step":1,"instruction":"Haferflocken mit Protein, Honig und Nussbutter mischen."},{"step":2,"instruction":"In Form pressen."},{"step":3,"instruction":"Schokolade schmelzen, darüber träufeln."},{"step":4,"instruction":"2h kühlen, in Riegel schneiden."}]""","high-protein,snack,meal-prep"),
            Recipe("r_074","Apfel mit Mandelbutter","Einfacher sättigender Snack",null,2,0,1,160f,4f,24f,7f,55f,35f,10f,
                """[{"name":"Apfel","amount":"1","unit":"Stück"},{"name":"Mandelbutter","amount":"1","unit":"EL"}]""",
                """[{"step":1,"instruction":"Apfel in Scheiben schneiden."},{"step":2,"instruction":"Mit Mandelbutter dippen."}]""","snack,schnell"),
            Recipe("r_075","Edamame mit Meersalz","Japanischer Protein-Snack",null,2,5,1,100f,8f,8f,4f,70f,25f,5f,
                """[{"name":"Tiefkühl-Edamame","amount":"150","unit":"g"},{"name":"Meersalz","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Edamame in Salzwasser 5 Min. kochen."},{"step":2,"instruction":"Abgießen, mit Meersalz bestreuen."}]""","vegan,snack"),
            Recipe("r_076","Frozen Yogurt Beeren","Erfrischender Joghurt-Snack",null,5,120,4,130f,6f,22f,2f,60f,35f,5f,
                """[{"name":"Griechischer Joghurt","amount":"400","unit":"g"},{"name":"Gemischte Beeren","amount":"200","unit":"g"},{"name":"Honig","amount":"2","unit":"EL"},{"name":"Vanille","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Joghurt mit Honig und Vanille mischen."},{"step":2,"instruction":"Beeren unterheben."},{"step":3,"instruction":"Auf Backpapier flach ausstreichen."},{"step":4,"instruction":"2h einfrieren, in Stücke brechen."}]""","vegetarisch,snack,dessert"),
            Recipe("r_077","Kokos-Protein-Bällchen","Kokos-Energy-Balls",null,10,60,10,140f,8f,14f,6f,35f,52f,13f,
                """[{"name":"Proteinpulver (Vanille)","amount":"60","unit":"g"},{"name":"Haferflocken","amount":"100","unit":"g"},{"name":"Kokosflocken","amount":"40","unit":"g"},{"name":"Kokosöl","amount":"2","unit":"EL"},{"name":"Honig","amount":"2","unit":"EL"}]""",
                """[{"step":1,"instruction":"Alle Zutaten mischen."},{"step":2,"instruction":"Zu Bällchen formen."},{"step":3,"instruction":"1h kühlen."}]""","snack,meal-prep"),
            Recipe("r_078","Reiswaffeln mit Avocado","Leichter Avocado-Snack",null,5,0,1,120f,2f,18f,5f,60f,32f,8f,
                """[{"name":"Reiswaffeln","amount":"3","unit":"Stück"},{"name":"Avocado","amount":"0.25","unit":"Stück"},{"name":"Zitronensaft","amount":"1","unit":"Spritzer"},{"name":"Salz und Pfeffer","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Avocado zerdrücken und würzen."},{"step":2,"instruction":"Auf Reiswaffeln streichen."}]""","vegan,schnell,snack"),
            Recipe("r_079","Nussmix (Mandeln/Cashews)","Nährstoffreicher Nuss-Mix",null,2,0,1,170f,5f,8f,14f,20f,25f,55f,
                """[{"name":"Mandeln","amount":"20","unit":"g"},{"name":"Cashews","amount":"15","unit":"g"},{"name":"Walnüsse","amount":"10","unit":"g"}]""",
                """[{"step":1,"instruction":"Nüsse mischen."},{"step":2,"instruction":"In Portionsbeutel abwiegen."}]""","vegan,snack"),
            Recipe("r_080","Smoothie (Beeren & Protein)","Cremiger Protein-Shake",null,5,0,1,200f,16f,28f,3f,55f,40f,5f,
                """[{"name":"Gefrorene Beeren","amount":"150","unit":"g"},{"name":"Proteinpulver","amount":"30","unit":"g"},{"name":"Mandelmilch","amount":"250","unit":"ml"},{"name":"Banane","amount":"0.5","unit":"Stück"}]""",
                """[{"step":1,"instruction":"Alle Zutaten in Mixer geben."},{"step":2,"instruction":"Cremig mixen."},{"step":3,"instruction":"Sofort trinken."}]""","high-protein,snack"),
            Recipe("r_081","Guacamole mit Nachos","Tex-Mex Treat",null,10,0,2,220f,3f,24f,14f,25f,30f,45f,
                """[{"name":"Avocados","amount":"2","unit":"Stück"},{"name":"Tomate","amount":"1","unit":"Stück"},{"name":"Limette","amount":"1","unit":"Stück"},{"name":"Koriander","amount":"nach","unit":"Geschmack"},{"name":"Nachos","amount":"40","unit":"g"}]""",
                """[{"step":1,"instruction":"Avocados zerdrücken."},{"step":2,"instruction":"Tomate würfeln."},{"step":3,"instruction":"Mit Limette, Salz und Koriander mischen."},{"step":4,"instruction":"Mit Nachos servieren."}]""","vegan,treat,snack"),
            Recipe("r_082","Joghurt-Parfait","Geschichtetes Joghurt-Dessert",null,5,0,1,180f,12f,24f,4f,55f,40f,5f,
                """[{"name":"Griechischer Joghurt","amount":"150","unit":"g"},{"name":"Granola","amount":"30","unit":"g"},{"name":"Frische Beeren","amount":"80","unit":"g"},{"name":"Honig","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Joghurt in Glas füllen."},{"step":2,"instruction":"Granola und Beeren schichten."},{"step":3,"instruction":"Mit Honig abschließen."}]""","vegetarisch,schnell,snack"),
            Recipe("r_083","Gebratene Kichererbsen","Knuspriger Protein-Snack",null,5,25,2,160f,8f,24f,4f,55f,38f,7f,
                """[{"name":"Kichererbsen (Dose)","amount":"1","unit":"Dose"},{"name":"Olivenöl","amount":"1","unit":"EL"},{"name":"Paprikapulver","amount":"1","unit":"TL"},{"name":"Kreuzkümmel","amount":"0.5","unit":"TL"},{"name":"Meersalz","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Ofen auf 200°C vorheizen."},{"step":2,"instruction":"Kichererbsen abtropfen, trocken tupfen."},{"step":3,"instruction":"Mit Öl und Gewürzen mischen."},{"step":4,"instruction":"25 Min. backen bis knusprig."}]""","vegan,snack,meal-prep"),
            Recipe("r_084","Ei-Weiß-Würfel","Reines Protein",null,2,0,1,80f,10f,0f,4f,75f,20f,5f,
                """[{"name":"Hartgekochte Eier","amount":"2","unit":"Stück"},{"name":"Salz und Pfeffer","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Eier kochen (10 Min. hart)."},{"step":2,"instruction":"Eiweiß vom Eigelb trennen."},{"step":3,"instruction":"Eiweiß würfeln, würzen."}]""","high-protein,low-carb,snack"),
            Recipe("r_085","Ingwer-Kurkuma-Shot","Wellness-Shot",null,2,0,2,30f,0f,7f,0f,85f,15f,0f,
                """[{"name":"Frischer Ingwer","amount":"30","unit":"g"},{"name":"Kurkuma (frisch oder Pulver)","amount":"5","unit":"g"},{"name":"Zitronensaft","amount":"30","unit":"ml"},{"name":"Pfeffer","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Ingwer und Kurkuma mit Zitronensaft pressen oder mixen."},{"step":2,"instruction":"Mit Pfeffer würzen."},{"step":3,"instruction":"Als Shot auf nüchternen Magen trinken."}]""","vegan,detox,snack"),

            // DESSERTS 86-100
            Recipe("r_086","Protein-Muffins Blaubeere","High-Protein Muffins",null,10,20,6,160f,10f,20f,4f,30f,58f,12f,
                """[{"name":"Haferflocken","amount":"100","unit":"g"},{"name":"Proteinpulver","amount":"40","unit":"g"},{"name":"Ei","amount":"2","unit":"Stück"},{"name":"Joghurt","amount":"150","unit":"g"},{"name":"Blaubeeren","amount":"100","unit":"g"},{"name":"Backpulver","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Ofen auf 180°C vorheizen."},{"step":2,"instruction":"Alle Zutaten außer Blaubeeren mischen."},{"step":3,"instruction":"Blaubeeren unterheben."},{"step":4,"instruction":"In Muffinformen 20 Min. backen."}]""","high-protein,backen,dessert"),
            Recipe("r_087","Nice Cream (Bananen-Eis)","Veganes Eis aus Bananen",null,5,120,2,140f,2f,34f,1f,60f,38f,2f,
                """[{"name":"Reife Bananen (gefroren)","amount":"3","unit":"Stück"}]""",
                """[{"step":1,"instruction":"Bananen in Scheiben einfrieren."},{"step":2,"instruction":"Im Mixer zu cremigem Eis pürieren."},{"step":3,"instruction":"Sofort servieren oder weitere 30 Min. frieren."}]""","vegan,dessert"),
            Recipe("r_088","Dunkle-Schokolade-Mousse","Luftiges Schoko-Mousse",null,15,60,4,210f,5f,20f,12f,15f,45f,40f,
                """[{"name":"Dunkle Schokolade (70%)","amount":"100","unit":"g"},{"name":"Eier","amount":"3","unit":"Stück"},{"name":"Zucker","amount":"2","unit":"EL"},{"name":"Vanilleextrakt","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Schokolade schmelzen, abkühlen lassen."},{"step":2,"instruction":"Eigelb einrühren."},{"step":3,"instruction":"Eiweiß mit Zucker steif schlagen."},{"step":4,"instruction":"Eiweiß unterheben, kalt stellen."}]""","vegetarisch,dessert"),
            Recipe("r_089","Erdbeeren mit Balsamico","Fruchtiger Genuss",null,5,0,2,90f,1f,20f,0f,75f,23f,2f,
                """[{"name":"Erdbeeren","amount":"300","unit":"g"},{"name":"Balsamico-Essig","amount":"2","unit":"EL"},{"name":"Zucker","amount":"1","unit":"TL"},{"name":"Schwarzer Pfeffer","amount":"1","unit":"Prise"}]""",
                """[{"step":1,"instruction":"Erdbeeren halbieren."},{"step":2,"instruction":"Mit Balsamico, Zucker und Pfeffer mischen."},{"step":3,"instruction":"30 Min. marinieren."}]""","vegan,leicht,dessert"),
            Recipe("r_090","Kürbis-Protein-Kuchen","Gesunder Herbstkuchen",null,15,50,10,200f,12f,24f,6f,30f,55f,15f,
                """[{"name":"Kürbispüree","amount":"200","unit":"g"},{"name":"Vollkornmehl","amount":"150","unit":"g"},{"name":"Proteinpulver","amount":"60","unit":"g"},{"name":"Ei","amount":"2","unit":"Stück"},{"name":"Honig","amount":"3","unit":"EL"},{"name":"Zimt und Gewürze","amount":"2","unit":"TL"}]""",
                """[{"step":1,"instruction":"Ofen auf 175°C vorheizen."},{"step":2,"instruction":"Alle Zutaten zu Teig rühren."},{"step":3,"instruction":"In Kastenform 50 Min. backen."}]""","backen,high-protein,dessert"),
            Recipe("r_091","Chia-Pudding-Torte (no bake)","Rohe Torte ohne Backen",null,20,240,8,240f,8f,30f,10f,30f,55f,15f,
                """[{"name":"Chiasamen","amount":"100","unit":"g"},{"name":"Kokosmilch","amount":"400","unit":"ml"},{"name":"Datteln","amount":"80","unit":"g"},{"name":"Cashews","amount":"100","unit":"g"},{"name":"Zitronensaft","amount":"30","unit":"ml"},{"name":"Beeren zum Belegen","amount":"150","unit":"g"}]""",
                """[{"step":1,"instruction":"Boden aus Datteln und Cashews mixen."},{"step":2,"instruction":"In Form pressen."},{"step":3,"instruction":"Chiasamen mit Kokosmilch mischen."},{"step":4,"instruction":"Auf Boden geben, 4h kühlen."},{"step":5,"instruction":"Mit Beeren belegen."}]""","vegan,dessert"),
            Recipe("r_092","Frozen Yogurt Bark","Gefroren bunte Joghurtplatte",null,10,120,6,120f,5f,18f,3f,55f,40f,5f,
                """[{"name":"Griechischer Joghurt","amount":"500","unit":"g"},{"name":"Honig","amount":"2","unit":"EL"},{"name":"Gemischte Beeren","amount":"150","unit":"g"},{"name":"Granola","amount":"40","unit":"g"}]""",
                """[{"step":1,"instruction":"Joghurt mit Honig mischen."},{"step":2,"instruction":"Auf Backpapier ausstreichen."},{"step":3,"instruction":"Beeren und Granola darüber."},{"step":4,"instruction":"2h einfrieren, in Stücke brechen."}]""","vegetarisch,dessert"),
            Recipe("r_093","Haferflocken-Cookies","Gesunde Cookies ohne Butter",null,10,15,12,180f,5f,26f,6f,25f,58f,17f,
                """[{"name":"Haferflocken","amount":"200","unit":"g"},{"name":"Reife Bananen","amount":"2","unit":"Stück"},{"name":"Dunkle Schokolade (chips)","amount":"50","unit":"g"},{"name":"Nüsse","amount":"40","unit":"g"},{"name":"Zimt","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Bananen zerdrücken."},{"step":2,"instruction":"Haferflocken und alle anderen Zutaten einrühren."},{"step":3,"instruction":"Cookies formen."},{"step":4,"instruction":"Bei 180°C 15 Min. backen."}]""","vegan,backen,dessert"),
            Recipe("r_094","Kokos-Panna-Cotta","Cremiges Dessert mit Kokos",null,10,120,4,190f,3f,22f,10f,30f,50f,20f,
                """[{"name":"Kokosmilch","amount":"400","unit":"ml"},{"name":"Agar-Agar","amount":"2","unit":"TL"},{"name":"Honig","amount":"3","unit":"EL"},{"name":"Vanille","amount":"1","unit":"TL"},{"name":"Mango zum Servieren","amount":"150","unit":"g"}]""",
                """[{"step":1,"instruction":"Kokosmilch mit Agar-Agar aufkochen."},{"step":2,"instruction":"Honig und Vanille einrühren."},{"step":3,"instruction":"In Förmchen füllen, 2h kühlen."},{"step":4,"instruction":"Stürzen, mit Mango servieren."}]""","vegan,dessert"),
            Recipe("r_095","Protein-Brownies","Schokoladig und proteinreich",null,15,25,9,200f,14f,22f,6f,20f,58f,22f,
                """[{"name":"Schwarze Bohnen (Dose)","amount":"1","unit":"Dose"},{"name":"Kakaopulver","amount":"4","unit":"EL"},{"name":"Proteinpulver","amount":"60","unit":"g"},{"name":"Ei","amount":"2","unit":"Stück"},{"name":"Honig","amount":"4","unit":"EL"},{"name":"Backpulver","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Ofen auf 180°C vorheizen."},{"step":2,"instruction":"Bohnen abtropfen und pürieren."},{"step":3,"instruction":"Alle anderen Zutaten unterrühren."},{"step":4,"instruction":"In Backform 25 Min. backen."}]""","high-protein,backen,dessert"),
            Recipe("r_096","Watermelon Slushie","Erfrischender Sommer-Drink",null,5,30,2,60f,1f,14f,0f,85f,14f,1f,
                """[{"name":"Wassermelone","amount":"500","unit":"g"},{"name":"Limettensaft","amount":"2","unit":"EL"},{"name":"Minze","amount":"5","unit":"Blätter"},{"name":"Eis","amount":"1","unit":"Handvoll"}]""",
                """[{"step":1,"instruction":"Wassermelone würfeln, einfrieren."},{"step":2,"instruction":"Mit Lime und Minze mixen."},{"step":3,"instruction":"Sofort servieren."}]""","vegan,leicht,dessert"),
            Recipe("r_097","Gesunder Käsekuchen","Leichter Quark-Käsekuchen",null,20,60,10,220f,14f,22f,8f,25f,55f,20f,
                """[{"name":"Magerquark","amount":"500","unit":"g"},{"name":"Ei","amount":"3","unit":"Stück"},{"name":"Honig","amount":"4","unit":"EL"},{"name":"Vanille","amount":"1","unit":"TL"},{"name":"Vollkorn-Keksboden","amount":"150","unit":"g"}]""",
                """[{"step":1,"instruction":"Ofen auf 160°C vorheizen."},{"step":2,"instruction":"Keksboden in Form drücken."},{"step":3,"instruction":"Quark, Ei, Honig und Vanille mixen."},{"step":4,"instruction":"Auf Boden geben, 60 Min. backen."}]""","vegetarisch,backen,dessert"),
            Recipe("r_098","Dattel-Kakaoballen","Natürlich süß ohne Zucker",null,10,30,12,150f,3f,24f,5f,30f,52f,18f,
                """[{"name":"Medjool-Datteln","amount":"150","unit":"g"},{"name":"Kakaopulver","amount":"3","unit":"EL"},{"name":"Mandeln","amount":"80","unit":"g"},{"name":"Kokosflocken","amount":"30","unit":"g"}]""",
                """[{"step":1,"instruction":"Datteln und Mandeln im Mixer mixen."},{"step":2,"instruction":"Kakao einrühren."},{"step":3,"instruction":"Bällchen formen."},{"step":4,"instruction":"In Kokosflocken wälzen."}]""","vegan,dessert,snack"),
            Recipe("r_099","Mango-Kokosnuss-Parfait","Tropisches Schichtdessert",null,10,0,2,200f,4f,32f,7f,45f,45f,10f,
                """[{"name":"Mango","amount":"200","unit":"g"},{"name":"Griechischer Joghurt","amount":"200","unit":"g"},{"name":"Kokosflocken","amount":"20","unit":"g"},{"name":"Limettenzeste","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Mango würfeln."},{"step":2,"instruction":"Joghurt mit Limettenzeste mischen."},{"step":3,"instruction":"Joghurt und Mango schichten."},{"step":4,"instruction":"Mit Kokosflocken toppen."}]""","vegan,dessert"),
            Recipe("r_100","Erdbeer-Rhabarber-Kompott","Fruchtiges Kompott",null,5,15,4,80f,1f,18f,0f,80f,18f,2f,
                """[{"name":"Erdbeeren","amount":"300","unit":"g"},{"name":"Rhabarber","amount":"200","unit":"g"},{"name":"Zucker","amount":"2","unit":"EL"},{"name":"Vanille","amount":"1","unit":"TL"}]""",
                """[{"step":1,"instruction":"Erdbeeren halbieren, Rhabarber in Stücke schneiden."},{"step":2,"instruction":"Mit Zucker und Vanille aufkochen."},{"step":3,"instruction":"15 Min. köcheln bis Rhabarber weich."},{"step":4,"instruction":"Warm oder kalt servieren."}]""","vegan,leicht,dessert")
        )
        recipeRepository.seedRecipes(erweiterungRecipes)
    }
}
