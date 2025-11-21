package com.recipeindex.app.data.parsers

import android.content.Context
import android.net.Uri
import com.recipeindex.app.data.entities.Recipe
import com.recipeindex.app.data.entities.RecipeSource
import com.recipeindex.app.utils.DebugConfig
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * PdfRecipeParser - Extracts text from PDF and parses recipe using TextRecipeParser
 *
 * Uses PdfBox-Android to extract text from PDF files
 */
class PdfRecipeParser(
    private val context: Context
) : RecipeParser {

    init {
        // Initialize PdfBox resource loader (required before using PdfBox)
        PDFBoxResourceLoader.init(context)
    }

    /**
     * Parse recipe from PDF file
     * @param source URI string of the PDF file
     */
    override suspend fun parse(source: String): Result<Recipe> = withContext(Dispatchers.IO) {
        try {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Parsing PDF from: $source"
            )

            val uri = Uri.parse(source)
            val text = extractTextFromPdf(uri)

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Extracted ${text.length} characters from PDF"
            )

            // Use TextRecipeParser to parse the extracted text
            TextRecipeParser.parseText(text, RecipeSource.PDF, source)
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "Failed to parse PDF: ${e.message}"
            )
            Result.failure(Exception("Failed to parse recipe from PDF: ${e.message}", e))
        }
    }

    /**
     * Extract all text from PDF using PdfBox
     *
     * Uses sortByPosition=true to attempt better ordering of multi-column layouts,
     * though this doesn't always work perfectly for complex PDFs.
     */
    private fun extractTextFromPdf(uri: Uri): String {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                val stripper = PDFTextStripper().apply {
                    // Sort by position to improve multi-column layout extraction
                    sortByPosition = true
                }
                return stripper.getText(document)
            }
        } ?: throw Exception("Could not open PDF file")
    }
}
