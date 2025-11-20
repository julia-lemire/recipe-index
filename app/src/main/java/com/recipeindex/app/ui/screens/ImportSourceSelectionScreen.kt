package com.recipeindex.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.InsertLink
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Import Source Selection Screen - Choose how to import a recipe or create one manually
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSourceSelectionScreen(
    onNavigateBack: () -> Unit,
    onSourceSelected: (ImportSource) -> Unit,
    onCreateManually: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Import") },
                    icon = { Icon(Icons.Default.CloudDownload, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Create") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> ImportTabContent(onSourceSelected = onSourceSelected)
                1 -> CreateTabContent(onCreateManually = onCreateManually)
            }
        }
    }
}

/**
 * Import tab content - shows 3 import source options
 */
@Composable
private fun ImportTabContent(onSourceSelected: (ImportSource) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Choose Import Source",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Select where you'd like to import your recipe from",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // URL Import Option
        ImportSourceCard(
            icon = Icons.Default.InsertLink,
            title = "From URL",
            description = "Import from recipe websites like Skinnytaste, AllRecipes, etc.",
            onClick = { onSourceSelected(ImportSource.URL) }
        )

        // PDF Import Option
        ImportSourceCard(
            icon = Icons.Default.PictureAsPdf,
            title = "From PDF",
            description = "Import from PDF cookbooks or saved recipe files",
            onClick = { onSourceSelected(ImportSource.PDF) }
        )

        // Photo Import Option
        ImportSourceCard(
            icon = Icons.Default.CameraAlt,
            title = "From Photo",
            description = "Import from recipe photos using OCR (camera or gallery)",
            onClick = { onSourceSelected(ImportSource.PHOTO) }
        )
    }
}

/**
 * Create tab content - button to create recipe manually
 */
@Composable
private fun CreateTabContent(onCreateManually: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create Recipe Manually",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Start from scratch and enter your recipe details",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Create Recipe Card
        ImportSourceCard(
            icon = Icons.Default.Add,
            title = "Create Recipe",
            description = "Enter recipe title, ingredients, instructions, and other details",
            onClick = onCreateManually
        )
    }
}

/**
 * Import source option card
 */
@Composable
private fun ImportSourceCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )

                if (!enabled) {
                    Text(
                        text = "Coming soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Import source options
 */
enum class ImportSource {
    URL,
    PDF,
    PHOTO
}
