package com.recipeindex.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.recipeindex.app.data.AppDatabase
import com.recipeindex.app.data.managers.MealPlanManager
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.parsers.PdfRecipeParser
import com.recipeindex.app.data.parsers.PhotoRecipeParser
import com.recipeindex.app.data.parsers.SchemaOrgRecipeParser
import com.recipeindex.app.ui.components.AppNavigationDrawer
import com.recipeindex.app.ui.theme.HearthTheme
import com.recipeindex.app.ui.viewmodels.ViewModelFactory
import com.recipeindex.app.utils.DebugConfig
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.logging.*

/**
 * MainActivity - Orchestrator only, no business logic
 *
 * Responsibilities:
 * - Setup dependencies (Database, Managers, ViewModelFactory)
 * - Configure window and theme
 * - Wire navigation and UI components
 *
 * Follows design principle: MainActivity orchestrates, business logic elsewhere
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DebugConfig.debugLog(DebugConfig.Category.GENERAL, "MainActivity onCreate")

        enableEdgeToEdge()

        // Setup dependencies
        val database = AppDatabase.getDatabase(applicationContext)
        val recipeManager = RecipeManager(database.recipeDao())
        val mealPlanManager = MealPlanManager(database.mealPlanDao(), database.recipeDao())

        // Setup HTTP client for URL recipe import
        val httpClient = HttpClient(OkHttp) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }
        val urlRecipeParser = SchemaOrgRecipeParser(httpClient)

        // Setup PDF and Photo parsers
        val pdfRecipeParser = PdfRecipeParser(applicationContext)
        val photoRecipeParser = PhotoRecipeParser(applicationContext)

        val viewModelFactory = ViewModelFactory(
            recipeManager,
            mealPlanManager,
            urlRecipeParser,
            pdfRecipeParser,
            photoRecipeParser
        )

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            DebugConfig.debugLog(
                DebugConfig.Category.GENERAL,
                "Window size class: ${windowSizeClass.widthSizeClass}"
            )

            HearthTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                AppNavigationDrawer(
                    navController = navController,
                    currentRoute = currentRoute,
                    windowSizeClass = windowSizeClass
                ) { paddingValues, onMenuClick ->
                    RecipeIndexNavigation(
                        navController = navController,
                        viewModelFactory = viewModelFactory,
                        onMenuClick = onMenuClick
                    )
                }
            }
        }
    }
}
