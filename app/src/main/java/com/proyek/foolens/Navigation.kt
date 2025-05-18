package com.proyek.foolens

import android.Manifest
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.proyek.foolens.domain.model.AllergenCategory
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.ui.allergens.AllergensScreen
import com.proyek.foolens.ui.allergens.AllergensViewModel
import com.proyek.foolens.ui.allergens.add.AddAllergenScreen
import com.proyek.foolens.ui.allergens.detail.AllergenDetailScreen
import com.proyek.foolens.ui.auth.login.LoginScreen
import com.proyek.foolens.ui.auth.register.RegisterScreen
import com.proyek.foolens.ui.component.BottomNavItem
import com.proyek.foolens.ui.component.FoolensBottomNavigation
import com.proyek.foolens.ui.history.ScanHistoryScreen
import com.proyek.foolens.ui.history.detail.ScanDetailScreen
import com.proyek.foolens.ui.home.HomeScreen
import com.proyek.foolens.ui.landing.LandingScreen
import com.proyek.foolens.ui.profile.EditProfileScreen
import com.proyek.foolens.ui.profile.ProfileScreen
import com.proyek.foolens.ui.profile.ProfileViewModel
import com.proyek.foolens.ui.scan.ScanScreen
import com.proyek.foolens.ui.splash.SplashScreen
import com.proyek.foolens.util.DoubleBackPressHandler

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
    val context = LocalContext.current

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
                // Tambahkan DoubleBackPressHandler di dalam bottom navigation
                DoubleBackPressHandler(
                    onFirstBackPress = {
                        Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                    },
                    onSecondBackPress = {
                        // Keluar dari aplikasi
                        (context as? Activity)?.finish()
                    }
                )

                FoolensBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Hapus semua route sebelumnya
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Hindari multiple copies
                            launchSingleTop = true
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
    var selectedAllergen by remember { mutableStateOf<UserAllergen?>(null) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Splash Screen - Entry point of the app
        composable("splash") {
            SplashScreen(
                onNavigateToLanding = {
                    navController.navigate("landing") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    // Directly navigate to home when user is logged in
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
                    // Navigate to home and clear back stack
                    navController.navigate("home") {
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onClose = {
                    // Navigate back to landing screen
                    navController.navigate("landing") {
                        popUpTo("login") { inclusive = true }
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
                    // Directly navigate to home when registration is successful
                    Log.d("Navigation", "Registration successful, navigating to home")
                    navController.navigate("home") {
                        // Clear all previous destinations
                        popUpTo(0) { inclusive = true }
                    }
                },
                onClose = {
                    // Navigate back to landing screen
                    navController.navigate("landing") {
                        popUpTo("register") { inclusive = true }
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
                },
                onProfileClick = {
                    // Navigate to profile screen
                    navController.navigate("profile")
                },
                onHistoryClick = {
                    // Navigate to list history screen
                    navController.navigate("history")
                }
            )
        }

        // Profile screen
        composable("profile") {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onEditProfile = {
                    navController.navigate("edit_profile")
                }
            )
        }

        // Edit Profile screen
        composable("edit_profile") {
            val viewModel: ProfileViewModel = hiltViewModel()
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // Allergens screen dengan navigasi ke detail dan add allergen
        composable(BottomNavItem.Allergens.route) {
            val viewModel: AllergensViewModel = hiltViewModel()
            val navBackStackEntry = navController.currentBackStackEntry
            val shouldRefresh = navBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>("should_refresh_allergens") ?: false

            // LaunchedEffect untuk trigger refresh
            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    viewModel.refreshAllergens()

                    // Reset the flag
                    navBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_allergens", false)
                }
            }

            AllergensScreen(
                viewModel = viewModel,
                onNavigateToAddAllergen = {
                    navController.navigate("add_allergen")
                },
                onNavigateToAllergenDetail = { allergen ->
                    // Demo: simpan allergen yang dipilih
                    selectedAllergen = allergen
                    navController.navigate("allergen_detail")
                }
            )
        }

        // Add allergen screen
        composable("add_allergen") {
            AddAllergenScreen(
                onClose = {
                    navController.popBackStack()
                },
                onSuccess = {
                    // Set flag di SavedStateHandle untuk refresh
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_allergens", true)
                    navController.popBackStack()
                }
            )
        }

        // Allergen detail screen
        composable("allergen_detail") {
            // Use the allergen that was stored in navigation scope
            val allergen = selectedAllergen ?: UserAllergen(
                id = 0,
                name = "Unknown Allergen",
                description = null,
                alternativeNames = null,
                category = AllergenCategory(
                    id = 0,
                    name = "Unknown",
                    icon = null
                ),
                severityLevel = 1,
                notes = null,
                createdAt = "",
                updatedAt = ""
            )

            AllergenDetailScreen(
                allergen = allergen,
                onClose = {
                    navController.popBackStack()
                },
                onSave = {
                    // Set flag to refresh allergen list when navigating back
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_allergens", true)
                    navController.popBackStack()
                },
                onDelete = {
                    // Set flag to refresh allergen list when navigating back
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_allergens", true)
                    navController.popBackStack()
                }
            )
        }

        // Updated scan screen with OCR-based allergen detection
        composable("scan") {
            ScanScreen(
                onClose = {
                    navController.popBackStack()
                }
            )
        }

        // Navigate to History Screen
        composable("history") {
            ScanHistoryScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { scanId ->
                    navController.navigate("history_detail/$scanId")
                }
            )
        }

        // Navigate to History Scan Detail
        composable(
            route = "history_detail/{scanId}",
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
            ScanDetailScreen(
                scanId = scanId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}