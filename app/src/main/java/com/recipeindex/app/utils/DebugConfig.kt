package com.recipeindex.app.utils

import android.util.Log

/**
 * Centralized debug logging configuration
 *
 * CRITICAL: ALWAYS use DebugConfig.debugLog() instead of android.util.Log
 * Provides category-based filtering and can be disabled in production builds
 */
object DebugConfig {
    private const val ENABLE_DEBUG_LOGS = true
    private const val TAG = "RecipeIndex"

    enum class Category {
        NAVIGATION,
        DATABASE,
        IMPORT,
        UI,
        MANAGER,
        SETTINGS,
        GENERAL
    }

    /**
     * Log a debug message with category
     *
     * @param category The log category for filtering
     * @param message The message to log
     * @param throwable Optional throwable to log
     */
    fun debugLog(
        category: Category,
        message: String,
        throwable: Throwable? = null
    ) {
        if (!ENABLE_DEBUG_LOGS) return

        val logMessage = "[${category.name}] $message"

        if (throwable != null) {
            Log.d(TAG, logMessage, throwable)
        } else {
            Log.d(TAG, logMessage)
        }
    }

    /**
     * Log an error message
     */
    fun error(
        category: Category,
        message: String,
        throwable: Throwable? = null
    ) {
        val logMessage = "[${category.name}] $message"

        if (throwable != null) {
            Log.e(TAG, logMessage, throwable)
        } else {
            Log.e(TAG, logMessage)
        }
    }
}
