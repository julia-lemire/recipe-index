package com.recipeindex.app.utils

import kotlin.math.abs

/**
 * IngredientScaler - Utility for parsing and scaling ingredient quantities
 *
 * Handles:
 * - Fractions (1/2, 1/4, 2/3)
 * - Mixed numbers (1 1/2, 2 1/4)
 * - Decimals (0.5, 1.25)
 * - Whole numbers (1, 2, 3)
 * - Multiple quantities (2-3, 1-2)
 * - Units preservation (cups, tbsp, oz, etc.)
 */
object IngredientScaler {

    /**
     * Scale an ingredient string by a given factor
     * Example: "1/2 cup flour" scaled by 2.0 → "1 cup flour"
     */
    fun scaleIngredient(ingredient: String, scaleFactor: Double): String {
        if (scaleFactor == 1.0) return ingredient

        // Try to parse quantity at the beginning
        val quantityPattern = Regex("^([\\d./\\s-]+)\\s+(.+)")
        val match = quantityPattern.find(ingredient.trim())

        return if (match != null) {
            val quantityStr = match.groupValues[1]
            val remainder = match.groupValues[2]

            val originalQuantity = parseQuantity(quantityStr)
            if (originalQuantity != null) {
                val scaledQuantity = originalQuantity * scaleFactor
                val formattedQuantity = formatQuantity(scaledQuantity)
                "$formattedQuantity $remainder"
            } else {
                ingredient // Couldn't parse, return original
            }
        } else {
            ingredient // No quantity found, return original
        }
    }

    /**
     * Parse a quantity string to a Double
     * Handles: "1/2", "1 1/2", "0.5", "2", "2-3" (takes first number)
     */
    private fun parseQuantity(quantityStr: String): Double? {
        val trimmed = quantityStr.trim()

        // Handle range (2-3) - take the first number
        if (trimmed.contains('-')) {
            val parts = trimmed.split('-')
            if (parts.size == 2) {
                return parseQuantity(parts[0].trim())
            }
        }

        // Handle mixed numbers (1 1/2)
        val mixedPattern = Regex("^(\\d+)\\s+(\\d+)/(\\d+)$")
        val mixedMatch = mixedPattern.find(trimmed)
        if (mixedMatch != null) {
            val whole = mixedMatch.groupValues[1].toDoubleOrNull() ?: return null
            val numerator = mixedMatch.groupValues[2].toDoubleOrNull() ?: return null
            val denominator = mixedMatch.groupValues[3].toDoubleOrNull() ?: return null
            if (denominator == 0.0) return null
            return whole + (numerator / denominator)
        }

        // Handle fractions (1/2)
        val fractionPattern = Regex("^(\\d+)/(\\d+)$")
        val fractionMatch = fractionPattern.find(trimmed)
        if (fractionMatch != null) {
            val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: return null
            val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: return null
            if (denominator == 0.0) return null
            return numerator / denominator
        }

        // Handle decimals and whole numbers
        return trimmed.toDoubleOrNull()
    }

    /**
     * Format a quantity as a readable string
     * Prefers fractions for common values (0.25 → "1/4", 0.5 → "1/2", 0.75 → "3/4")
     * Uses mixed numbers when appropriate (1.5 → "1 1/2")
     */
    private fun formatQuantity(quantity: Double): String {
        // Check if it's close to a whole number
        val rounded = quantity.toInt()
        if (abs(quantity - rounded) < 0.01) {
            return rounded.toString()
        }

        // Extract whole number part
        val whole = quantity.toInt()
        val fraction = quantity - whole

        // Check common fractions
        val commonFraction = when {
            abs(fraction - 0.25) < 0.01 -> "1/4"
            abs(fraction - 0.33) < 0.02 -> "1/3"
            abs(fraction - 0.5) < 0.01 -> "1/2"
            abs(fraction - 0.67) < 0.02 -> "2/3"
            abs(fraction - 0.75) < 0.01 -> "3/4"
            else -> null
        }

        return if (commonFraction != null) {
            if (whole > 0) {
                "$whole $commonFraction"
            } else {
                commonFraction
            }
        } else {
            // Fall back to decimal with max 2 decimal places
            String.format("%.2f", quantity).trimEnd('0').trimEnd('.')
        }
    }
}
