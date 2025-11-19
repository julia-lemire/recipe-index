package com.recipeindex.app.data

/**
 * AppSettings - User preferences for the application
 *
 * Persisted using DataStore for type-safe settings management
 */
data class AppSettings(
    /** Preferred unit system for displaying recipes (deprecated - use granular preferences) */
    @Deprecated("Use liquidVolumePreference and weightPreference for granular control")
    val unitSystem: UnitSystem = UnitSystem.IMPERIAL,

    /** Temperature display preference */
    val temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,

    /** Show recipe photos in list view */
    val showPhotosInList: Boolean = true,

    /** Default servings for new recipes */
    val defaultServings: Int = 4,

    /** Liquid volume unit preference (cups, tbsp, tsp, fl oz vs ml, L) */
    val liquidVolumePreference: UnitSystem = UnitSystem.IMPERIAL,

    /** Weight unit preference (oz, lbs vs g, kg) */
    val weightPreference: UnitSystem = UnitSystem.IMPERIAL
)

/**
 * Unit system preference
 */
enum class UnitSystem {
    /** Imperial (cups, tablespoons, ounces, pounds, °F) */
    IMPERIAL,

    /** Metric (ml, liters, grams, kg, °C) */
    METRIC,

    /** Show both units side-by-side */
    BOTH
}

/**
 * Temperature unit preference
 */
enum class TemperatureUnit {
    FAHRENHEIT,
    CELSIUS
}
