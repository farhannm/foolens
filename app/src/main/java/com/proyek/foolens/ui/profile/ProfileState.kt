package com.proyek.foolens.ui.profile

import android.net.Uri
import com.proyek.foolens.domain.model.Profile

/**
 * State class untuk ProfileScreen
 */
data class ProfileState(
    val profile: Profile? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedOut: Boolean = false,

    // Form fields
    val nameField: String = "",
    val phoneField: String = "",
    val selectedImageUri: Uri? = null
)