package com.proyek.foolens.util

object Constants {
    // API Related Constants
    const val BASE_URL = "https://api.example.com/"
    const val API_VERSION = "v1"

    // Network Constants
    const val NETWORK_TIMEOUT = 30L
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"

    // Endpoints
    const val ENDPOINT_LOGIN = "auth/login"
    const val ENDPOINT_REGISTER = "auth/register"
    const val ENDPOINT_USER = "user"

    // User Preferences
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_IS_LOGGED_IN = "is_logged_in"

    // Default User Parameters
    const val DEFAULT_PAGE_SIZE = 20
    const val DEFAULT_PAGE_NUMBER = 1
}