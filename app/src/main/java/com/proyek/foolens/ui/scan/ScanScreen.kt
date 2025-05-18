package com.proyek.foolens.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.proyek.foolens.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onClose: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Analysis thread
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // Text recognizer
    val textRecognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    // Barcode scanner
    val barcodeScannerOptions = remember {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93
            )
            .build()
    }
    val barcodeScanner = remember { BarcodeScanning.getClient(barcodeScannerOptions) }

    // Check if permission is already granted
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request camera permission if not already granted
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, start scanning
            viewModel.startScanning()
        } else {
            // Show error message
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required for scanning")
            }
        }
    }

    // Launch permission request if needed
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            viewModel.startScanning()
        }
    }

    // Show error message as snackbar if present
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    // Cleanup the executor when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Main UI
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            // Header with back button and title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
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

            // Tabs for mode selection
            TabRow(
                selectedTabIndex = if (state.currentScanMode == ScanMode.ALLERGEN) 0 else 1,
                containerColor = Color.White,
                contentColor = Color(0xFF062207)
            ) {
                Tab(
                    selected = state.currentScanMode == ScanMode.ALLERGEN,
                    onClick = { viewModel.setScanMode(ScanMode.ALLERGEN) },
                    text = {
                        Text(
                            text = "Ingredients",
                            fontWeight = if (state.currentScanMode == ScanMode.ALLERGEN) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.currentScanMode == ScanMode.ALLERGEN) Color(0xFF062207) else Color(0xFFA3A4A3)
                        )
                    }
                )
                Tab(
                    selected = state.currentScanMode == ScanMode.BARCODE,
                    onClick = { viewModel.setScanMode(ScanMode.BARCODE) },
                    text = {
                        Text(
                            text = "Barcode",
                            fontWeight = if (state.currentScanMode == ScanMode.BARCODE) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.currentScanMode == ScanMode.BARCODE) Color(0xFF062207) else Color(0xFFA3A4A3)
                        )
                    }
                )
            }


            // Camera content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Camera preview with real-time analysis
                if (hasCameraPermission && state.isScanning) {
                    RealtimeCameraPreview(
                        textRecognizer = textRecognizer,
                        barcodeScanner = barcodeScanner,
                        cameraExecutor = cameraExecutor,
                        isPaused = state.temporaryPauseScan,
                        currentScanMode = state.currentScanMode,
                        onTextDetected = { text ->
                            if (text.isNotEmpty() && !state.isProcessing && !state.temporaryPauseScan) {
                                // Process detected text with API only if not paused
                                viewModel.detectAllergens(text)
                            }
                        },
                        onBarcodeDetected = { barcode ->
                            if (barcode.isNotEmpty() && !state.isProcessing && !state.temporaryPauseScan) {
                                // Process detected barcode if not paused
                                viewModel.scanProductBarcode(barcode)
                            }
                        }
                    )

                    // Mode info overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0x80000000)
                            ),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = when (state.currentScanMode) {
                                    ScanMode.ALLERGEN -> "Scanning for allergens..."
                                    ScanMode.BARCODE -> "Scanning for product barcodes..."
                                },
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Barcode frame guide when in barcode mode
                    if (state.currentScanMode == ScanMode.BARCODE && !state.temporaryPauseScan) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(250.dp, 150.dp)
                                    .background(Color(0x00000000))
                                    .padding(4.dp)
                            ) {
                                // Top-left corner
                                Box(
                                    modifier = Modifier
                                        .size(30.dp, 3.dp)
                                        .background(Color(0xFFC7F131))
                                        .align(Alignment.TopStart)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(3.dp, 30.dp)
                                        .background(Color(0xFFC7F131))
                                        .align(Alignment.TopStart)
                                )

                                // Top-right corner
                                Box(
                                    modifier = Modifier
                                        .size(30.dp, 3.dp)
                                        .background(Color(0xFFC7F131))
                                        .align(Alignment.TopEnd)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(3.dp, 30.dp)
                                        .background(Color(0xFFC7F131))
                                        .align(Alignment.TopEnd)
                                )

                                // Bottom-left corner
                                Box(
                                    modifier = Modifier
                                        .size(30.dp, 3.dp)
                                        .background(Color(0xFFC7F131))
                                        .align(Alignment.BottomStart)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(3.dp, 30.dp)
                                        .background(Color(0xFFC7F131))
                                        .align(Alignment.BottomStart)
                                )

                                // Bottom-right corner
                                Box(
                                    modifier = Modifier
                                        .size(30.dp, 3.dp)
                                        .background(Color(0xFFC7F131))
                                        .align(Alignment.BottomEnd)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(3.dp, 30.dp)
                                        .background(Color(0xFFC7F131))
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }
                    }
                } else if (!hasCameraPermission) {
                    // Permission not granted - show a message
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

                // Loading indicator during processing
                if (state.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x40000000)), // Semi-transparent overlay
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFC7F131),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                // Allergen alert popup
                if (state.showAllergenAlert) {
                    AllergenAlertDialog(
                        state = state,
                        onDismiss = { viewModel.dismissAllergenAlert() }
                    )
                }

                // Safe product alert popup (produk aman)
                if (state.showSafeProductAlert) {
                    SafeProductDialog(
                        onDismiss = { viewModel.dismissSafeProductAlert() }
                    )
                }

                // Product found dialog
                if (state.showProductFoundDialog) {
                    ProductFoundDialog(
                        state = state,
                        onDismiss = { viewModel.dismissProductFoundDialog() }
                    )
                }

                // Product not found dialog
                if (state.showProductNotFoundDialog) {
                    ProductNotFoundDialog(
                        onDismiss = { viewModel.dismissProductNotFoundDialog() }
                    )
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun RealtimeCameraPreview(
    textRecognizer: TextRecognizer,
    barcodeScanner: BarcodeScanner,
    cameraExecutor: ExecutorService,
    isPaused: Boolean,
    currentScanMode: ScanMode,
    onTextDetected: (String) -> Unit,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create throttling mechanisms to not overwhelm the system
    val isAnalyzing = remember { kotlinx.coroutines.flow.MutableStateFlow(false) }
    val lastProcessingTimestamp = remember { kotlinx.coroutines.flow.MutableStateFlow(0L) }
    val processingThrottleMs = 3000L

    // Create PreviewView
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    // Handle camera setup
    DisposableEffect(lifecycleOwner, currentScanMode) {
        Log.d("CameraPreview", "Setting up camera with real-time analysis for mode: $currentScanMode")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                Log.d("CameraPreview", "Camera provider obtained")

                // Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Image analysis use case
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                // Set up image analyzer based on current scanning mode
                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    // Skip frame analysis if paused
                    if (isPaused) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    // Check if we should process this frame
                    val currentTime = System.currentTimeMillis()
                    if (!isAnalyzing.value && (currentTime - lastProcessingTimestamp.value > processingThrottleMs)) {
                        isAnalyzing.value = true
                        lastProcessingTimestamp.value = currentTime

                        try {
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                when (currentScanMode) {
                                    ScanMode.ALLERGEN -> {
                                        // In allergen mode, use OCR
                                        textRecognizer.process(image)
                                            .addOnSuccessListener { visionText ->
                                                val detectedText = visionText.text
                                                if (detectedText.isNotEmpty()) {
                                                    Log.d("ImageAnalyzer", "Detected text: ${detectedText.take(50)}...")
                                                    onTextDetected(detectedText)
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("ImageAnalyzer", "Text detection failed", e)
                                            }
                                            .addOnCompleteListener {
                                                // Release analyzing flag and close image proxy
                                                isAnalyzing.value = false
                                                imageProxy.close()
                                            }
                                    }
                                    ScanMode.BARCODE -> {
                                        Log.d("BarcodeAnalyzer", "Processing image for barcode...")
                                        // In barcode mode, use barcode scanner
                                        barcodeScanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                if (barcodes.isNotEmpty()) {
                                                    // Found a barcode, use the first valid one
                                                    val barcode = barcodes.firstOrNull { it.rawValue != null }
                                                    if (barcode != null) {
                                                        Log.d("BarcodeAnalyzer", "Detected barcode: ${barcode.rawValue}")
                                                        barcode.rawValue?.let { onBarcodeDetected(it) }
                                                    }
                                                }
                                                isAnalyzing.value = false
                                                imageProxy.close()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("BarcodeAnalyzer", "Barcode detection failed", e)
                                                isAnalyzing.value = false
                                                imageProxy.close()
                                            }
                                    }
                                }
                            } else {
                                isAnalyzing.value = false
                                imageProxy.close()
                            }
                        } catch (e: Exception) {
                            Log.e("ImageAnalyzer", "Error processing image", e)
                            isAnalyzing.value = false
                            imageProxy.close()
                        }
                    } else {
                        // Skip this frame, just close the image proxy
                        imageProxy.close()
                    }
                }

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera - use back camera for barcode, might use front for allergen
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                Log.d("CameraPreview", "Camera bound successfully with mode: $currentScanMode")

            } catch (exc: Exception) {
                Log.e("CameraPreview", "Camera setup failed", exc)
            }
        }, executor)

        onDispose {
            Log.d("CameraPreview", "Disposing camera")
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ProductFoundDialog(
    state: ScanState,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Semi-transparent background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable { onDismiss() }
        )

        // Card dialog from bottom
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Close button at top-right
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(42.dp)
                            .align(Alignment.TopStart)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }

                // Product name - check if product has a name field or use a fallback
                Text(
                    text = when {
                        state.product?.productName != null -> state.product.productName
                        else -> "Produk"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Left
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Barcode
                Text(
                    text = state.scannedBarcode ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Left,
                    color = Color(0xFF5D6AE9)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Product info message
                Text(
                    text = "Produk ditemukan, lihat komposisi dan informasi terkait nutrisi.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Details button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Navigate to product details */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Details",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View Details",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Show allergen information if available
                if (state.hasAllergens && state.detectedAllergens.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Alergen Terdeteksi:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                    ) {
                        items(state.detectedAllergens) { allergen ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Bullet point
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black,
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                // Allergen name
                                Text(
                                    text = allergen.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Black
                                )

                                // Severity indicator if available
                                if (allergen.severityLevel > 0) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    SeverityIndicator(severityLevel = allergen.severityLevel)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProductNotFoundDialog(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Semi-transparent background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable { onDismiss() }
        )

        // Card dialog from bottom
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    // Gambar dari drawable di kiri atas
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(42.dp)
                            .align(Alignment.TopStart)
                    )

                    // Tombol Close di kanan atas
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }

                // Title
                Text(
                    text = "Produk tidak ditemukan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Left
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                Text(
                    text = "Maaf, produk yang Anda pindai tidak ditemukan.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AllergenAlertDialog(
    state: ScanState,
    onDismiss: () -> Unit
) {
    // Dialog custom yang muncul dari bawah layar
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Latar belakang semi-transparan untuk keseluruhan layar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable { onDismiss() }
        )

        // Card alert yang muncul dari bawah
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button at top-right
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }

                // Title
                Text(
                    text = "Whoops!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Produk ini mengandung bahan yang dapat memicu alergi!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Show allergens in different formats based on how many there are
                when {
                    // Few allergens - show them in a row
                    state.detectedAllergens.size <= 3 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            state.detectedAllergens.forEach { allergen ->
                                AllergenChip(allergenName = allergen.name)
                                if (state.detectedAllergens.last() != allergen) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                        }
                    }

                    // Many allergens - show them in a scrollable list
                    else -> {
                        Text(
                            text = "Daftar Alergen Terdeteksi:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(state.detectedAllergens) { allergen ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Bullet point
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Black,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )

                                    // Allergen name
                                    Text(
                                        text = allergen.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Black
                                    )

                                    // Severity indicator if available
                                    if (allergen.severityLevel > 0) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        SeverityIndicator(severityLevel = allergen.severityLevel)
                                    }
                                }
                            }
                        }
                    }
                }

                // Extra space at the bottom
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AllergenChip(allergenName: String) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF0F0F0),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = allergenName,
            color = Color(0xFF5B6EE1),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun SafeProductDialog(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable { onDismiss() }
        )

        // Card alert yang muncul dari bawah
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Yuhuuu!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Produk ini tidak mengandung bahan yang dapat memicu alergi berdasarkan informasi yang tersedia.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Disclaimer atau info tambahan
                Text(
                    text = "*Silahkan tetap periksa label produk untuk memastikan keamanan.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Extra space at the bottom
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SeverityIndicator(severityLevel: Int) {
    val color = when(severityLevel) {
        1 -> Color(0xFFFFCC00) // Low - Yellow
        2 -> Color(0xFFFF9900) // Medium - Orange
        3 -> Color(0xFFFF0000) // High - Red
        else -> Color(0xFFCCCCCC) // Unknown - Gray
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Tingkat:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(end = 4.dp)
        )

        // Small colored circles to indicate severity
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(horizontal = 1.dp)
                    .background(
                        color = if (index < severityLevel) color else Color(0xFFEEEEEE),
                        shape = CircleShape
                    )
            )
        }
    }
}