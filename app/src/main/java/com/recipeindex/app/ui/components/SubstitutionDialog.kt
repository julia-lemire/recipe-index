package com.recipeindex.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.recipeindex.app.data.entities.IngredientSubstitution
import com.recipeindex.app.ui.viewmodels.SubstitutionViewModel
import kotlinx.coroutines.launch

/**
 * SubstitutionDialog - Show substitutes for an ingredient from a recipe
 *
 * Triggered by long-pressing an ingredient in RecipeDetailScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstitutionDialog(
    ingredient: String,
    quantity: Double?,
    unit: String,
    viewModel: SubstitutionViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var substitution by remember { mutableStateOf<IngredientSubstitution?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load substitution data
    LaunchedEffect(ingredient) {
        scope.launch {
            substitution = viewModel.getSubstitutionByIngredient(ingredient)
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Substitutes for ${ingredient.capitalize()}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (quantity != null && quantity > 0) {
                            Text(
                                text = "${viewModel.formatAmount(quantity)} $unit",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Divider()

                // Content
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    substitution == null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No substitutes found for \"$ingredient\".\n\nTry searching in the Substitution Guide for similar ingredients.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(substitution!!.substitutes.sortedByDescending { it.suitability }) { substitute ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Substitute name and rating
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = substitute.name.capitalize(),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${substitute.suitability}/10",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Converted amount
                                        if (quantity != null && quantity > 0) {
                                            val convertedAmount = viewModel.calculateConvertedAmount(quantity, substitute.conversionRatio)
                                            val formattedAmount = viewModel.formatAmount(convertedAmount)
                                            Text(
                                                text = "Use: $formattedAmount $unit",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        // Conversion note
                                        substitute.conversionNote?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Notes
                                        substitute.notes?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }

                                        // Dietary tags
                                        if (substitute.dietaryTags.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.horizontalScroll(rememberScrollState())
                                            ) {
                                                substitute.dietaryTags.forEach { tag ->
                                                    SuggestionChip(
                                                        onClick = { },
                                                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
