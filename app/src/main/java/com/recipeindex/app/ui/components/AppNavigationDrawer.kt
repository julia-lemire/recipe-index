package com.recipeindex.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.recipeindex.app.navigation.Screen
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.launch

/**
 * Responsive Navigation Drawer - UI component only, no business logic
 *
 * Modal drawer for phones (Samsung S23 Ultra)
 * Permanent drawer for tablets (Samsung Galaxy Tab S10+)
 *
 * Follows design principle: MainActivity orchestrates, navigation logic separated
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationDrawer(
    navController: NavHostController,
    currentRoute: String?,
    windowSizeClass: WindowSizeClass,
    content: @Composable (PaddingValues) -> Unit
) {
    DebugConfig.debugLog(
        DebugConfig.Category.NAVIGATION,
        "AppNavigationDrawer - windowSize: ${windowSizeClass.widthSizeClass}"
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Determine if we should use permanent drawer (tablet) or modal drawer (phone)
    val usePermanentDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    if (usePermanentDrawer) {
        // Permanent drawer for tablets
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(
                    modifier = Modifier.width(280.dp)
                ) {
                    DrawerContent(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            DebugConfig.debugLog(
                                DebugConfig.Category.NAVIGATION,
                                "Navigate to: ${screen.route}"
                            )
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) {
            MainContent(
                navController = navController,
                showMenuButton = false,
                onMenuClick = {},
                content = content
            )
        }
    } else {
        // Modal drawer for phones
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp)
                ) {
                    DrawerContent(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            DebugConfig.debugLog(
                                DebugConfig.Category.NAVIGATION,
                                "Navigate to: ${screen.route}"
                            )
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            MainContent(
                navController = navController,
                showMenuButton = true,
                onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                content = content
            )
        }
    }
}

@Composable
private fun DrawerContent(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp)
    ) {
        // Drawer Header with app logo and name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Placeholder logo - using default icon for now
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Recipe Index",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        // Navigation items
        Screen.drawerScreens.forEach { screen ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(text = screen.title)
                },
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    navController: NavHostController,
    showMenuButton: Boolean,
    onMenuClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (showMenuButton) {
                TopAppBar(
                    title = {
                        // Title updates based on current route
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        val currentScreen = Screen.drawerScreens.find { it.route == currentRoute }
                        Text(text = currentScreen?.title ?: "Recipe Index")
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}
