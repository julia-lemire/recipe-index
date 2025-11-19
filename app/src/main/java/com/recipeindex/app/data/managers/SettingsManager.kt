package com.recipeindex.app.data.managers

import android.content.Context
import android.content.SharedPreferences
import com.recipeindex.app.data.AppSettings
import com.recipeindex.app.data.TemperatureUnit
import com.recipeindex.app.data.UnitSystem
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SettingsManager - Manages user preferences
 *
 * Uses SharedPreferences for simple key-value persistence
 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_settings",
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /**
     * Load settings from SharedPreferences
     */
    private fun loadSettings(): AppSettings {
        return AppSettings(
            unitSystem = UnitSystem.valueOf(
                prefs.getString(KEY_UNIT_SYSTEM, UnitSystem.IMPERIAL.name) ?: UnitSystem.IMPERIAL.name
            ),
            temperatureUnit = TemperatureUnit.valueOf(
                prefs.getString(KEY_TEMPERATURE_UNIT, TemperatureUnit.FAHRENHEIT.name) ?: TemperatureUnit.FAHRENHEIT.name
            ),
            showPhotosInList = prefs.getBoolean(KEY_SHOW_PHOTOS_IN_LIST, true),
            defaultServings = prefs.getInt(KEY_DEFAULT_SERVINGS, 4)
        )
    }

    /**
     * Update unit system preference
     */
    fun setUnitSystem(unitSystem: UnitSystem) {
        prefs.edit().putString(KEY_UNIT_SYSTEM, unitSystem.name).apply()
        _settings.value = _settings.value.copy(unitSystem = unitSystem)
        DebugConfig.debugLog(DebugConfig.Category.SETTINGS, "Unit system changed to: $unitSystem")
    }

    /**
     * Update temperature unit preference
     */
    fun setTemperatureUnit(temperatureUnit: TemperatureUnit) {
        prefs.edit().putString(KEY_TEMPERATURE_UNIT, temperatureUnit.name).apply()
        _settings.value = _settings.value.copy(temperatureUnit = temperatureUnit)
        DebugConfig.debugLog(DebugConfig.Category.SETTINGS, "Temperature unit changed to: $temperatureUnit")
    }

    /**
     * Update show photos in list preference
     */
    fun setShowPhotosInList(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_PHOTOS_IN_LIST, show).apply()
        _settings.value = _settings.value.copy(showPhotosInList = show)
        DebugConfig.debugLog(DebugConfig.Category.SETTINGS, "Show photos in list changed to: $show")
    }

    /**
     * Update default servings preference
     */
    fun setDefaultServings(servings: Int) {
        prefs.edit().putInt(KEY_DEFAULT_SERVINGS, servings).apply()
        _settings.value = _settings.value.copy(defaultServings = servings)
        DebugConfig.debugLog(DebugConfig.Category.SETTINGS, "Default servings changed to: $servings")
    }

    /**
     * Reset all settings to defaults
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = AppSettings()
        DebugConfig.debugLog(DebugConfig.Category.SETTINGS, "Settings reset to defaults")
    }

    companion object {
        private const val KEY_UNIT_SYSTEM = "unit_system"
        private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
        private const val KEY_SHOW_PHOTOS_IN_LIST = "show_photos_in_list"
        private const val KEY_DEFAULT_SERVINGS = "default_servings"
    }
}
