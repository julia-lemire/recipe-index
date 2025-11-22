package com.recipeindex.app.utils

/**
 * ResultUtils - Utility functions for reducing try-catch boilerplate in Managers
 *
 * Provides standardized Result handling with consistent logging.
 */

/**
 * Execute a suspending block and wrap the result, with optional logging.
 *
 * Replaces this boilerplate pattern:
 * ```
 * return try {
 *     // operation
 *     DebugConfig.debugLog(Category.MANAGER, "success message")
 *     Result.success(value)
 * } catch (e: Exception) {
 *     DebugConfig.error(Category.MANAGER, "error message", e)
 *     Result.failure(e)
 * }
 * ```
 *
 * With:
 * ```
 * return resultOf(
 *     successLog = "success message",
 *     errorLog = "error message"
 * ) {
 *     // operation
 * }
 * ```
 *
 * @param category The debug category for logging (defaults to MANAGER)
 * @param successLog Optional message to log on success
 * @param errorLog Message to log on failure (defaults to "Operation failed")
 * @param block The suspending operation to execute
 * @return Result<T> wrapping the success value or failure exception
 */
suspend fun <T> resultOf(
    category: DebugConfig.Category = DebugConfig.Category.MANAGER,
    successLog: String? = null,
    errorLog: String = "Operation failed",
    block: suspend () -> T
): Result<T> {
    return try {
        val result = block()
        if (successLog != null) {
            DebugConfig.debugLog(category, successLog)
        }
        Result.success(result)
    } catch (e: Exception) {
        DebugConfig.error(category, errorLog, e)
        Result.failure(e)
    }
}

/**
 * Non-suspending version for synchronous operations.
 */
inline fun <T> resultOfSync(
    category: DebugConfig.Category = DebugConfig.Category.MANAGER,
    successLog: String? = null,
    errorLog: String = "Operation failed",
    block: () -> T
): Result<T> {
    return try {
        val result = block()
        if (successLog != null) {
            DebugConfig.debugLog(category, successLog)
        }
        Result.success(result)
    } catch (e: Exception) {
        DebugConfig.error(category, errorLog, e)
        Result.failure(e)
    }
}

/**
 * Execute with validation before the main operation.
 * Validation failures are treated as exceptions.
 *
 * @param validate Validation block that should throw on invalid state
 * @param operation The main operation to execute after validation
 */
suspend fun <T> resultOfValidated(
    category: DebugConfig.Category = DebugConfig.Category.MANAGER,
    successLog: String? = null,
    errorLog: String = "Operation failed",
    validate: () -> Unit,
    operation: suspend () -> T
): Result<T> {
    return try {
        validate()
        val result = operation()
        if (successLog != null) {
            DebugConfig.debugLog(category, successLog)
        }
        Result.success(result)
    } catch (e: Exception) {
        DebugConfig.error(category, errorLog, e)
        Result.failure(e)
    }
}
