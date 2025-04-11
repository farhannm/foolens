package com.proyek.foolens

import android.Manifest
import android.util.Log
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
import com.proyek.foolens.ui.home.HomeScreen
import com.proyek.foolens.ui.landing.LandingScreen
import com.proyek.foolens.ui.scan.ScanScreen
import com.proyek.foolens.ui.splash.SplashScreen

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
    // Untuk Demo: Simpan sementara selected allergen di Navigation scope
    var selectedAllergen by remember { mutableStateOf<UserAllergen?>(null) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // ... Composables yang sudah ada (splash, landing, login, dll)

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
                }
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
            // Gunakan allergen yang disimpan atau default empty allergen
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
                onSave = { severity, notes ->
                    // Di sini akan memanggil ViewModel untuk update
                    // Untuk demo kita hanya pop back
                    navController.popBackStack()
                },
                onDelete = {
                    // Di sini akan memanggil ViewModel untuk delete
                    // Untuk demo kita hanya pop back
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
    }
}