package com.proyek.foolens.ui.allergens

import com.proyek.foolens.domain.model.UserAllergen

data class AllergensState(
    val isLoading: Boolean = false,
    val userAllergens: List<UserAllergen> = emptyList(),
    val filteredAllergens: List<UserAllergen> = emptyList(),
    val errorMessage: String? = null,
    val selectedTab: Tab = Tab.PERSONAL,
    val isRefreshing: Boolean = false
) {
    enum class Tab {
        PERSONAL, COMMON
    }
}