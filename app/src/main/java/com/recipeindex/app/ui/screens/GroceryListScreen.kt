package com.recipeindex.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.ui.viewmodels.GroceryListViewModel
import com.recipeindex.app.utils.DebugConfig
import com.recipeindex.app.utils.ShareHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Grocery List Screen - Card-based list of grocery lists with search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    groceryListViewModel: GroceryListViewModel,
    onViewList: (Long) -> Unit,
    onMenuClick: () -> Unit = {}
) {
    DebugConfig.debugLog(DebugConfig.Category.UI, "GroceryListScreen composed")

    val groceryLists by groceryListViewModel.groceryLists.collectAsState()
    val isLoading by groceryListViewModel.isLoading.collectAsState()
    val searchQuery by groceryListViewModel.searchQuery.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var listToDelete by remember { mutableStateOf<GroceryList?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grocery Lists") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Add Grocery List FAB clicked")
                    newListName = ""
                    showCreateDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Grocery List"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { groceryListViewModel.searchLists(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search grocery lists...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { groceryListViewModel.searchLists("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                groceryLists.isEmpty() -> {
                    EmptyGroceryListState(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(groceryLists, key = { it.id }) { groceryList ->
                            val items by groceryListViewModel.getItems(groceryList.id).collectAsState(initial = emptyList())

                            GroceryListCard(
                                groceryList = groceryList,
                                groceryListViewModel = groceryListViewModel,
                                onView = { onViewList(groceryList.id) },
                                onShare = {
                                    ShareHelper.shareGroceryList(context, groceryList, items)
                                },
                                onDelete = {
                                    listToDelete = groceryList
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create list dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Grocery List") },
            text = {
                Column {
                    Text("Enter name for the new list:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Weekly Shopping") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        groceryListViewModel.createList(newListName)
                        showCreateDialog = false
                    },
                    enabled = newListName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && listToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Grocery List") },
            text = { Text("Are you sure you want to delete \"${listToDelete!!.name}\"? This will also delete all items in the list.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        groceryListViewModel.deleteList(listToDelete!!)
                        showDeleteDialog = false
                        listToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GroceryListCard(
    groceryList: GroceryList,
    groceryListViewModel: GroceryListViewModel,
    onView: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val itemCount by groceryListViewModel.getItemCount(groceryList.id).collectAsState(initial = 0)
    val checkedCount by groceryListViewModel.getCheckedCount(groceryList.id).collectAsState(initial = 0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onView
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = groceryList.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatListInfo(itemCount, checkedCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Progress indicator if there are items
                if (itemCount > 0) {
                    val progress = checkedCount.toFloat() / itemCount.toFloat()
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                    )
                }
            }

            // Created date
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Created ${formatDate(groceryList.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onView,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun EmptyGroceryListState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No grocery lists yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to create your first list",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun formatListInfo(itemCount: Int, checkedCount: Int): String {
    return if (itemCount == 0) {
        "Empty list"
    } else {
        "$checkedCount of $itemCount items checked"
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
