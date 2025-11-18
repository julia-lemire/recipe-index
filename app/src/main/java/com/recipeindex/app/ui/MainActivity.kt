package com.recipeindex.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.recipeindex.app.ui.components.AppNavigationDrawer
import com.recipeindex.app.ui.theme.HearthTheme
import com.recipeindex.app.utils.DebugConfig

/**
 * MainActivity - Entry point for Recipe Index
 *
 * Implements responsive navigation:
 * - Modal drawer for phones (Samsung S23 Ultra)
 * - Permanent drawer for tablets (Samsung Galaxy Tab S10+)
 *
 * Uses Hearth design system theme
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DebugConfig.debugLog(DebugConfig.Category.GENERAL, "MainActivity onCreate")

        enableEdgeToEdge()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            DebugConfig.debugLog(
                DebugConfig.Category.GENERAL,
                "Window size class: ${windowSizeClass.widthSizeClass}"
            )

            HearthTheme {
                RecipeIndexApp(windowSizeClass = windowSizeClass)
            }
        }
    }
}

@Composable
fun RecipeIndexApp(windowSizeClass: androidx.compose.material3.windowsizeclass.WindowSizeClass) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    AppNavigationDrawer(
        navController = navController,
        currentRoute = currentRoute,
        windowSizeClass = windowSizeClass
    )
}
