package com.recipeindex.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.recipeindex.app.navigation.Screen

private data class PrimaryTab(val screen: Screen, val icon: ImageVector, val label: String)

private val tabs = listOf(
    PrimaryTab(Screen.RecipeIndex, Icons.Default.MenuBook, "Recipes"),
    PrimaryTab(Screen.MealPlanning, Icons.Default.CalendarMonth, "Meal Plans"),
    PrimaryTab(Screen.GroceryLists, Icons.Default.ShoppingCart, "Grocery"),
)

/**
 * Top tab bar shown on the three primary screens (Recipes, Meal Plans, Grocery Lists).
 * Sits below the screen's TopAppBar inside a Column topBar.
 */
@Composable
fun PrimaryTabBar(
    currentRoute: String?,
    onTabSelect: (Screen) -> Unit
) {
    val selectedIndex = tabs.indexOfFirst { it.screen.route == currentRoute }.coerceAtLeast(0)

    TabRow(selectedTabIndex = selectedIndex) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelect(tab.screen) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                text = { Text(tab.label, style = MaterialTheme.typography.labelLarge) }
            )
        }
    }
}
