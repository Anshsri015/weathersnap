package com.example.weathersnap.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weathersnap.ui.screens.camera.CameraScreen
import com.example.weathersnap.ui.screens.createreport.CreateReportScreen
import com.example.weathersnap.ui.screens.reports.SavedReportsScreen
import com.example.weathersnap.ui.screens.weather.WeatherScreen

//Route Definitions

object Routes {
    const val WEATHER       = "weather"
    const val CREATE_REPORT = "create_report/{cityName}/{temperature}/{condition}/{humidity}/{windSpeed}/{pressure}/{latitude}/{longitude}"
    const val CAMERA        = "camera"
    const val SAVED_REPORTS = "saved_reports"

    fun createReport(
        cityName: String,
        temperature: String,
        condition: String,
        humidity: String,
        windSpeed: String,
        pressure: String,
        latitude: Double,
        longitude: Double
    ) = "create_report/$cityName/$temperature/$condition/$humidity/$windSpeed/$pressure/$latitude/$longitude"
}

//NavHost

@Composable
fun WeatherSnapNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.WEATHER,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        }
    ) {
        //Weather Screen
        composable(Routes.WEATHER) {
            WeatherScreen(
                onNavigateToCreateReport = { cityName, temperature, condition, humidity, windSpeed, pressure, lat, lon ->
                    navController.navigate(
                        Routes.createReport(cityName, temperature, condition, humidity, windSpeed, pressure, lat, lon)
                    )
                },
                onNavigateToSavedReports = {
                    navController.navigate(Routes.SAVED_REPORTS)
                }
            )
        }

        //Create Report Screen
        composable(
            route = Routes.CREATE_REPORT,
            arguments = listOf(
                navArgument("cityName")    { type = NavType.StringType },
                navArgument("temperature") { type = NavType.StringType },
                navArgument("condition")   { type = NavType.StringType },
                navArgument("humidity")    { type = NavType.StringType },
                navArgument("windSpeed")   { type = NavType.StringType },
                navArgument("pressure")    { type = NavType.StringType },
                navArgument("latitude")    { type = NavType.StringType },
                navArgument("longitude")   { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            CreateReportScreen(
                cityName    = args?.getString("cityName")    ?: "",
                temperature = args?.getString("temperature") ?: "",
                condition   = args?.getString("condition")   ?: "",
                humidity    = args?.getString("humidity")    ?: "",
                windSpeed   = args?.getString("windSpeed")   ?: "",
                pressure    = args?.getString("pressure")    ?: "",
                latitude    = args?.getString("latitude")?.toDoubleOrNull()  ?: 0.0,
                longitude   = args?.getString("longitude")?.toDoubleOrNull() ?: 0.0,
                onNavigateToCamera = {
                    navController.navigate(Routes.CAMERA)
                },
                onNavigateToSavedReports = {
                    navController.navigate(Routes.SAVED_REPORTS) {
                        popUpTo(Routes.WEATHER)
                    }
                },
                onBack = { navController.popBackStack() },
                // Camera result is passed back via SavedStateHandle
                savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
            )
        }

        //Camera Screen
        composable(Routes.CAMERA) {
            CameraScreen(
                onImageCaptured = { imagePath, originalSize, compressedSize ->
                    // Pass result back to CreateReport via SavedStateHandle
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("imagePath",      imagePath)
                        set("originalSize",   originalSize)
                        set("compressedSize", compressedSize)
                    }
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        //Saved Reports Screen
        composable(Routes.SAVED_REPORTS) {
            SavedReportsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
