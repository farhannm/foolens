package com.proyek.foolens.ui.splash
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLanding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Use a lime green background color to match the provided image
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFCEEB44)),
        contentAlignment = Alignment.Center
    ) {
        // Display the logo text from drawable
        Image(
            painter = painterResource(id = R.drawable.logo_text),
            contentDescription = "Foolens Logo",
            modifier = Modifier
                .width(180.dp)
                .height(60.dp)
        )
    }

    // Handle navigation based on login status with a delay
    LaunchedEffect(state.isLoading) {
        // Wait until loading is complete plus a short delay for splash screen
        if (!state.isLoading) {
            delay(2000) // 2 seconds delay after loading completes

            if (state.isLoggedIn) {
                onNavigateToHome()
            } else {
                onNavigateToLanding()
            }
        }
    }
}