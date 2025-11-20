package com.recipeindex.app.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

/**
 * TextFormatUtils - Utilities for formatting and styling text
 */
object TextFormatUtils {

    /**
     * Highlight numbers in text with bold font weight
     * Matches: standalone numbers, numbers with units (°F, °C, min, minutes, hrs, hours, etc.)
     *
     * Examples:
     * - "Bake at 350°F for 25 minutes" -> bolds "350°F" and "25 minutes"
     * - "Add 2 cups flour" -> bolds "2"
     * - "Simmer for 1-2 hours" -> bolds "1-2 hours"
     */
    fun highlightNumbersInText(text: String): AnnotatedString {
        return buildAnnotatedString {
            append(text)

            // Pattern matches:
            // - Numbers (with optional decimals, fractions, ranges)
            // - Optional whitespace
            // - Optional units (°F, °C, F, C, min, minutes, hrs, hours, sec, seconds, degrees)
            val pattern = Regex(
                """(\d+(?:[./]\d+)?(?:\s*-\s*\d+(?:[./]\d+)?)?)\s*(°[FC]|degrees?\s*[FC]?|[FC](?!\w)|min(?:ute)?s?|hrs?|hours?|sec(?:ond)?s?)?\b""",
                RegexOption.IGNORE_CASE
            )

            pattern.findAll(text).forEach { matchResult ->
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                    start = matchResult.range.first,
                    end = matchResult.range.last + 1
                )
            }
        }
    }

    /**
     * Parse instructions into individual steps
     * Handles numbered steps (1., 2., etc.) or splits by newlines
     */
    fun parseInstructionsIntoSteps(instructions: String): List<String> {
        // Check if instructions have numbered steps
        val numberedPattern = Regex("""^\d+\.""", RegexOption.MULTILINE)

        return if (numberedPattern.containsMatchIn(instructions)) {
            // Split by numbered steps
            instructions.split(Regex("""\n(?=\d+\.)"""))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        } else {
            // Split by newlines/sentences
            instructions.split("\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
    }
}
