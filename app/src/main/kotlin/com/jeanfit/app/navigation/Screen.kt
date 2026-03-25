package com.jeanfit.app.navigation

sealed class Screen(val route: String) {
    // Onboarding
    data object Welcome : Screen("onboarding/welcome")
    data object GoalWeight : Screen("onboarding/goal_weight")
    data object CurrentWeight : Screen("onboarding/current_weight")
    data object GenderAgeHeight : Screen("onboarding/gender_age_height")
    data object ActivityLevel : Screen("onboarding/activity_level")
    data object HealthConditions : Screen("onboarding/health_conditions")
    data object Motivation : Screen("onboarding/motivation")
    data object CalorieCalculation : Screen("onboarding/calorie_calculation")
    data object ProfileCreated : Screen("onboarding/profile_created")

    // Main Bottom Nav
    data object Home : Screen("main/home")
    data object Progress : Screen("main/progress")
    data object Tools : Screen("main/tools")
    data object Learn : Screen("main/learn")

    // Progress sub-screens
    data object WeightHistory : Screen("main/progress/history")

    // Food logging
    data object FoodSearch : Screen("main/food_search/{mealType}") {
        fun createRoute(mealType: String) = "main/food_search/$mealType"
    }
    data object BarcodeScanner : Screen("main/barcode_scanner/{mealType}") {
        fun createRoute(mealType: String) = "main/barcode_scanner/$mealType"
    }

    // Learn sub-screens
    data object LessonList : Screen("main/learn/course/{courseId}") {
        fun createRoute(courseId: String) = "main/learn/course/$courseId"
    }
    data object LessonReader : Screen("main/learn/lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "main/learn/lesson/$lessonId"
    }

    // Tools sub-screens
    data object RecipeList : Screen("main/tools/recipes")
    data object RecipeDetail : Screen("main/tools/recipe/{recipeId}") {
        fun createRoute(recipeId: String) = "main/tools/recipe/$recipeId"
    }
    data object MealPlanner : Screen("main/tools/meal_planner")
}
