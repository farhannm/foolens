package com.proyek.foolens.ui.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.proyek.foolens.R
import com.proyek.foolens.data.util.ImageUtils
import com.proyek.foolens.ui.component.ConfirmationDialog
import com.proyek.foolens.ui.component.FullScreenImageDialog
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    // Reload profile
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Navigate to login when logout is successful
    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            onLogout()
        }
    }

    // Track if logout dialog is showing
    var showLogoutDialog by remember { mutableStateOf(false) }

    val imageLoader = remember { ImageUtils.createProfileImageLoader(context) }

    // Show snackbar for success or error messages
    LaunchedEffect(state.successMessage, state.errorMessage) {
        when {
            !state.successMessage.isNullOrEmpty() -> {
                snackbarHostState.showSnackbar(state.successMessage ?: "")
                viewModel.resetMessages()
            }
            !state.errorMessage.isNullOrEmpty() -> {
                snackbarHostState.showSnackbar(state.errorMessage ?: "")
                viewModel.resetMessages()
            }
        }
    }

    LaunchedEffect(state.profile) {
        val profilePic = state.profile?.profilePicture
        Log.d("ProfileScreen", "Profile loaded: ${state.profile?.name}")
        Log.d("ProfileScreen", "Original profile picture URL: $profilePic")
        Log.d("ProfileScreen", "Converted URL: ${ImageUtils.getFullImageUrl(profilePic)}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile picture
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (!state.profile?.profilePicture.isNullOrEmpty()) {
                                        selectedImageUrl =
                                            "${ImageUtils.getFullImageUrl(state.profile?.profilePicture)}?t=${System.currentTimeMillis()}"
                                        showImageDialog = true
                                    }
                                }
                        ) {
                            if (!state.profile?.profilePicture.isNullOrEmpty()) {
                                AsyncImage(
                                    model = "${ImageUtils.getFullImageUrl(state.profile?.profilePicture)}?t=${System.currentTimeMillis()}",
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    imageLoader = imageLoader
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.profile_image),
                                    contentDescription = "Default Profile Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Name and email
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = state.profile?.name ?: "",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                color = Color.Black
                            )
                            Text(
                                text = state.profile?.email ?: "",
                                fontSize = 14.sp,
                                color = Color(0xFF1E88E5),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Phone label and number
                    Text(
                        text = "No telepon",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp, bottom = 4.dp),
                        textAlign = TextAlign.Left
                    )
                    Text(
                        text = state.profile?.phoneNumber ?: "",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Left
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Logout button
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .padding(end = 2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .clickable {
                                    showLogoutDialog = true
                                }
                        ) {
                            Text(
                                text = "Logout",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = Color.Red,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }

    // View image dialog
    if (showImageDialog && selectedImageUrl != null) {
        FullScreenImageDialog(
            imageUrl = selectedImageUrl,
            onDismiss = { showImageDialog = false }
        )
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Logout",
            message = "Are you sure you want to logout from this account?",
            icon = painterResource(id = R.drawable.ilustration_sticker),
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            }
        )
    }
}