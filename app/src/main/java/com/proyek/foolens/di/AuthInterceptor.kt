package com.proyek.foolens.di

import com.proyek.foolens.util.Constants
import com.proyek.foolens.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // Get token from TokenManager
        val token = tokenManager.getToken()

        // For debugging
        println("Intercepting request to: $url")
        println("Token available: ${token.isNotEmpty()}")

        // Add Authorization header only if token exists and request is not for login/register
        return if (token.isNotEmpty() && !isAuthEndpoint(url)) {
            val newRequest = originalRequest.newBuilder()
                .header(Constants.HEADER_AUTHORIZATION, "Bearer $token")
                .build()

            println("Added Authorization header for URL: $url")
            chain.proceed(newRequest)
        } else {
            println("No Authorization header added for URL: $url")
            chain.proceed(originalRequest)
        }
    }

    /**
     * Check if the request URL is for authentication endpoints
     * This prevents adding authorization header to login/register requests
     */
    private fun isAuthEndpoint(url: String): Boolean {
        val result = url.contains(Constants.ENDPOINT_LOGIN) || url.contains(Constants.ENDPOINT_REGISTER)
        println("Is auth endpoint ($url): $result")
        return result
    }
}