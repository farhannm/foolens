package com.proyek.foolens.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun ScanScreen(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Check if permission is already granted
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Full screen camera with header
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera view or permission message
        if (hasCameraPermission) {
            CameraPreview()
        } else {
            // Permission not yet granted - show a message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera permission required.\nPlease restart the app and grant permission.",
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Header with back button and title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Back button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                // "Scan" title, centered in the header
                Text(
                    text = "Scan",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun CameraPreview() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create PreviewView
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Handle camera setup
    DisposableEffect(lifecycleOwner) {
        Log.d("CameraPreview", "Setting up camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                Log.d("CameraPreview", "Camera provider obtained")

                // Preview use case
                val preview = Preview.Builder().build().also {
                    Log.d("CameraPreview", "Setting surface provider")
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()
                Log.d("CameraPreview", "Unbound previous camera uses")

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
                Log.d("CameraPreview", "Camera bound successfully")

            } catch (exc: Exception) {
                Log.e("CameraPreview", "Camera setup failed", exc)
            }
        }, executor)

        onDispose {
            Log.d("CameraPreview", "Disposing camera")
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                Log.d("CameraPreview", "Camera unbound successfully")
            } catch (exc: Exception) {
                Log.e("CameraPreview", "Failed to unbind camera", exc)
            }
        }
    }

    AndroidView(
        factory = {
            Log.d("CameraPreview", "Creating AndroidView with PreviewView")
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}