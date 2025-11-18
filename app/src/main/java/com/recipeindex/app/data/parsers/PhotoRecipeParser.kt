package com.recipeindex.app.data.parsers

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.utils.DebugConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * PhotoRecipeParser - Uses ML Kit OCR to extract text from photos and parses recipe
 *
 * Extracts text from recipe photos/screenshots using Google ML Kit Text Recognition
 */
class PhotoRecipeParser(
    private val context: Context
) : RecipeParser {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Parse recipe from photo using OCR
     * @param source URI string of the photo/image file
     */
    override suspend fun parse(source: String): Result<Recipe> = withContext(Dispatchers.IO) {
        try {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Parsing photo from: $source"
            )

            val uri = Uri.parse(source)
            val text = extractTextFromPhoto(uri)

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Extracted ${text.length} characters from photo via OCR"
            )

            if (text.isBlank()) {
                return@withContext Result.failure(Exception("No text found in image. Please ensure the image contains readable recipe text."))
            }

            // Use TextRecipeParser to parse the extracted text
            TextRecipeParser.parseText(text, RecipeSource.PHOTO, source)
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Failed to parse photo: ${e.message}"
            )
            Result.failure(Exception("Failed to parse recipe from photo: ${e.message}", e))
        }
    }

    /**
     * Extract text from photo using ML Kit Text Recognition
     */
    private suspend fun extractTextFromPhoto(uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)

        // Process image with ML Kit (converts Task to coroutine)
        val visionText = recognizer.process(image).await()

        // Extract all text blocks
        return visionText.textBlocks.joinToString("\n") { block ->
            block.text
        }
    }

    /**
     * Clean up resources
     */
    fun close() {
        recognizer.close()
    }
}
