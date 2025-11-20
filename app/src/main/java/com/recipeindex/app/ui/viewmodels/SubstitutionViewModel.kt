package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recipeindex.app.data.Substitute
import com.recipeindex.app.data.entities.IngredientSubstitution
import com.recipeindex.app.data.managers.SubstitutionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * SubstitutionViewModel - UI state for substitution guide
 *
 * Handles search, filtering, and CRUD operations for ingredient substitutions
 */
class SubstitutionViewModel(
    private val substitutionManager: SubstitutionManager
) : ViewModel() {

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Category filter
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Dietary filter
    private val _selectedDietaryTag = MutableStateFlow<String?>(null)
    val selectedDietaryTag: StateFlow<String?> = _selectedDietaryTag.asStateFlow()

    // All substitutions (filtered)
    val substitutions: StateFlow<List<IngredientSubstitution>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        when {
            category != null -> substitutionManager.getSubstitutionsByCategory(category)
            query.isNotBlank() -> substitutionManager.searchSubstitutions(query)
            else -> substitutionManager.getAllSubstitutions()
        }
    }.map { list ->
        // Apply dietary filter if selected
        val dietaryTag = _selectedDietaryTag.value
        if (dietaryTag != null) {
            list.filter { substitution ->
                substitution.substitutes.any { sub ->
                    dietaryTag in sub.dietaryTags
                }
            }
        } else {
            list
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All categories
    val categories: StateFlow<List<String>> = substitutionManager.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Update search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Set category filter
     */
    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    /**
     * Set dietary tag filter
     */
    fun setDietaryTag(tag: String?) {
        _selectedDietaryTag.value = tag
    }

    /**
     * Get substitution for a specific ingredient
     */
    suspend fun getSubstitutionByIngredient(ingredient: String): IngredientSubstitution? {
        return substitutionManager.getSubstitutionByIngredient(ingredient)
    }

    /**
     * Get substitution by ID
     */
    suspend fun getSubstitutionById(id: Long): IngredientSubstitution? {
        return substitutionManager.getSubstitutionById(id)
    }

    /**
     * Create new substitution
     */
    fun createSubstitution(
        ingredient: String,
        category: String,
        substitutes: List<Substitute>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                substitutionManager.createSubstitution(ingredient, category, substitutes)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create substitution")
            }
        }
    }

    /**
     * Update existing substitution
     */
    fun updateSubstitution(
        id: Long,
        ingredient: String,
        category: String,
        substitutes: List<Substitute>,
        isUserAdded: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                substitutionManager.updateSubstitution(id, ingredient, category, substitutes, isUserAdded)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update substitution")
            }
        }
    }

    /**
     * Delete substitution
     */
    fun deleteSubstitution(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            substitutionManager.deleteSubstitution(id)
            onSuccess()
        }
    }

    /**
     * Calculate converted amount with substitution ratio
     */
    fun calculateConvertedAmount(originalAmount: Double, ratio: Double): Double {
        return substitutionManager.calculateConvertedAmount(originalAmount, ratio)
    }

    /**
     * Format amount as human-readable string
     */
    fun formatAmount(amount: Double): String {
        return substitutionManager.formatConvertedAmount(amount)
    }

    /**
     * Initialize database with default substitutions if needed
     */
    fun initializeDefaultSubstitutions() {
        viewModelScope.launch {
            substitutionManager.populateDefaultSubstitutionsIfNeeded()
        }
    }
}
