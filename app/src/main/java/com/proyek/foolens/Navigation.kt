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
import com.proyek.foolens.ui.allergens.AllergenGuideScreen
import com.proyek.foolens.ui.allergens.AllergensScreen
import com.proyek.foolens.ui.allergens.AllergensViewModel
import com.proyek.foolens.ui.allergens.add.AddAllergenScreen
import com.proyek.foolens.ui.allergens.detail.AllergenDetailScreen
import com.proyek.foolens.ui.auth.login.LoginScreen
import com.proyek.foolens.ui.auth.password.InputEmailScreen
import com.proyek.foolens.ui.auth.password.ConfirmEmailScreen
import com.proyek.foolens.ui.auth.password.VerificationCodeScreen
import com.proyek.foolens.ui.auth.password.ChangePasswordScreen
import com.proyek.foolens.ui.auth.register.RegisterScreen
import com.proyek.foolens.ui.component.BottomNavItem
import com.proyek.foolens.ui.component.FoolensBottomNavigation
import com.proyek.foolens.ui.history.ScanHistoryScreen
import com.proyek.foolens.ui.history.detail.ScanDetailScreen
import com.proyek.foolens.ui.home.HomeScreen
import com.proyek.foolens.ui.home.HomeViewModel
import com.proyek.foolens.ui.landing.LandingScreen
import com.proyek.foolens.ui.profile.EditProfileScreen
import com.proyek.foolens.ui.profile.ProfileScreen
import com.proyek.foolens.ui.profile.ProfileViewModel
import com.proyek.foolens.ui.scan.ScanScreen
import com.proyek.foolens.ui.splash.SplashScreen
import com.proyek.foolens.util.DoubleBackPressHandler

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val permissionsState = rememberPermissionState(Manifest.permission.CAMERA)
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?") ?: ""
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        permissionsState.launchPermissionRequest()
    }

    val showBottomBar = currentRoute in listOf(
        BottomNavItem.Home.route,
        BottomNavItem.Allergens.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DoubleBackPressHandler(
                    onFirstBackPress = {
                        Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                    },
                    onSecondBackPress = {
                        (context as? Activity)?.finish()
                    }
                )

                FoolensBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
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
                },
                onClose = {
                    navController.navigate("landing") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate("forgot_password_email")
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
                    Log.d("Navigation", "Registration successful, navigating to home")
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onClose = {
                    navController.navigate("landing") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("forgot_password_email") {
            InputEmailScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNext = { email ->
                    navController.navigate("forgot_password_confirm/$email")
                }
            )
        }

        composable(
            route = "forgot_password_confirm/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ConfirmEmailScreen(
                email = email,
                onBack = {
                    navController.popBackStack()
                },
                onNext = { email ->
                    navController.navigate("forgot_password_verify/$email")
                }
            )
        }

        composable(
            route = "forgot_password_verify/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerificationCodeScreen(
                email = email,
                onBack = {
                    navController.popBackStack()
                },
                onVerified = { resetToken ->
                    navController.navigate("forgot_password_change/$email/$resetToken")
                }
            )
        }

        composable(
            route = "forgot_password_change/{email}/{resetToken}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("resetToken") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ChangePasswordScreen(
                email = email,
                onBack = {
                    navController.popBackStack()
                },
                onPasswordChanged = {
                    navController.navigate("login") {
                        popUpTo("forgot_password_email") { inclusive = true }
                    }
                }
            )
        }

        composable(BottomNavItem.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            val navBackStackEntry = navController.currentBackStackEntry
            val shouldRefresh = navBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>("should_refresh_user_data") ?: false

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    viewModel.refreshData()
                    navBackStackEntry?.savedStateHandle?.set("should_refresh_user_data", false)
                }
            }

            HomeScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onHistoryClick = {
                    navController.navigate("history")
                },
                onNavigateToDetail = { scanId ->
                    navController.navigate("scan_detail/$scanId")
                }
            )
        }

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

        composable("edit_profile") {
            val viewModel: ProfileViewModel = hiltViewModel()
            EditProfileScreen(
                onBack = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_user_data", true)
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        composable(BottomNavItem.Allergens.route) {
            val viewModel: AllergensViewModel = hiltViewModel()
            val navBackStackEntry = navController.currentBackStackEntry
            val shouldRefresh = navBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>("should_refresh_allergens") ?: false

            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    viewModel.refreshAllergens()
                    navBackStackEntry?.savedStateHandle?.set("should_refresh_allergens", false)
                }
            }

            AllergensScreen(
                viewModel = viewModel,
                onNavigateToAddAllergen = {
                    navController.navigate("add_allergen")
                },
                onNavigateToAllergenDetail = { allergen ->
                    selectedAllergen = allergen
                    navController.navigate("allergen_detail")
                },
                onNavigateToGuide = {
                    navController.navigate("allergen_guide")
                }
            )
        }

        composable("allergen_guide") {
            AllergenGuideScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("add_allergen") {
            AddAllergenScreen(
                onClose = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_allergens", true)
                    navController.popBackStack()
                }
            )
        }

        composable("allergen_detail") {
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
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_allergens", true)
                    navController.popBackStack()
                },
                onDelete = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("should_refresh_allergens", true)
                    navController.popBackStack()
                }
            )
        }

        composable("scan") {
            ScanScreen(
                onClose = {
                    navController.popBackStack()
                },
                onDetailsClick = { scanId ->
                    navController.navigate("scan_detail/$scanId")
                }
            )
        }

        composable(
            route = "history?deletionTriggered={deletionTriggered}",
            arguments = listOf(
                navArgument("deletionTriggered") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val deletionTriggered = backStackEntry.arguments?.getBoolean("deletionTriggered") ?: false
            ScanHistoryScreen(
                onBack = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo("history") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToDetail = { scanId ->
                    navController.navigate("scan_detail/$scanId")
                },
                deletionTriggered = deletionTriggered
            )
        }

        // Modifikasi rute scan_detail
        composable(
            route = "scan_detail/{scanId}",
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
            ScanDetailScreen(
                scanId = scanId,
                onBack = { deletionTriggered ->
                    navController.navigate("history?deletionTriggered=$deletionTriggered") {
                        popUpTo("scan_detail") { inclusive = true }
                    }
                }
            )
        }
    }
}