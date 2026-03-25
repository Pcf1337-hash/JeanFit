package com.jeanfit.app.data.repository

import com.jeanfit.app.data.db.dao.RecipeDao
import com.jeanfit.app.data.db.entities.MealPlan
import com.jeanfit.app.data.db.entities.Recipe
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val dao: RecipeDao
) {
    fun getAllRecipes(): Flow<List<Recipe>> = dao.getAllRecipes()
    fun searchRecipes(query: String): Flow<List<Recipe>> = dao.searchRecipes(query)
    fun getFavoriteRecipes(): Flow<List<Recipe>> = dao.getFavoriteRecipes()
    suspend fun getRecipeById(id: String): Recipe? = dao.getRecipeById(id)
    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = dao.toggleFavorite(id, isFavorite)
    suspend fun insertRecipe(recipe: Recipe) = dao.insertRecipe(recipe)
    suspend fun seedRecipes(recipes: List<Recipe>) = dao.insertRecipes(recipes)
    fun getMealPlanForDay(dayEpoch: Long): Flow<List<MealPlan>> = dao.getMealPlanForDay(dayEpoch)
    suspend fun upsertMealPlan(plan: MealPlan) = dao.upsertMealPlan(plan)
}
