package com.proyek.foolens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyek.foolens.ui.auth.login.LoginScreen
import com.proyek.foolens.ui.auth.register.RegisterScreen
import com.proyek.foolens.ui.home.HomeScreen
import com.proyek.foolens.ui.landing.LandingScreen
import com.proyek.foolens.ui.splash.SplashScreen

/**
 * Komponen navigasi utama aplikasi
 * Mengatur semua rute navigasi dan transisi antar screen
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToLanding = {
                    navController.navigate("landing") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("landing") {
            LandingScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate("register") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("landing") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("landing") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onLogout = {
                    // Saat logout, kembali ke landing screen
                    navController.navigate("landing") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}