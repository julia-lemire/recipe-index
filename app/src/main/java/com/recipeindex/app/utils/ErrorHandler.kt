package com.recipeindex.app.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Centralized error handling utility
 *
 * Provides consistent error messages and logging throughout the app
 */
object ErrorHandler {
    /**
     * Handle a Result and show error to user if failed
     */
    fun <T> handleResult(
        result: Result<T>,
        context: Context? = null,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = {},
        customErrorMessage: String? = null
    ) {
        result
            .onSuccess { value ->
                onSuccess(value)
            }
            .onFailure { throwable ->
                val errorMessage = customErrorMessage ?: getErrorMessage(throwable)
                DebugConfig.error(DebugConfig.Category.GENERAL, errorMessage, throwable)
                onError(errorMessage)
            }
    }

    /**
     * Get user-friendly error message from throwable
     */
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException -> "No internet connection. Please check your network."
            is java.net.SocketTimeoutException -> "Request timed out. Please try again."
            is java.io.IOException -> "Network error. Please check your connection."
            is IllegalArgumentException -> throwable.message ?: "Invalid input. Please check your data."
            is IllegalStateException -> throwable.message ?: "Operation cannot be performed in current state."
            is NullPointerException -> "Missing required data. Please try again."
            else -> throwable.message ?: "An unexpected error occurred. Please try again."
        }
    }

    /**
     * Execute a suspending operation with error handling
     */
    suspend fun <T> executeSafely(
        operation: suspend () -> Result<T>,
        onError: (String) -> Unit = {}
    ): Result<T> {
        return try {
            operation()
        } catch (e: Exception) {
            val errorMessage = getErrorMessage(e)
            DebugConfig.error(DebugConfig.Category.GENERAL, "Operation failed: $errorMessage", e)
            onError(errorMessage)
            Result.failure(e)
        }
    }

    /**
     * Execute operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        operation: suspend () -> Result<T>,
        onError: (String, Int) -> Unit = { _, _ -> }
    ): Result<T> {
        var lastError: Throwable? = null

        repeat(maxRetries) { attempt ->
            try {
                val result = operation()
                if (result.isSuccess) {
                    return result
                }
                lastError = result.exceptionOrNull()
            } catch (e: Exception) {
                lastError = e
                if (attempt < maxRetries - 1) {
                    onError(getErrorMessage(e), attempt + 1)
                }
            }
        }

        val finalError = lastError ?: Exception("Operation failed after $maxRetries attempts")
        DebugConfig.error(DebugConfig.Category.GENERAL, "All retries failed", finalError)
        return Result.failure(finalError)
    }
}
