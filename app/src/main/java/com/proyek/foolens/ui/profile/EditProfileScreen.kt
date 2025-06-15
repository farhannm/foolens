package com.proyek.foolens.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.proyek.foolens.ui.component.FullScreenImageDialog
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<Any?>(null) }

    // Reload Profile
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Image selection
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        val buffer = ByteArray(1024) // Buffer sebesar 1KB
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                        output.flush()
                    }
                }
                viewModel.setSelectedImageUri(uri, file)
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar("Gagal memilih gambar: ${e.message}")
                }
            }
        }
    }

    val imageLoader = remember { ImageUtils.createProfileImageLoader(context) }

    // Show snackbar for success or error messages
    LaunchedEffect(state.successMessage, state.errorMessage) {
        when {
            !state.successMessage.isNullOrEmpty() -> {
                snackbarHostState.showSnackbar(state.successMessage ?: "")
                viewModel.resetMessages()
                viewModel.loadProfile()
                viewModel.clearSelectedImageUri()
            }
            !state.errorMessage.isNullOrEmpty() -> {
                snackbarHostState.showSnackbar(state.errorMessage ?: "")
                viewModel.resetMessages()
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Text(
                    text = "Edit Profile",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
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
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Profile Image
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (state.selectedImageUri != null) {
                                            selectedImageUrl = state.selectedImageUri
                                            showImageDialog = true
                                        } else if (!state.profile?.profilePicture.isNullOrEmpty()) {
                                            selectedImageUrl =
                                                "${ImageUtils.getFullImageUrl(state.profile?.profilePicture)}?t=${System.currentTimeMillis()}"
                                            showImageDialog = true
                                        }
                                    }
                            ) {
                                if (state.selectedImageUri != null) {
                                    AsyncImage(
                                        model = state.selectedImageUri,
                                        contentDescription = "Selected Profile Picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        imageLoader = imageLoader
                                    )
                                } else if (!state.profile?.profilePicture.isNullOrEmpty()) {
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

                            // Edit icon overlay
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFBBC41))
                                    .clickable {
                                        imagePickerLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.change_profile),
                                    contentDescription = "Change Profile Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    TextField(
                        value = state.nameField,
                        onValueChange = { viewModel.updateNameField(it) },
                        label = { Text("Name", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Black,
                            unfocusedIndicatorColor = Color.Black,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )

                    // Phone field with underline design
                    TextField(
                        value = state.phoneField,
                        onValueChange = { viewModel.updatePhoneField(it) },
                        label = { Text("No telepon", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Black,
                            unfocusedIndicatorColor = Color.Black,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                    )
                }


                // Save button
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .height(56.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC7F131),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Save",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // View Profile Dialog
    if (showImageDialog && selectedImageUrl != null) {
        FullScreenImageDialog(
            imageUrl = selectedImageUrl,
            onDismiss = { showImageDialog = false }
        )
    }
}