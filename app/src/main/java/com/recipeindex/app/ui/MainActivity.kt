package com.recipeindex.app.ui

import android.content.Intent
import android.net.Uri
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
import com.recipeindex.app.data.managers.GroceryListManager
import com.recipeindex.app.data.managers.ImportManager
import com.recipeindex.app.data.managers.MealPlanManager
import com.recipeindex.app.data.managers.PantryStapleManager
import com.recipeindex.app.data.managers.RecipeManager
import com.recipeindex.app.data.managers.SettingsManager
import com.recipeindex.app.data.managers.SubstitutionManager
import com.recipeindex.app.data.parsers.PdfRecipeParser
import com.recipeindex.app.data.parsers.PhotoRecipeParser
import com.recipeindex.app.data.parsers.UrlRecipeParser
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
 * - Handle incoming share intents
 *
 * Follows design principle: MainActivity orchestrates, business logic elsewhere
 */
class MainActivity : ComponentActivity() {

    companion object {
        // Shared import data accessible to Navigation
        var pendingImportJson: String? = null
        lateinit var importManager: ImportManager
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DebugConfig.debugLog(DebugConfig.Category.GENERAL, "MainActivity onCreate")

        enableEdgeToEdge()

        // Setup dependencies
        val database = AppDatabase.getDatabase(applicationContext)
        val recipeManager = RecipeManager(database.recipeDao(), database.recipeLogDao(), database.mealPlanDao())
        val mealPlanManager = MealPlanManager(database.mealPlanDao(), database.recipeDao())
        val pantryStapleManager = PantryStapleManager(database.pantryStapleConfigDao())
        val groceryListManager = GroceryListManager(
            database.groceryListDao(),
            database.groceryItemDao(),
            database.recipeDao(),
            database.mealPlanDao(),
            pantryStapleManager
        )
        val settingsManager = SettingsManager(applicationContext)
        val substitutionManager = SubstitutionManager(database.substitutionDao())

        // Initialize pantry staples defaults on first run
        kotlinx.coroutines.GlobalScope.launch {
            pantryStapleManager.initializeDefaults()
        }
        val localImportManager = ImportManager(
            applicationContext,
            recipeManager,
            mealPlanManager,
            groceryListManager,
            database.recipeDao(),
            database.groceryItemDao()
        )
        importManager = localImportManager

        // Handle incoming share intent
        handleIncomingIntent(intent)

        // Setup HTTP client for URL recipe import
        val httpClient = HttpClient(OkHttp) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }
        val urlRecipeParser = UrlRecipeParser(httpClient)

        // Setup PDF and Photo parsers
        val pdfRecipeParser = PdfRecipeParser(applicationContext)
        val photoRecipeParser = PhotoRecipeParser(applicationContext)

        val viewModelFactory = ViewModelFactory(
            recipeManager,
            mealPlanManager,
            groceryListManager,
            settingsManager,
            substitutionManager,
            pantryStapleManager,
            urlRecipeParser,
            pdfRecipeParser,
            photoRecipeParser,
            applicationContext,
            httpClient
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    /**
     * Handle incoming share intent from other apps
     * Extracts JSON from intent and stores for import dialog
     */
    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        DebugConfig.debugLog(DebugConfig.Category.GENERAL, "Handling intent: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_SEND -> {
                // Handle shared text (JSON from share sheet)
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val customJson = intent.getStringExtra("recipeindex.json")

                val json = customJson ?: sharedText
                if (json != null && (json.contains("\"recipeIndexShare\"") || json.contains("SharePackage"))) {
                    DebugConfig.debugLog(DebugConfig.Category.GENERAL, "Received share data")
                    pendingImportJson = json
                    // Navigation will handle showing import dialog
                }
            }
            Intent.ACTION_VIEW -> {
                // Handle file opened with Recipe Index
                intent.data?.let { uri ->
                    handleFileUri(uri)
                }
            }
        }
    }

    /**
     * Handle file URI from external app (e.g., file manager)
     */
    private fun handleFileUri(uri: Uri) {
        try {
            val json = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null && (json.contains("\"recipeIndexShare\"") || json.contains("SharePackage"))) {
                DebugConfig.debugLog(DebugConfig.Category.GENERAL, "Loaded file from URI")
                pendingImportJson = json
            }
        } catch (e: Exception) {
            DebugConfig.error(DebugConfig.Category.GENERAL, "Failed to read file URI", e)
        }
    }
}
