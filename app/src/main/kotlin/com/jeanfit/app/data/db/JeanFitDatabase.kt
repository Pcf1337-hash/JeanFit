package com.jeanfit.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jeanfit.app.data.db.dao.FoodDao
import com.jeanfit.app.data.db.dao.GamificationDao
import com.jeanfit.app.data.db.dao.LessonDao
import com.jeanfit.app.data.db.dao.RecipeDao
import com.jeanfit.app.data.db.dao.UserProfileDao
import com.jeanfit.app.data.db.dao.WeightDao
import com.jeanfit.app.data.db.entities.Achievement
import com.jeanfit.app.data.db.entities.Course
import com.jeanfit.app.data.db.entities.DailyTask
import com.jeanfit.app.data.db.entities.FoodItem
import com.jeanfit.app.data.db.entities.FoodLogEntry
import com.jeanfit.app.data.db.entities.Lesson
import com.jeanfit.app.data.db.entities.LessonProgress
import com.jeanfit.app.data.db.entities.MealPlan
import com.jeanfit.app.data.db.entities.Recipe
import com.jeanfit.app.data.db.entities.Streak
import com.jeanfit.app.data.db.entities.UserProfile
import com.jeanfit.app.data.db.entities.WeightEntry

@Database(
    entities = [
        UserProfile::class,
        FoodItem::class,
        FoodLogEntry::class,
        WeightEntry::class,
        Course::class,
        Lesson::class,
        LessonProgress::class,
        DailyTask::class,
        Streak::class,
        Achievement::class,
        Recipe::class,
        MealPlan::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class JeanFitDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun foodDao(): FoodDao
    abstract fun weightDao(): WeightDao
    abstract fun lessonDao(): LessonDao
    abstract fun gamificationDao(): GamificationDao
    abstract fun recipeDao(): RecipeDao

    companion object {
        const val DATABASE_NAME = "jeanfit.db"
    }
}
