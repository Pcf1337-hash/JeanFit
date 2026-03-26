package com.jeanfit.app.data.repository

import com.jeanfit.app.data.api.TheMealDbApi
import com.jeanfit.app.data.db.dao.RecipeDao
import com.jeanfit.app.data.db.entities.MealPlan
import com.jeanfit.app.data.db.entities.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val dao: RecipeDao,
    private val mealDbApi: TheMealDbApi
) {
    fun getAllRecipes(): Flow<List<Recipe>> = dao.getAllRecipes()
    fun searchRecipes(query: String): Flow<List<Recipe>> = dao.searchRecipes(query)
    fun getFavoriteRecipes(): Flow<List<Recipe>> = dao.getFavoriteRecipes()
    suspend fun getRecipeById(id: String): Recipe? = dao.getRecipeById(id)
    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = dao.toggleFavorite(id, isFavorite)
    suspend fun insertRecipe(recipe: Recipe) = dao.insertRecipe(recipe)
    suspend fun seedRecipes(recipes: List<Recipe>) = dao.insertRecipes(recipes)
    suspend fun deleteEnglishMealDbRecipes() = dao.deleteMealDbRecipes()
    fun getMealPlanForDay(dayEpoch: Long): Flow<List<MealPlan>> = dao.getMealPlanForDay(dayEpoch)
    suspend fun upsertMealPlan(plan: MealPlan) = dao.upsertMealPlan(plan)
    suspend fun deleteMealPlan(dayEpoch: Long, mealType: String) = dao.deleteMealPlan(dayEpoch, mealType)

    /** Lädt Rezepte einer TheMealDB-Kategorie und cached sie in Room */
    suspend fun preloadCategory(category: String) {
        try {
            val listResponse = mealDbApi.getMealsByCategory(category)
            val summaries = listResponse.body()?.meals ?: return
            summaries.take(15).forEach { summary ->
                val mealId = summary.id
                if (dao.getRecipeById("mealdb_$mealId") == null) {
                    try {
                        val detail = mealDbApi.getMealById(mealId)
                        detail.body()?.meals?.firstOrNull()?.let { meal ->
                            dao.insertRecipe(meal.toRecipe())
                        }
                    } catch (_: Exception) { }
                }
            }
        } catch (_: Exception) { }
    }

    /** Suche: erst local, dann TheMealDB API */
    suspend fun searchRemote(query: String) {
        try {
            val response = mealDbApi.searchMeals(query)
            val summaries = response.body()?.meals ?: return
            summaries.take(10).forEach { summary ->
                if (dao.getRecipeById("mealdb_${summary.id}") == null) {
                    val detail = mealDbApi.getMealById(summary.id)
                    detail.body()?.meals?.firstOrNull()?.let { dao.insertRecipe(it.toRecipe()) }
                }
            }
        } catch (_: Exception) { }
    }

    suspend fun getRandomRecipe(): Recipe? {
        return try {
            val response = mealDbApi.getRandomMeal()
            response.body()?.meals?.firstOrNull()?.toRecipe()
        } catch (_: Exception) { null }
    }
}
