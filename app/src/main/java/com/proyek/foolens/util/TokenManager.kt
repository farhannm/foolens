package com.proyek.foolens.util

import com.proyek.foolens.data.preferences.PreferencesManager
import com.proyek.foolens.util.Constants.PREF_AUTH_TOKEN
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    // In-memory cache for quick access
    private var cachedToken: String = ""

    /**
     * Save token to both preferences and in-memory cache
     */
    fun saveToken(token: String) {
        println("TokenManager: Saving token: $token")
        cachedToken = token
        preferencesManager.saveString(PREF_AUTH_TOKEN, token)
    }

    /**
     * Get token from in-memory cache or preferences
     */
    fun getToken(): String {
        // First check in-memory cache
        if (cachedToken.isNotEmpty()) {
            println("TokenManager: Returning token from cache")
            return cachedToken
        }

        // If not in cache, retrieve from preferences
        val token = preferencesManager.getString(PREF_AUTH_TOKEN, "")
        cachedToken = token

        println("TokenManager: Getting token from preferences: ${if (token.isNotEmpty()) "Available" else "Not available"}")
        return token
    }

    /**
     * Check if token exists
     */
    fun hasToken(): Boolean {
        // First check in-memory cache
        if (cachedToken.isNotEmpty()) {
            println("TokenManager: Has token from cache: true")
            return true
        }

        // Check in preferences
        val hasToken = preferencesManager.getString(PREF_AUTH_TOKEN, "").isNotEmpty()
        println("TokenManager: Has token from preferences: $hasToken")
        return hasToken
    }

    /**
     * Clear token from both in-memory cache and preferences
     */
    fun clearToken() {
        println("TokenManager: Clearing token")
        cachedToken = ""
        preferencesManager.saveString(PREF_AUTH_TOKEN, "")
    }
}