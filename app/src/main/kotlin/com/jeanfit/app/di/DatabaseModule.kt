package com.jeanfit.app.di

import android.content.Context
import androidx.room.Room
import com.jeanfit.app.data.db.JeanFitDatabase
import com.jeanfit.app.data.db.dao.CoachDao
import com.jeanfit.app.data.db.dao.FoodDao
import com.jeanfit.app.data.db.dao.GamificationDao
import com.jeanfit.app.data.db.dao.LessonDao
import com.jeanfit.app.data.db.dao.RecipeDao
import com.jeanfit.app.data.db.dao.UserProfileDao
import com.jeanfit.app.data.db.dao.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JeanFitDatabase {
        return Room.databaseBuilder(
            context,
            JeanFitDatabase::class.java,
            JeanFitDatabase.DATABASE_NAME
        )
            .addMigrations(JeanFitDatabase.MIGRATION_1_2, JeanFitDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides @Singleton
    fun provideUserProfileDao(db: JeanFitDatabase): UserProfileDao = db.userProfileDao()

    @Provides @Singleton
    fun provideFoodDao(db: JeanFitDatabase): FoodDao = db.foodDao()

    @Provides @Singleton
    fun provideWeightDao(db: JeanFitDatabase): WeightDao = db.weightDao()

    @Provides @Singleton
    fun provideLessonDao(db: JeanFitDatabase): LessonDao = db.lessonDao()

    @Provides @Singleton
    fun provideGamificationDao(db: JeanFitDatabase): GamificationDao = db.gamificationDao()

    @Provides @Singleton
    fun provideRecipeDao(db: JeanFitDatabase): RecipeDao = db.recipeDao()

    @Provides @Singleton
    fun provideCoachDao(db: JeanFitDatabase): CoachDao = db.coachDao()
}
