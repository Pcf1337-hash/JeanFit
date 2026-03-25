package com.jeanfit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jeanfit.app.data.db.entities.MealPlan
import com.jeanfit.app.data.db.entities.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE recipeId = :id LIMIT 1")
    suspend fun getRecipeById(id: String): Recipe?

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchRecipes(query: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteRecipes(): Flow<List<Recipe>>

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE recipeId = :id")
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMealPlan(plan: MealPlan)

    @Query("SELECT * FROM meal_plan WHERE dateEpochDay = :dayEpoch")
    fun getMealPlanForDay(dayEpoch: Long): Flow<List<MealPlan>>

    @Query("DELETE FROM meal_plan WHERE dateEpochDay = :dayEpoch AND mealType = :mealType")
    suspend fun deleteMealPlan(dayEpoch: Long, mealType: String)
}
