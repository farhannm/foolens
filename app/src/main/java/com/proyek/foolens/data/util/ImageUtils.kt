package com.proyek.foolens.data.util

import android.content.Context
import android.util.Log
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.proyek.foolens.util.Constants
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Utility class for handling images throughout the app
 */
object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * Constructs a full URL for an image based on a relative path
     *
     * @param relativePath The relative path from the API
     * @return A full URL that can be used to fetch the image
     */
    fun getFullImageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrEmpty()) {
            Log.w(TAG, "Relative path is null or empty")
            return null
        }

        // If the URL already starts with http:// or https://, return it as is
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            Log.d(TAG, "URL is already absolute: $relativePath")
            return relativePath
        }

        // For Laravel/storage URLs, we typically need to go to the domain root
        val domain = extractDomainFromBaseUrl(Constants.BASE_URL)
        val cleanPath = relativePath.trimStart('/')
        // Encode the path to handle spaces and special characters
        val encodedPath = URLEncoder.encode(cleanPath, StandardCharsets.UTF_8.toString())
            .replace("+", "%20") // Ensure spaces are encoded as %20, not +
        val fullUrl = "https://$domain/storage/$encodedPath"

        Log.d(TAG, "Converted $relativePath to $fullUrl")
        return fullUrl
    }

    /**
     * Extracts the domain name from a base URL
     */
    private fun extractDomainFromBaseUrl(baseUrl: String): String {
        val withoutProtocol = baseUrl
            .replace("https://", "")
            .replace("http://", "")
        val domain = withoutProtocol.split("/").firstOrNull() ?: "foolens.my.id"
        Log.d(TAG, "Extracted domain: $domain from baseUrl: $baseUrl")
        return domain
    }

    /**
     * Creates an image loader that can be used for profile images
     */
    fun createProfileImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.DISABLED) // For testing to avoid caching issues
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    /**
     * Creates an ImageRequest for a profile image with proper options
     */
    fun createProfileImageRequest(
        context: Context,
        imageUrl: String?,
        placeholderId: Int
    ): ImageRequest {
        val fullUrl = getFullImageUrl(imageUrl)
        Log.d(TAG, "Creating image request for URL: $fullUrl")

        return ImageRequest.Builder(context)
            .data(fullUrl)
            .placeholder(placeholderId)
            .crossfade(true)
            .error(placeholderId)
            .build()
    }
}