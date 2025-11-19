package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.GroceryItem
import com.recipeindex.app.data.entities.GroceryList
import com.recipeindex.app.data.managers.GroceryListManager
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * GroceryListViewModel - UI state for grocery list screens
 */
class GroceryListViewModel(
    private val groceryListManager: GroceryListManager
) : ViewModel() {

    private val _lists = MutableStateFlow<List<GroceryList>>(emptyList())
    val lists: StateFlow<List<GroceryList>> = _lists.asStateFlow()

    private val _currentList = MutableStateFlow<GroceryList?>(null)
    val currentList: StateFlow<GroceryList?> = _currentList.asStateFlow()

    private val _currentListItems = MutableStateFlow<List<GroceryItem>>(emptyList())
    val currentListItems: StateFlow<List<GroceryItem>> = _currentListItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadLists()
    }

    /**
     * Load all grocery lists
     */
    fun loadLists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                groceryListManager.getAllLists().collect { lists ->
                    _lists.value = lists
                    _isLoading.value = false
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Loaded ${lists.size} grocery lists")
                }
            } catch (e: Exception) {
                _error.value = "Failed to load grocery lists: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "loadLists failed", e)
            }
        }
    }

    /**
     * Load grocery list and its items
     */
    fun loadList(listId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = groceryListManager.getListById(listId)
                _currentList.value = list

                if (list != null) {
                    groceryListManager.getItemsForList(listId).collect { items ->
                        _currentListItems.value = items
                        _isLoading.value = false
                        DebugConfig.debugLog(DebugConfig.Category.UI, "Loaded ${items.size} items for list")
                    }
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load grocery list: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "loadList failed", e)
            }
        }
    }

    /**
     * Create new grocery list
     */
    fun createList(name: String, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = groceryListManager.createList(name)
                _isLoading.value = false

                result.onSuccess { listId ->
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Created grocery list: $listId")
                    onSuccess(listId)
                }.onFailure { e ->
                    _error.value = "Failed to create list: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "createList failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to create list: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "createList exception", e)
            }
        }
    }

    /**
     * Update grocery list
     */
    fun updateList(list: GroceryList, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = groceryListManager.updateList(list)
                _isLoading.value = false

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Updated grocery list: ${list.id}")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to update list: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "updateList failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update list: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "updateList exception", e)
            }
        }
    }

    /**
     * Delete grocery list
     */
    fun deleteList(list: GroceryList, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = groceryListManager.deleteList(list)
                _isLoading.value = false

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Deleted grocery list: ${list.id}")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to delete list: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "deleteList failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete list: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "deleteList exception", e)
            }
        }
    }

    /**
     * Add recipes to grocery list
     */
    fun addRecipesToList(listId: Long, recipeIds: List<Long>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = groceryListManager.addRecipesToList(listId, recipeIds)
                _isLoading.value = false

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Added ${recipeIds.size} recipes to list")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to add recipes: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "addRecipesToList failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to add recipes: ${e.message}"
                _isLoading.value = false
                DebugConfig.error(DebugConfig.Category.UI, "addRecipesToList exception", e)
            }
        }
    }

    /**
     * Add manual item to list
     */
    fun addManualItem(listId: Long, itemName: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = groceryListManager.addManualItem(listId, itemName)

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Added manual item")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to add item: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "addManualItem failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to add item: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "addManualItem exception", e)
            }
        }
    }

    /**
     * Toggle item checked status
     */
    fun toggleItemChecked(item: GroceryItem) {
        viewModelScope.launch {
            try {
                val result = groceryListManager.toggleItemChecked(item)

                result.onFailure { e ->
                    _error.value = "Failed to update item: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "toggleItemChecked failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update item: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "toggleItemChecked exception", e)
            }
        }
    }

    /**
     * Update grocery item
     */
    fun updateItem(item: GroceryItem, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = groceryListManager.updateItem(item)

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Updated item: ${item.id}")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to update item: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "updateItem failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to update item: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "updateItem exception", e)
            }
        }
    }

    /**
     * Delete grocery item
     */
    fun deleteItem(item: GroceryItem, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = groceryListManager.deleteItem(item)

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Deleted item: ${item.id}")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to delete item: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "deleteItem failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete item: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "deleteItem exception", e)
            }
        }
    }

    /**
     * Clear all checked items from list
     */
    fun clearCheckedItems(listId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = groceryListManager.clearCheckedItems(listId)

                result.onSuccess {
                    DebugConfig.debugLog(DebugConfig.Category.UI, "Cleared checked items")
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Failed to clear items: ${e.message}"
                    DebugConfig.error(DebugConfig.Category.UI, "clearCheckedItems failed", e)
                }
            } catch (e: Exception) {
                _error.value = "Failed to clear items: ${e.message}"
                DebugConfig.error(DebugConfig.Category.UI, "clearCheckedItems exception", e)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}
