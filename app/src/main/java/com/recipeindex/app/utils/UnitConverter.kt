package com.recipeindex.app.utils

import kotlin.math.roundToInt

/**
 * UnitConverter - Convert between common cooking measurement units
 *
 * Supports conversions between Imperial and Metric systems for:
 * - Volume (cups, tablespoons, teaspoons, ml, liters)
 * - Weight (ounces, pounds, grams, kg)
 * - Temperature (°F, °C)
 */
object UnitConverter {

    // ========== Volume Conversions ==========

    /**
     * Convert cups to milliliters
     */
    fun cupsToMl(cups: Double): Double = cups * 236.588

    /**
     * Convert milliliters to cups
     */
    fun mlToCups(ml: Double): Double = ml / 236.588

    /**
     * Convert tablespoons to milliliters
     */
    fun tablespoonsToMl(tablespoons: Double): Double = tablespoons * 14.7868

    /**
     * Convert milliliters to tablespoons
     */
    fun mlToTablespoons(ml: Double): Double = ml / 14.7868

    /**
     * Convert teaspoons to milliliters
     */
    fun teaspoonsToMl(teaspoons: Double): Double = teaspoons * 4.92892

    /**
     * Convert milliliters to teaspoons
     */
    fun mlToTeaspoons(ml: Double): Double = ml / 4.92892

    /**
     * Convert fluid ounces to milliliters
     */
    fun flOzToMl(flOz: Double): Double = flOz * 29.5735

    /**
     * Convert milliliters to fluid ounces
     */
    fun mlToFlOz(ml: Double): Double = ml / 29.5735

    // ========== Weight Conversions ==========

    /**
     * Convert ounces to grams
     */
    fun ouncesToGrams(ounces: Double): Double = ounces * 28.3495

    /**
     * Convert grams to ounces
     */
    fun gramsToOunces(grams: Double): Double = grams / 28.3495

    /**
     * Convert pounds to grams
     */
    fun poundsToGrams(pounds: Double): Double = pounds * 453.592

    /**
     * Convert grams to pounds
     */
    fun gramsToPounds(grams: Double): Double = grams / 453.592

    /**
     * Convert pounds to kilograms
     */
    fun poundsToKg(pounds: Double): Double = pounds * 0.453592

    /**
     * Convert kilograms to pounds
     */
    fun kgToPounds(kg: Double): Double = kg / 0.453592

    // ========== Temperature Conversions ==========

    /**
     * Convert Fahrenheit to Celsius
     */
    fun fahrenheitToCelsius(fahrenheit: Double): Double = (fahrenheit - 32) * 5.0 / 9.0

    /**
     * Convert Celsius to Fahrenheit
     */
    fun celsiusToFahrenheit(celsius: Double): Double = (celsius * 9.0 / 5.0) + 32

    // ========== Formatting Helpers ==========

    /**
     * Format a number to a readable string (remove unnecessary decimals)
     *
     * Examples:
     * - 1.0 → "1"
     * - 1.5 → "1.5"
     * - 1.333333 → "1.33"
     */
    fun formatNumber(value: Double): String {
        return when {
            value == value.roundToInt().toDouble() -> value.roundToInt().toString()
            else -> String.format("%.2f", value).trimEnd('0').trimEnd('.')
        }
    }

    /**
     * Format temperature with unit symbol
     */
    fun formatTemperature(value: Double, useCelsius: Boolean): String {
        val formatted = formatNumber(value)
        return if (useCelsius) "$formatted°C" else "$formatted°F"
    }

    /**
     * Format volume with appropriate unit
     */
    fun formatVolume(value: Double, unit: String): String {
        return "${formatNumber(value)} $unit"
    }

    /**
     * Format weight with appropriate unit
     */
    fun formatWeight(value: Double, unit: String): String {
        return "${formatNumber(value)} $unit"
    }

    // ========== Smart Conversion Suggestions ==========

    /**
     * Convert volume to metric, choosing appropriate unit (ml or L)
     */
    fun volumeToMetric(value: Double, fromUnit: String): Pair<Double, String> {
        val ml = when (fromUnit.lowercase()) {
            "cup", "cups" -> cupsToMl(value)
            "tablespoon", "tablespoons", "tbsp" -> tablespoonsToMl(value)
            "teaspoon", "teaspoons", "tsp" -> teaspoonsToMl(value)
            "fl oz", "fluid ounce", "fluid ounces" -> flOzToMl(value)
            else -> value // Already in ml
        }

        return if (ml >= 1000) {
            Pair(ml / 1000, "L")
        } else {
            Pair(ml, "ml")
        }
    }

    /**
     * Convert weight to metric, choosing appropriate unit (g or kg)
     */
    fun weightToMetric(value: Double, fromUnit: String): Pair<Double, String> {
        val grams = when (fromUnit.lowercase()) {
            "oz", "ounce", "ounces" -> ouncesToGrams(value)
            "lb", "lbs", "pound", "pounds" -> poundsToGrams(value)
            else -> value // Already in grams
        }

        return if (grams >= 1000) {
            Pair(grams / 1000, "kg")
        } else {
            Pair(grams, "g")
        }
    }

    /**
     * Convert volume to imperial, choosing appropriate unit
     */
    fun volumeToImperial(value: Double, fromUnit: String): Pair<Double, String> {
        val ml = when (fromUnit.lowercase()) {
            "l", "liter", "liters" -> value * 1000
            "ml", "milliliter", "milliliters" -> value
            else -> value
        }

        return when {
            ml >= 236.588 -> Pair(mlToCups(ml), "cups")
            ml >= 14.7868 -> Pair(mlToTablespoons(ml), "tbsp")
            else -> Pair(mlToTeaspoons(ml), "tsp")
        }
    }

    /**
     * Convert weight to imperial, choosing appropriate unit
     */
    fun weightToImperial(value: Double, fromUnit: String): Pair<Double, String> {
        val grams = when (fromUnit.lowercase()) {
            "kg", "kilogram", "kilograms" -> value * 1000
            "g", "gram", "grams" -> value
            else -> value
        }

        return when {
            grams >= 453.592 -> Pair(gramsToPounds(grams), "lbs")
            else -> Pair(gramsToOunces(grams), "oz")
        }
    }
}
