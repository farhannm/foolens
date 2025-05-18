package com.proyek.foolens.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.domain.model.ScanHistory
import com.proyek.foolens.ui.component.ConfirmationDialog
import com.proyek.foolens.ui.theme.Typography
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ScanHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // Menampilkan Snackbar untuk error atau penghapusan
    LaunchedEffect(state.error, showDeleteDialog) {
        when {
            state.error != null -> {
                snackbarHostState.showSnackbar(state.error!!)
            }
            showDeleteDialog == null && state.deleteSuccess -> {
                snackbarHostState.showSnackbar("Riwayat dihapus")
                viewModel.resetDeleteSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
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
            } else if (state.error != null) {
                Text(
                    text = "Error: ${state.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.scanHistories.isEmpty()) {
                Text(
                    text = "Belum ada riwayat pemindaian",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(state.scanHistories) { scanHistory ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HistoryScan(
                                name = scanHistory.product?.productName ?: "Produk Tidak Dikenal",
                                allergens = scanHistory.unsafeAllergens?.joinToString() ?: "Tidak Ada",
                                onClick = { onNavigateToDetail(scanHistory.id) },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { showDeleteDialog = scanHistory.id }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog konfirmasi penghapusan
    if (showDeleteDialog != null) {
        ConfirmationDialog(
            title = "Hapus Riwayat",
            message = "Apakah Anda yakin ingin menghapus riwayat ini?",
            icon = painterResource(id = R.drawable.ilustration_sticker),
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteScan(showDeleteDialog!!)
                showDeleteDialog = null
            }
        )
    }
}

@Composable
fun HistoryScan(
    name: String,
    allergens: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )

                Text(
                    text = "Risky Ingredients",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )

                Text(
                    text = allergens,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = Color.Blue
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                tint = Color.Black
            )
        }
    }
}