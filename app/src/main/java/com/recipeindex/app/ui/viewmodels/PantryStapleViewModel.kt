package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.entities.PantryStapleConfig
import com.recipeindex.app.data.managers.PantryStapleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * PantryStapleViewModel - Manages pantry staple configuration state
 */
class PantryStapleViewModel(
    private val pantryStapleManager: PantryStapleManager
) : ViewModel() {

    val allConfigs: StateFlow<List<PantryStapleConfig>> =
        pantryStapleManager.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Get unique categories from configs
     */
    fun getCategories(): List<String> {
        return listOf("All") + allConfigs.value.map { it.category }.distinct().sorted()
    }

    /**
     * Get filtered configs by selected category
     */
    fun getFilteredConfigs(): List<PantryStapleConfig> {
        return if (_selectedCategory.value == "All") {
            allConfigs.value
        } else {
            allConfigs.value.filter { it.category == _selectedCategory.value }
        }
    }

    /**
     * Set selected category filter
     */
    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    /**
     * Save or update a configuration
     */
    fun saveConfig(config: PantryStapleConfig) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            pantryStapleManager.saveConfig(config)
                .onFailure { e ->
                    _errorMessage.value = "Failed to save: ${e.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * Delete a configuration
     */
    fun deleteConfig(config: PantryStapleConfig) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            pantryStapleManager.deleteConfig(config)
                .onFailure { e ->
                    _errorMessage.value = "Failed to delete: ${e.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * Toggle enabled status for a configuration
     */
    fun toggleEnabled(config: PantryStapleConfig) {
        saveConfig(config.copy(enabled = !config.enabled))
    }

    /**
     * Reset all configurations to defaults
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            pantryStapleManager.resetToDefaults()
                .onFailure { e ->
                    _errorMessage.value = "Failed to reset: ${e.message}"
                }

            _isLoading.value = false
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
