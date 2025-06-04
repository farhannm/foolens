package com.proyek.foolens.ui.history.detail

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.proyek.foolens.R
import com.proyek.foolens.ui.component.ConfirmationDialog
import com.proyek.foolens.ui.scan.SeverityIndicator

@Composable
fun ScanDetailScreen(
    scanId: String,
    onBack: () -> Unit,
    viewModel: ScanDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(scanId) {
        Log.d("ScanDetailScreen", "Loading scan details for scanId: $scanId")
        viewModel.loadScanDetails(scanId)
    }

    LaunchedEffect(state.product?.imageUrl) {
        Log.d("ScanDetailScreen", "Current imageUrl: ${state.product?.imageUrl}")
    }

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            Log.d("ScanDetailScreen", "Scan deleted successfully, navigating back")
            onBack() // Navigasi kembali setelah penghapusan berhasil
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header: Tombol Kembali, Judul, dan Tombol Hapus
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Kembali",
                    tint = Color.Black
                )
            }

            Text(
                text = "Detail Scan",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = Color.Black
                )
            }
        }

        // Dialog Konfirmasi Hapus
        if (showDeleteDialog) {
            ConfirmationDialog(
                title = "Hapus Riwayat",
                message = "Apakah Anda yakin ingin menghapus riwayat ini?",
                icon = painterResource(id = R.drawable.ilustration_sticker),
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    viewModel.deleteScan(scanId)
                    showDeleteDialog = false
                }
            )
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFC7F131),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "Terjadi kesalahan",
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Gambar Produk
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        state.product?.imageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                            Log.d("ScanDetailScreen", "Attempting to load image: $imageUrl")
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Gambar Produk",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentScale = ContentScale.Fit,
                                placeholder = painterResource(R.drawable.logo),
                                error = painterResource(R.drawable.logo),
                                onError = { errorResult ->
                                    Log.e("ScanDetailScreen", "Failed to load image: $imageUrl, Error: ${errorResult.result.throwable?.message}")
                                },
                                onSuccess = {
                                    Log.d("ScanDetailScreen", "Image loaded successfully: $imageUrl")
                                }
                            )
                        } ?: Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Keamanan Produk
                    Text(
                        text = if (state.isSafe) "is Safe!" else "Whoops!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isSafe) Color.Black else Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (state.isSafe) {
                            "Produk aman dari komposisi yang memicu alergi, tetap cek kembali terkait komposisi pada kemasan."
                        } else {
                            "Produk ini mengandung bahan yang dapat memicu alergi."
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    @Composable
                    fun AllergenChip(allergenName: String, chipColor: Color) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = chipColor,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = allergenName,
                                color = Color(0xFF5B6EE1),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }

                    // Tampilkan alergen terdeteksi sebagai chip jika produk tidak aman
                    if (!state.isSafe && state.detectedAllergens.isNotEmpty()) {
                        Column(
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            state.detectedAllergens.forEachIndexed { index, allergen ->
                                AllergenChip(
                                    allergenName = allergen.name,
                                    chipColor = Color(0xFFF0F0F0)
                                )
                            }
                        }
                    }

                    Divider(
                        color = Color.Gray,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nama Produk
                    Text(
                        text = state.product?.productName ?: "Produk Tidak Diketahui",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    state.product?.brand?.let { brand ->
                        Text(
                            text = brand,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF5D6AE9),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barcode
                    Text(
                        text = "Barcode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${state.scannedBarcode ?: "Tidak tersedia"}",
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Komposisi
                    state.product?.ingredients?.let { ingredients ->
                        Text(
                            text = "Komposisi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = ingredients,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Informasi Nutrisi
                    Text(
                        text = "Informasi Nutrisi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${state.product?.nutritionalInfo?.carbs ?: 0} karbohidrat, " +
                                "${state.product?.nutritionalInfo?.protein ?: 0} protein, " +
                                "${state.product?.nutritionalInfo?.fat ?: 0} lemak, " +
                                "${state.product?.nutritionalInfo?.calories ?: 0} kalori",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daftar Alergen Tidak Aman
                    if (!state.isSafe && state.unsafeAllergens.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            state.unsafeAllergens.forEach { allergen ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = allergen,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Daftar Alergen Terdeteksi
                    if (!state.isSafe && state.detectedAllergens.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            state.detectedAllergens.forEach { allergen ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 14.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = allergen.name,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    if (allergen.severityLevel > 0) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        SeverityIndicator(severityLevel = allergen.severityLevel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}