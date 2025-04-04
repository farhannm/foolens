package com.proyek.foolens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.proyek.foolens.ui.allergens.AllergensScreen
import com.proyek.foolens.ui.auth.login.LoginScreen
import com.proyek.foolens.ui.auth.register.RegisterScreen
import com.proyek.foolens.ui.component.BottomNavItem
import com.proyek.foolens.ui.component.FoolensBottomNavigation
import com.proyek.foolens.ui.home.HomeScreen
import com.proyek.foolens.ui.landing.LandingScreen
import com.proyek.foolens.ui.scan.ScanScreen
import com.proyek.foolens.ui.splash.SplashScreen
import android.Manifest

/**
 * Komponen navigasi utama aplikasi
 * Mengatur semua rute navigasi dan transisi antar screen
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val permissionsState = rememberPermissionState(Manifest.permission.CAMERA)
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Request camera permission when app starts
    LaunchedEffect(Unit) {
        permissionsState.launchPermissionRequest()
    }

    // Check if the current route is one of the bottom nav routes
    val showBottomBar = currentRoute in listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Allergens.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                FoolensBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    onCameraClick = {
                        navController.navigate("scan")
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            MainNavHost(navController = navController)
        }
    }
}

@Composable
fun MainNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToLanding = {
                    // Skip landing, go directly to home instead
                    navController.navigate("home") {
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

        // Landing screen navigation (currently skipped, going directly to home)
        composable("landing") {
            // Skipping implementation - we're going directly to home
        }

        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate("register") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    // Navigate to home and clear back stack
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
                    // Navigate to home and clear back stack
                    navController.navigate("home") {
                        popUpTo("landing") { inclusive = true }
                    }
                }
            )
        }

        // Main app screens with bottom navigation
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onLogout = {
                    // Saat logout, kembali ke login screen (skip landing)
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable(BottomNavItem.Allergens.route) {
            AllergensScreen()
        }

        // Fixed: Single composable for scan screen instead of nested composables
        composable("scan") {
            ScanScreen(
                onClose = {
                    navController.popBackStack()
                }
            )
        }
    }
}