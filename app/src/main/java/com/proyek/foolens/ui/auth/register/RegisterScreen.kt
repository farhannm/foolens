package com.proyek.foolens.ui.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.ui.component.CommonButton
import com.proyek.foolens.ui.component.LoadingView

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Navigate to home screen if registration is successful
    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            onRegisterSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            LoadingView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Name field
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Nama") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email field
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password field
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChange(it) },
                    label = { Text("Konfirmasi Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Register button
                CommonButton(
                    text = "Register",
                    onClick = { viewModel.register() },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Login link
                TextButton(
                    onClick = onLoginClick
                ) {
                    Text("Sudah punya akun? Login di sini")
                }
            }
        }
    }
}