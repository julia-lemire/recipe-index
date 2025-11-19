package com.recipeindex.app.utils

/**
 * IngredientUnitConverter - Convert ingredient units and display conversions
 *
 * Detects units in ingredient strings and adds metric/imperial conversions
 * Example: "1 cup flour" → "1 cup (237 ml) flour"
 */
object IngredientUnitConverter {

    /**
     * Add unit conversion to an ingredient string (shows both units)
     * Example: "2 cups flour" → "2 cups (473 ml) flour"
     */
    fun addConversion(ingredient: String, toMetric: Boolean): String {
        // Pattern to match quantity + unit at the beginning
        val pattern = Regex("^([\\d./\\s-]+)\\s+(cups?|tbsp|tablespoons?|tsp|teaspoons?|fl\\.?\\s?oz|ounces?|oz|lbs?|pounds?|g|grams?|kg|kilograms?|ml|milliliters?|liters?|L)\\s+(.+)", RegexOption.IGNORE_CASE)
        val match = pattern.find(ingredient.trim())

        return if (match != null) {
            val quantityStr = match.groupValues[1]
            val unit = match.groupValues[2]
            val remainder = match.groupValues[3]

            val quantity = parseQuantity(quantityStr) ?: return ingredient

            val conversion = when {
                toMetric -> convertToMetric(quantity, unit)
                else -> convertToImperial(quantity, unit)
            }

            if (conversion != null) {
                "$quantityStr $unit ($conversion) $remainder"
            } else {
                ingredient
            }
        } else {
            ingredient
        }
    }

    /**
     * Convert imperial units to metric
     */
    private fun convertToMetric(quantity: Double, unit: String): String? {
        return when (unit.lowercase().replace(".", "").trim()) {
            "cup", "cups" -> {
                val ml = UnitConverter.cupsToMl(quantity)
                if (ml >= 1000) {
                    "${formatQuantity(ml / 1000)} L"
                } else {
                    "${formatQuantity(ml)} ml"
                }
            }
            "tbsp", "tablespoon", "tablespoons" -> {
                "${formatQuantity(UnitConverter.tablespoonsToMl(quantity))} ml"
            }
            "tsp", "teaspoon", "teaspoons" -> {
                "${formatQuantity(UnitConverter.teaspoonsToMl(quantity))} ml"
            }
            "fl oz", "floz" -> {
                "${formatQuantity(UnitConverter.flOzToMl(quantity))} ml"
            }
            "oz", "ounce", "ounces" -> {
                "${formatQuantity(UnitConverter.ouncesToGrams(quantity))} g"
            }
            "lb", "lbs", "pound", "pounds" -> {
                val grams = UnitConverter.poundsToGrams(quantity)
                if (grams >= 1000) {
                    "${formatQuantity(grams / 1000)} kg"
                } else {
                    "${formatQuantity(grams)} g"
                }
            }
            else -> null
        }
    }

    /**
     * Convert metric units to imperial
     */
    private fun convertToImperial(quantity: Double, unit: String): String? {
        return when (unit.lowercase().replace(".", "").trim()) {
            "ml", "milliliter", "milliliters" -> {
                // Try to convert to cups first if large enough
                val cups = UnitConverter.mlToCups(quantity)
                when {
                    cups >= 1 -> "${formatQuantity(cups)} cups"
                    cups >= 0.5 -> "${formatQuantity(cups)} cup"
                    else -> {
                        val tbsp = UnitConverter.mlToTablespoons(quantity)
                        if (tbsp >= 1) {
                            "${formatQuantity(tbsp)} tbsp"
                        } else {
                            "${formatQuantity(UnitConverter.mlToTeaspoons(quantity))} tsp"
                        }
                    }
                }
            }
            "l", "liter", "liters" -> {
                val cups = UnitConverter.mlToCups(quantity * 1000)
                "${formatQuantity(cups)} cups"
            }
            "g", "gram", "grams" -> {
                "${formatQuantity(UnitConverter.gramsToOunces(quantity))} oz"
            }
            "kg", "kilogram", "kilograms" -> {
                val pounds = UnitConverter.kgToPounds(quantity)
                if (pounds >= 1) {
                    "${formatQuantity(pounds)} lbs"
                } else {
                    "${formatQuantity(UnitConverter.gramsToOunces(quantity * 1000))} oz"
                }
            }
            else -> null
        }
    }

    /**
     * Parse quantity string to Double
     */
    private fun parseQuantity(quantityStr: String): Double? {
        val trimmed = quantityStr.trim()

        // Handle range (2-3) - take the average
        if (trimmed.contains('-')) {
            val parts = trimmed.split('-')
            if (parts.size == 2) {
                val first = parseQuantity(parts[0].trim())
                val second = parseQuantity(parts[1].trim())
                return if (first != null && second != null) (first + second) / 2 else first
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
     * Format quantity to a readable string (round to 1 decimal place)
     */
    private fun formatQuantity(quantity: Double): String {
        return when {
            quantity == quantity.toInt().toDouble() -> quantity.toInt().toString()
            else -> String.format("%.1f", quantity).trimEnd('0').trimEnd('.')
        }
    }
}
