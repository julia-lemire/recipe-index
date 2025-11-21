package com.recipeindex.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.util.Size
import com.recipeindex.app.data.entities.MediaItem
import com.recipeindex.app.data.entities.MediaType
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

/**
 * MediaDownloader - Handles downloading and processing of recipe media (images and videos)
 *
 * Features:
 * - Downloads images from URLs
 * - Downloads videos from URLs
 * - Compresses images to save storage
 * - Generates thumbnails for videos
 * - Saves media to app's internal storage
 * - Preserves media beyond URL availability
 *
 * Storage structure:
 * - Images: app_data/media/images/
 * - Videos: app_data/media/videos/
 * - Thumbnails: app_data/media/thumbnails/
 */
class MediaDownloader(
    private val context: Context,
    private val httpClient: HttpClient
) {
    companion object {
        // Image compression quality (0-100)
        private const val IMAGE_QUALITY = 85

        // Maximum image dimensions for compression
        private const val MAX_IMAGE_WIDTH = 1920
        private const val MAX_IMAGE_HEIGHT = 1920

        // Thumbnail size for videos
        private const val THUMBNAIL_WIDTH = 320
        private const val THUMBNAIL_HEIGHT = 240

        // Supported video extensions
        private val VIDEO_EXTENSIONS = setOf("mp4", "webm", "mkv", "avi", "mov", "m4v", "3gp")

        // Supported image extensions
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")
    }

    /**
     * Downloads and processes a single media item from URL
     * Returns MediaItem with local file path, or null on failure
     */
    suspend fun downloadMedia(url: String): MediaItem? = withContext(Dispatchers.IO) {
        try {
            val mediaType = detectMediaType(url)

            when (mediaType) {
                MediaType.IMAGE -> downloadAndCompressImage(url)
                MediaType.VIDEO -> downloadAndProcessVideo(url)
                null -> null
            }
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Failed to download media from $url: ${e.message}"
            )
            null
        }
    }

    /**
     * Downloads and processes multiple media items from URLs
     * Returns list of successfully downloaded MediaItems
     */
    suspend fun downloadMediaList(urls: List<String>): List<MediaItem> = withContext(Dispatchers.IO) {
        urls.mapNotNull { url ->
            try {
                downloadMedia(url)
            } catch (e: Exception) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[MEDIA] Failed to download media from $url: ${e.message}"
                )
                null
            }
        }
    }

    /**
     * Detects media type from URL extension
     */
    private fun detectMediaType(url: String): MediaType? {
        val extension = url.substringAfterLast('.', "").lowercase()
        return when {
            extension in IMAGE_EXTENSIONS -> MediaType.IMAGE
            extension in VIDEO_EXTENSIONS -> MediaType.VIDEO
            else -> {
                // If no extension, try to infer from content type or default to image
                // Most recipe images don't have extensions in URLs
                MediaType.IMAGE
            }
        }
    }

    /**
     * Downloads an image, compresses it, and saves to local storage
     */
    private suspend fun downloadAndCompressImage(url: String): MediaItem? {
        try {
            // Download image bytes
            val imageBytes = httpClient.get(url).readBytes()

            // Decode bitmap
            val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return null

            // Calculate scaled dimensions maintaining aspect ratio
            val scaledDimensions = calculateScaledDimensions(
                originalBitmap.width,
                originalBitmap.height,
                MAX_IMAGE_WIDTH,
                MAX_IMAGE_HEIGHT
            )

            // Scale bitmap if needed
            val scaledBitmap = if (scaledDimensions.first < originalBitmap.width ||
                                    scaledDimensions.second < originalBitmap.height) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    scaledDimensions.first,
                    scaledDimensions.second,
                    true
                ).also {
                    originalBitmap.recycle()
                }
            } else {
                originalBitmap
            }

            // Save compressed image
            val filename = "${UUID.randomUUID()}.jpg"
            val file = File(getImagesDir(), filename)

            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
            }

            scaledBitmap.recycle()

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Downloaded and compressed image: $url -> ${file.absolutePath}"
            )

            return MediaItem(
                type = MediaType.IMAGE,
                path = file.absolutePath
            )
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Failed to download/compress image from $url: ${e.message}"
            )
            return null
        }
    }

    /**
     * Downloads a video, generates a thumbnail, and saves both to local storage
     */
    private suspend fun downloadAndProcessVideo(url: String): MediaItem? {
        try {
            // Download video bytes
            val videoBytes = httpClient.get(url).readBytes()

            // Save video file
            val videoFilename = "${UUID.randomUUID()}.${url.substringAfterLast('.', "mp4")}"
            val videoFile = File(getVideosDir(), videoFilename)
            videoFile.writeBytes(videoBytes)

            // Generate thumbnail
            val thumbnailPath = generateVideoThumbnail(videoFile.absolutePath)

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Downloaded video: $url -> ${videoFile.absolutePath}"
            )

            return MediaItem(
                type = MediaType.VIDEO,
                path = videoFile.absolutePath,
                thumbnailPath = thumbnailPath
            )
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Failed to download/process video from $url: ${e.message}"
            )
            return null
        }
    }

    /**
     * Generates a thumbnail for a video file
     * Returns thumbnail path or null on failure
     */
    private fun generateVideoThumbnail(videoPath: String): String? {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)

            // Get frame at 1 second
            val bitmap = retriever.getFrameAtTime(
                1_000_000, // 1 second in microseconds
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            ) ?: return null

            retriever.release()

            // Scale thumbnail
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                THUMBNAIL_WIDTH,
                THUMBNAIL_HEIGHT,
                true
            )
            bitmap.recycle()

            // Save thumbnail
            val thumbnailFilename = "${UUID.randomUUID()}_thumb.jpg"
            val thumbnailFile = File(getThumbnailsDir(), thumbnailFilename)

            FileOutputStream(thumbnailFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
            }

            scaledBitmap.recycle()

            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Generated thumbnail: ${thumbnailFile.absolutePath}"
            )

            return thumbnailFile.absolutePath
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Failed to generate thumbnail for $videoPath: ${e.message}"
            )
            return null
        }
    }

    /**
     * Calculates scaled dimensions maintaining aspect ratio
     */
    private fun calculateScaledDimensions(
        width: Int,
        height: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        if (width <= maxWidth && height <= maxHeight) {
            return Pair(width, height)
        }

        val widthRatio = maxWidth.toFloat() / width
        val heightRatio = maxHeight.toFloat() / height
        val ratio = minOf(widthRatio, heightRatio)

        return Pair(
            (width * ratio).toInt(),
            (height * ratio).toInt()
        )
    }

    /**
     * Gets or creates the images directory
     */
    private fun getImagesDir(): File {
        val dir = File(context.filesDir, "media/images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Gets or creates the videos directory
     */
    private fun getVideosDir(): File {
        val dir = File(context.filesDir, "media/videos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Gets or creates the thumbnails directory
     */
    private fun getThumbnailsDir(): File {
        val dir = File(context.filesDir, "media/thumbnails")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Deletes a media file and its thumbnail (if video)
     * Returns true if deletion was successful
     */
    fun deleteMedia(mediaItem: MediaItem): Boolean {
        var success = true

        // Delete main file
        val mainFile = File(mediaItem.path)
        if (mainFile.exists()) {
            success = mainFile.delete()
        }

        // Delete thumbnail if exists
        mediaItem.thumbnailPath?.let { thumbnailPath ->
            val thumbnailFile = File(thumbnailPath)
            if (thumbnailFile.exists()) {
                success = thumbnailFile.delete() && success
            }
        }

        return success
    }

    /**
     * Copies a local media file (from gallery picker) to app storage
     * Handles compression for images and thumbnail generation for videos
     */
    suspend fun copyLocalMedia(sourceUri: android.net.Uri, mediaType: MediaType): MediaItem? =
        withContext(Dispatchers.IO) {
            try {
                when (mediaType) {
                    MediaType.IMAGE -> copyAndCompressLocalImage(sourceUri)
                    MediaType.VIDEO -> copyAndProcessLocalVideo(sourceUri)
                }
            } catch (e: Exception) {
                DebugConfig.debugLog(
                    DebugConfig.Category.IMPORT,
                    "[MEDIA] Failed to copy local media: ${e.message}"
                )
                null
            }
        }

    /**
     * Copies and compresses a local image file
     */
    private suspend fun copyAndCompressLocalImage(sourceUri: android.net.Uri): MediaItem? {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return null

            // Calculate scaled dimensions
            val scaledDimensions = calculateScaledDimensions(
                originalBitmap.width,
                originalBitmap.height,
                MAX_IMAGE_WIDTH,
                MAX_IMAGE_HEIGHT
            )

            // Scale bitmap if needed
            val scaledBitmap = if (scaledDimensions.first < originalBitmap.width ||
                                    scaledDimensions.second < originalBitmap.height) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    scaledDimensions.first,
                    scaledDimensions.second,
                    true
                ).also {
                    originalBitmap.recycle()
                }
            } else {
                originalBitmap
            }

            // Save compressed image
            val filename = "${UUID.randomUUID()}.jpg"
            val file = File(getImagesDir(), filename)

            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
            }

            scaledBitmap.recycle()

            return MediaItem(
                type = MediaType.IMAGE,
                path = file.absolutePath
            )
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Failed to copy/compress local image: ${e.message}"
            )
            return null
        }
    }

    /**
     * Copies a local video file and generates thumbnail
     */
    private suspend fun copyAndProcessLocalVideo(sourceUri: android.net.Uri): MediaItem? {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null

            // Save video file
            val videoFilename = "${UUID.randomUUID()}.mp4"
            val videoFile = File(getVideosDir(), videoFilename)
            videoFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            // Generate thumbnail
            val thumbnailPath = generateVideoThumbnail(videoFile.absolutePath)

            return MediaItem(
                type = MediaType.VIDEO,
                path = videoFile.absolutePath,
                thumbnailPath = thumbnailPath
            )
        } catch (e: Exception) {
            DebugConfig.debugLog(
                DebugConfig.Category.IMPORT,
                "[MEDIA] Failed to copy/process local video: ${e.message}"
            )
            return null
        }
    }
}
