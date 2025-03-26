package com.proyek.foolens.ui.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.ui.component.CommonButton

@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LandingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo/icon position
            // Image(
            //     painter = painterResource(id = R.drawable.app_logo),
            //     contentDescription = "App Logo",
            //     modifier = Modifier.size(160.dp)
            // )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Auth MVVM App",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aplikasi dengan arsitektur MVVM untuk manajemen autentikasi yang aman dan efisien",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Button
            CommonButton(
                text = "Login",
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Register Button
            CommonButton(
                text = "Register",
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Additional information or features can be added here
            Text(
                text = "© 2025 My App. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}