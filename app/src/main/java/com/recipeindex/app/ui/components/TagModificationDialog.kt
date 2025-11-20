package com.recipeindex.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.recipeindex.app.utils.TagStandardizer

/**
 * TagModificationDialog - Shows which tags were modified during standardization
 *
 * Allows users to review and edit each tag modification before accepting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagModificationDialog(
    modifications: List<TagStandardizer.TagModification>,
    onAccept: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Track which tags the user has chosen to keep/modify
    var editedTags by remember {
        mutableStateOf(modifications.map { it.standardized }.toMutableList())
    }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "Review Tag Changes",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Some tags were modified during import. Review the changes below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                // List of modifications
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(modifications.indices.toList()) { index ->
                        val mod = modifications[index]
                        val currentTag = editedTags[index]
                        val isEditing = editingIndex == index
                        val isMarkedForDeletion = currentTag.isBlank()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isMarkedForDeletion -> MaterialTheme.colorScheme.errorContainer
                                    mod.wasModified -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                if (isEditing) {
                                    // Edit mode
                                    OutlinedTextField(
                                        value = editText,
                                        onValueChange = { editText = it },
                                        label = { Text("Edit tag") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = {
                                            editingIndex = null
                                            editText = ""
                                        }) {
                                            Text("Cancel")
                                        }
                                        TextButton(onClick = {
                                            if (editText.isNotBlank()) {
                                                editedTags[index] = editText.trim().lowercase()
                                            }
                                            editingIndex = null
                                            editText = ""
                                        }) {
                                            Text("Save")
                                        }
                                    }
                                } else {
                                    // Display mode
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            if (isMarkedForDeletion) {
                                                // Marked for deletion
                                                Text(
                                                    text = mod.original,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.error,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                                Text(
                                                    text = "Marked for removal",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            } else if (mod.wasModified) {
                                                // Show original → standardized
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = mod.original,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        textDecoration = TextDecoration.LineThrough
                                                    )
                                                    Icon(
                                                        Icons.Default.ArrowForward,
                                                        contentDescription = "changed to",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = currentTag,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                if (mod.original != mod.standardized) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = getRejectionReason(mod.original, mod.standardized),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            } else {
                                                // No change
                                                Text(
                                                    text = currentTag,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "No changes needed",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                        }

                                        if (isMarkedForDeletion) {
                                            // Show restore button
                                            TextButton(
                                                onClick = {
                                                    editedTags[index] = mod.standardized
                                                }
                                            ) {
                                                Text("Restore")
                                            }
                                        } else {
                                            // Show delete and edit buttons
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        editedTags[index] = ""
                                                    }
                                                ) {
                                                    Icon(
                                                        Icons.Default.Remove,
                                                        contentDescription = "Remove tag",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        editingIndex = index
                                                        editText = currentTag
                                                    }
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Edit tag",
                                                        tint = MaterialTheme.colorScheme.primary
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

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAccept(editedTags.filter { it.isNotBlank() }) }
                    ) {
                        Text("Accept Changes")
                    }
                }
            }
        }
    }
}

/**
 * Get a human-readable reason for why a tag was modified
 */
private fun getRejectionReason(original: String, standardized: String): String {
    val originalLower = original.lowercase()
    val standardizedLower = standardized.lowercase()

    return when {
        originalLower.contains("recipe") || originalLower.contains("meal") || originalLower.contains("dish") ->
            "Removed noise words (recipe, meal, dish)"
        originalLower.contains("food") || originalLower.contains("cuisine") ->
            "Removed noise words (food, cuisine)"
        originalLower.contains("dinner") || originalLower.contains("lunch") || originalLower.contains("breakfast") ->
            "Removed meal type suffix"
        standardizedLower != originalLower && !originalLower.contains(standardizedLower) ->
            "Standardized to common form"
        else ->
            "Cleaned and simplified"
    }
}
