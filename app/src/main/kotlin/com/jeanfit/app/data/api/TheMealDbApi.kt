package com.jeanfit.app.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApi {
    @GET("api/json/v1/1/categories.php")
    suspend fun getCategories(): Response<MealCategoriesResponse>

    @GET("api/json/v1/1/filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): Response<MealListResponse>

    @GET("api/json/v1/1/lookup.php")
    suspend fun getMealById(@Query("i") id: String): Response<MealDetailResponse>

    @GET("api/json/v1/1/search.php")
    suspend fun searchMeals(@Query("s") query: String): Response<MealListResponse>

    @GET("api/json/v1/1/random.php")
    suspend fun getRandomMeal(): Response<MealDetailResponse>
}
// BaseUrl: "https://www.themealdb.com/"

@JsonClass(generateAdapter = true)
data class MealCategoriesResponse(
    @Json(name = "categories") val categories: List<MealCategory> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MealCategory(
    @Json(name = "idCategory") val id: String,
    @Json(name = "strCategory") val name: String,
    @Json(name = "strCategoryThumb") val imageUrl: String,
    @Json(name = "strCategoryDescription") val description: String
)

@JsonClass(generateAdapter = true)
data class MealListResponse(
    @Json(name = "meals") val meals: List<MealSummary>? = null
)

@JsonClass(generateAdapter = true)
data class MealSummary(
    @Json(name = "idMeal") val id: String,
    @Json(name = "strMeal") val name: String,
    @Json(name = "strMealThumb") val imageUrl: String
)

@JsonClass(generateAdapter = true)
data class MealDetailResponse(
    @Json(name = "meals") val meals: List<MealDetail>? = null
)

@JsonClass(generateAdapter = true)
data class MealDetail(
    @Json(name = "idMeal") val id: String,
    @Json(name = "strMeal") val name: String,
    @Json(name = "strCategory") val category: String? = null,
    @Json(name = "strArea") val cuisine: String? = null,
    @Json(name = "strInstructions") val instructions: String? = null,
    @Json(name = "strMealThumb") val imageUrl: String? = null,
    @Json(name = "strYoutube") val youtubeUrl: String? = null,
    @Json(name = "strIngredient1") val ingredient1: String? = null,
    @Json(name = "strIngredient2") val ingredient2: String? = null,
    @Json(name = "strIngredient3") val ingredient3: String? = null,
    @Json(name = "strIngredient4") val ingredient4: String? = null,
    @Json(name = "strIngredient5") val ingredient5: String? = null,
    @Json(name = "strIngredient6") val ingredient6: String? = null,
    @Json(name = "strIngredient7") val ingredient7: String? = null,
    @Json(name = "strIngredient8") val ingredient8: String? = null,
    @Json(name = "strIngredient9") val ingredient9: String? = null,
    @Json(name = "strIngredient10") val ingredient10: String? = null,
    @Json(name = "strMeasure1") val measure1: String? = null,
    @Json(name = "strMeasure2") val measure2: String? = null,
    @Json(name = "strMeasure3") val measure3: String? = null,
    @Json(name = "strMeasure4") val measure4: String? = null,
    @Json(name = "strMeasure5") val measure5: String? = null,
    @Json(name = "strMeasure6") val measure6: String? = null,
    @Json(name = "strMeasure7") val measure7: String? = null,
    @Json(name = "strMeasure8") val measure8: String? = null,
    @Json(name = "strMeasure9") val measure9: String? = null,
    @Json(name = "strMeasure10") val measure10: String? = null,
) {
    fun getIngredients(): List<Pair<String, String>> {
        val ings = listOf(ingredient1, ingredient2, ingredient3, ingredient4, ingredient5,
            ingredient6, ingredient7, ingredient8, ingredient9, ingredient10)
        val measures = listOf(measure1, measure2, measure3, measure4, measure5,
            measure6, measure7, measure8, measure9, measure10)
        return ings.zip(measures).mapNotNull { (ing, mea) ->
            if (!ing.isNullOrBlank()) Pair(ing.trim(), mea?.trim() ?: "") else null
        }
    }

    fun toRecipe(): com.jeanfit.app.data.db.entities.Recipe {
        val cat = category ?: "Miscellaneous"
        val estimatedCal = estimateCaloriesByCategory(cat)
        val ingredientsJson = getIngredients().joinToString(",", "[", "]") { (name, measure) ->
            """{"name":"$name","amount":"$measure","unit":""}"""
        }
        return com.jeanfit.app.data.db.entities.Recipe(
            recipeId = "mealdb_$id",
            title = name,
            description = "${category ?: ""} · ${cuisine ?: ""}".trim(' ', '·', ' '),
            imageUrl = imageUrl,
            prepTimeMinutes = 15,
            cookTimeMinutes = estimateCookTime(cat),
            servings = 2,
            totalCaloriesPerServing = estimatedCal.toFloat(),
            proteinPerServing = estimatedCal * 0.25f / 4f,
            carbsPerServing = estimatedCal * 0.45f / 4f,
            fatPerServing = estimatedCal * 0.30f / 9f,
            greenPercent = if (cat in listOf("Vegetarian", "Vegan", "Side")) 60f else 35f,
            yellowPercent = if (cat in listOf("Pasta", "Breakfast")) 50f else 40f,
            orangePercent = if (cat in listOf("Dessert", "Beef", "Pork")) 30f else 25f,
            ingredientsJson = ingredientsJson,
            stepsJson = buildStepsJson(instructions),
            tags = "$cat,${cuisine ?: ""}".lowercase().trim(',')
        )
    }
}

private fun estimateCaloriesByCategory(category: String): Int = when (category) {
    "Beef", "Pork", "Lamb" -> 520
    "Chicken" -> 400
    "Seafood" -> 350
    "Vegetarian", "Vegan" -> 300
    "Pasta" -> 420
    "Breakfast" -> 320
    "Dessert" -> 450
    "Side", "Starter" -> 200
    else -> 380
}

private fun estimateCookTime(category: String): Int = when (category) {
    "Beef", "Pork", "Lamb" -> 45
    "Chicken" -> 30
    "Seafood" -> 20
    "Pasta" -> 25
    "Dessert" -> 35
    "Side", "Starter", "Salad" -> 10
    else -> 30
}

private fun buildStepsJson(instructions: String?): String {
    if (instructions.isNullOrBlank()) return "[]"
    val steps = instructions.split(Regex("\r?\n+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .take(20)
        .mapIndexed { i, step ->
            val escaped = step.replace("\"", "\\\"")
            """{"step":${i + 1},"instruction":"$escaped"}"""
        }
    return steps.joinToString(",", "[", "]")
}
