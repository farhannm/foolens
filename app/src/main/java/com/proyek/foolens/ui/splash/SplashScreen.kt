package com.proyek.foolens.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    onNavigateToLanding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    // Handle navigation based on login status
    LaunchedEffect(state.isLoading, state.isLoggedIn) {
        if (!state.isLoading) {
            // Sedikit delay untuk menampilkan splash screen
            kotlinx.coroutines.delay(1000)

            if (state.isLoggedIn) {
                onNavigateToHome()
            } else {
                onNavigateToLanding()
            }
        }
    }
}