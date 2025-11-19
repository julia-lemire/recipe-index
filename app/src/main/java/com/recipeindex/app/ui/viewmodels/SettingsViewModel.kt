package com.recipeindex.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.recipeindex.app.data.AppSettings
import com.recipeindex.app.data.TemperatureUnit
import com.recipeindex.app.data.UnitSystem
import com.recipeindex.app.data.managers.SettingsManager
import kotlinx.coroutines.flow.StateFlow

/**
 * SettingsViewModel - UI state for settings screen
 *
 * Delegates to SettingsManager for persistence
 */
class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsManager.settings

    /**
     * Update unit system preference
     */
    fun setUnitSystem(unitSystem: UnitSystem) {
        settingsManager.setUnitSystem(unitSystem)
    }

    /**
     * Update temperature unit preference
     */
    fun setTemperatureUnit(temperatureUnit: TemperatureUnit) {
        settingsManager.setTemperatureUnit(temperatureUnit)
    }

    /**
     * Update show photos in list preference
     */
    fun setShowPhotosInList(show: Boolean) {
        settingsManager.setShowPhotosInList(show)
    }

    /**
     * Update default servings preference
     */
    fun setDefaultServings(servings: Int) {
        settingsManager.setDefaultServings(servings)
    }

    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        settingsManager.resetToDefaults()
    }
}
