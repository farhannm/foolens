package com.proyek.foolens.di

import com.proyek.foolens.data.preferences.PreferencesManager
import com.proyek.foolens.util.Constants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = preferencesManager.getString(Constants.PREF_AUTH_TOKEN, "")

        return if (token.isNotEmpty()) {
            val newRequest = originalRequest.newBuilder()
                .header(Constants.HEADER_AUTHORIZATION, "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}