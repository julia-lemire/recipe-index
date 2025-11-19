package com.recipeindex.app.data

/**
 * AppSettings - User preferences for the application
 *
 * Persisted using DataStore for type-safe settings management
 */
data class AppSettings(
    /** Preferred unit system for displaying recipes */
    val unitSystem: UnitSystem = UnitSystem.IMPERIAL,

    /** Temperature display preference */
    val temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,

    /** Show recipe photos in list view */
    val showPhotosInList: Boolean = true,

    /** Default servings for new recipes */
    val defaultServings: Int = 4
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
