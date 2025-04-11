package com.proyek.foolens.util

object Constants {
    const val BASE_URL = "https://foolens.my.id/api/"

    // Network Constants
    const val NETWORK_TIMEOUT = 30L
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"

    // Authentication Endpoints
    const val ENDPOINT_LOGIN = "auth/login"
    const val ENDPOINT_REGISTER = "auth/register"
    const val ENDPOINT_LOGOUT = "auth/logout"
    const val ENDPOINT_PROFILE = "auth/me"

    // Allergen Endpoints
    const val ENDPOINT_DETECT_ALLERGENS = "allergens/detect"
    const val ENDPOINT_GET_ALL_ALLERGENS = "allergens"
    const val ENDPOINT_SEARCH_ALLERGENS = "allergens/search"

    // User Allergen Management Endpoints
    const val ENDPOINT_GET_USER_ALLERGENS = "users/{user_id}/allergens"
    const val ENDPOINT_ADD_USER_ALLERGENS = "users/{user_id}/allergens"
    const val ENDPOINT_UPDATE_USER_ALLERGEN = "users/{user_id}/allergens/{allergen_id}"
    const val ENDPOINT_DELETE_USER_ALLERGEN = "users/{user_id}/allergens/{allergen_id}"

    // Product Safety Statistics Endpoint
    const val ENDPOINT_PRODUCT_SAFETY_STATS = "users/{user_id}/product-safety-stats"

    // Scan History Endpoints
    const val ENDPOINT_SAVE_SCAN = "scans"
    const val ENDPOINT_DELETE_SCAN = "scans/{scan_id}"
    const val ENDPOINT_GET_SCAN_HISTORY = "scans"
    const val ENDPOINT_GET_SCAN_COUNT = "scans/count-summary"

    // User Preferences
    const val PREF_AUTH_TOKEN = "auth_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_IS_LOGGED_IN = "is_logged_in"

    // Default User Parameters
    const val DEFAULT_PAGE_SIZE = 20
    const val DEFAULT_PAGE_NUMBER = 1
}