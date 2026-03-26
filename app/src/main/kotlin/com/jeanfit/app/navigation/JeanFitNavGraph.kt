package com.jeanfit.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jeanfit.app.ui.coach.CoachChatScreen
import com.jeanfit.app.ui.home.HomeScreen
import com.jeanfit.app.ui.learn.CourseMapScreen
import com.jeanfit.app.ui.learn.LessonListScreen
import com.jeanfit.app.ui.learn.LessonReaderScreen
import com.jeanfit.app.ui.onboarding.ActivityLevelScreen
import com.jeanfit.app.ui.onboarding.CalorieCalculationScreen
import com.jeanfit.app.ui.onboarding.CurrentWeightScreen
import com.jeanfit.app.ui.onboarding.GenderAgeHeightScreen
import com.jeanfit.app.ui.onboarding.GoalWeightScreen
import com.jeanfit.app.ui.onboarding.HealthConditionsScreen
import com.jeanfit.app.ui.onboarding.MotivationScreen
import com.jeanfit.app.ui.onboarding.ProfileCreatedScreen
import com.jeanfit.app.ui.onboarding.WelcomeScreen
import com.jeanfit.app.ui.progress.ProgressScreen
import com.jeanfit.app.ui.progress.WeightHistoryScreen
import com.jeanfit.app.ui.tools.MealPlannerScreen
import com.jeanfit.app.ui.tools.RecipeDetailScreen
import com.jeanfit.app.ui.tools.RecipeListScreen
import com.jeanfit.app.ui.tools.ToolsScreen
import com.jeanfit.app.ui.foodlog.BarcodeScannerScreen
import com.jeanfit.app.ui.foodlog.FoodSearchScreen

@Composable
fun JeanFitNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) }
    ) {
        // Onboarding
        composable(Screen.Welcome.route) {
            WelcomeScreen(onNext = { navController.navigate(Screen.GoalWeight.route) })
        }
        composable(Screen.GoalWeight.route) {
            GoalWeightScreen(
                onNext = { navController.navigate(Screen.CurrentWeight.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CurrentWeight.route) {
            CurrentWeightScreen(
                onNext = { navController.navigate(Screen.GenderAgeHeight.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.GenderAgeHeight.route) {
            GenderAgeHeightScreen(
                onNext = { navController.navigate(Screen.ActivityLevel.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ActivityLevel.route) {
            ActivityLevelScreen(
                onNext = { navController.navigate(Screen.HealthConditions.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.HealthConditions.route) {
            HealthConditionsScreen(
                onNext = { navController.navigate(Screen.Motivation.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Motivation.route) {
            MotivationScreen(
                onNext = { navController.navigate(Screen.CalorieCalculation.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.CalorieCalculation.route) {
            CalorieCalculationScreen(
                onNext = { navController.navigate(Screen.ProfileCreated.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ProfileCreated.route) {
            ProfileCreatedScreen(
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // Main
        composable(Screen.Home.route) {
            HomeScreen(
                onLogFood = { mealType ->
                    navController.navigate(Screen.FoodSearch.createRoute(mealType))
                }
            )
        }
        composable(Screen.Progress.route) {
            ProgressScreen(
                onViewHistory = { navController.navigate(Screen.WeightHistory.route) }
            )
        }
        composable(Screen.WeightHistory.route) {
            WeightHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Tools.route) {
            ToolsScreen(
                onNavigateRecipes = { navController.navigate(Screen.RecipeList.route) },
                onNavigateMealPlanner = { navController.navigate(Screen.MealPlanner.route) },
                onLogFood = { navController.navigate(Screen.FoodSearch.createRoute("snack")) }
            )
        }
        composable(Screen.RecipeList.route) {
            RecipeListScreen(
                onBack = { navController.popBackStack() },
                onRecipeDetail = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) }
            )
        }
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStack ->
            RecipeDetailScreen(
                recipeId = backStack.arguments?.getString("recipeId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.MealPlanner.route) {
            MealPlannerScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Learn.route) {
            CourseMapScreen(
                onCourseSelected = { courseId ->
                    navController.navigate(Screen.LessonList.createRoute(courseId))
                }
            )
        }
        composable(
            route = Screen.LessonList.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStack ->
            LessonListScreen(
                courseId = backStack.arguments?.getString("courseId") ?: "",
                onBack = { navController.popBackStack() },
                onLessonSelected = { lessonId ->
                    navController.navigate(Screen.LessonReader.createRoute(lessonId))
                }
            )
        }
        composable(
            route = Screen.LessonReader.route,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStack ->
            LessonReaderScreen(
                lessonId = backStack.arguments?.getString("lessonId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.FoodSearch.route,
            arguments = listOf(navArgument("mealType") { type = NavType.StringType })
        ) { backStack ->
            FoodSearchScreen(
                mealType = backStack.arguments?.getString("mealType") ?: "snack",
                onBack = { navController.popBackStack() },
                onScanBarcode = { mealType ->
                    navController.navigate(Screen.BarcodeScanner.createRoute(mealType))
                }
            )
        }
        composable(
            route = Screen.BarcodeScanner.route,
            arguments = listOf(navArgument("mealType") { type = NavType.StringType })
        ) { backStack ->
            BarcodeScannerScreen(
                mealType = backStack.arguments?.getString("mealType") ?: "snack",
                onBack = { navController.popBackStack() },
                onBarcodeFound = { navController.popBackStack() }
            )
        }

        // KI-Coach
        composable(
            route = Screen.Coach.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300)) }
        ) {
            CoachChatScreen(onBack = { navController.popBackStack() })
        }
    }
}
