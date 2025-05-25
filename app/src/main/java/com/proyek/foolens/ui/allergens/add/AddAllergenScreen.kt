package com.proyek.foolens.ui.allergens.add

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.domain.model.Allergen
import com.proyek.foolens.ui.component.SeveritySelectionBottomSheet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAllergenScreen(
    viewModel: AddAllergenViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showAllergenDialog by remember { mutableStateOf(false) }
    var showSeverityDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Handle navigation on success
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSuccess()
        }
    }

    // Tampilkan Toast jika ada error
    LaunchedEffect(state.error) {
        state.error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Alergen") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Allergen label
                Text(
                    text = "Alergen",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Allergen selection - diubah untuk single select
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.1f))
                        .clickable { showAllergenDialog = true }
                        .padding(16.dp)
                ) {
                    if (state.selectedAllergen == null) {
                        Text(
                            text = "Pilih Alergen",
                            color = Color.Gray
                        )
                    } else {
                        // Tampilkan alergen yang dipilih
                        Column {
                            Text(
                                text = state.selectedAllergen!!.name,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = "Tingkat Keparahan (1-5)",
//                    fontSize = 12.sp,
//                    color = Color.Gray,
//                    modifier = Modifier.padding(bottom = 8.dp)
//                )
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color.LightGray.copy(alpha = 0.1f))
//                        .clickable { showSeverityDialog = true }
//                        .padding(16.dp)
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = state.severityLevel.toString(),
//                            color = Color.Black,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        // Indikator severity tetap sama
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            for (i in 1..5) {
//                                Box(
//                                    modifier = Modifier
//                                        .padding(end = 4.dp)
//                                        .size(12.dp)
//                                        .background(
//                                            color = if (i <= state.severityLevel) {
//                                                when (state.severityLevel) {
//                                                    1 -> Color(0xFF9DE24F) // Green
//                                                    2 -> Color(0xFFCEF33F) // Light Green
//                                                    3 -> Color(0xFFF3E03F) // Yellow
//                                                    4 -> Color(0xFFF3943F) // Orange
//                                                    else -> Color(0xFFF33F3F) // Red
//                                                }
//                                            } else {
//                                                Color.LightGray
//                                            },
//                                            shape = CircleShape
//                                        )
//                                )
//                            }
//                        }
//                    }
//                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Catatan (opsional)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextField(
                    value = state.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Catatan terkait alergi...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.LightGray.copy(alpha = 0.1f),
                        focusedContainerColor = Color.LightGray.copy(alpha = 0.1f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Error message - menggunakan Snackbar jika ada error
                // Di dalam fungsi AddAllergenScreen, tambahkan:
                val context = LocalContext.current

                // Tambahkan LaunchedEffect untuk menampilkan Toast
                LaunchedEffect(state.error) {
                    state.error?.let { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save button
                Button(
                    onClick = { viewModel.saveAllergen() }, // Diubah menjadi saveAllergen
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.selectedAllergen != null && !state.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCEEB44)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Simpan",
                            color = Color.Black,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }
            }

            // Overlay untuk bottom sheets tetap sama
            if (showAllergenDialog || showSeverityDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            showAllergenDialog = false
                            showSeverityDialog = false
                        }
                )
            }

            // Allergen Selection Bottom Sheet - diubah untuk single select
            if (showAllergenDialog) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AllergenSelectionBottomSheet(
                        allergens = state.availableAllergens,
                        selectedAllergenId = state.selectedAllergen?.id,
                        onDismiss = { showAllergenDialog = false },
                        onAllergenSelected = { allergen ->
                            viewModel.setSelectedAllergen(allergen)
                            showAllergenDialog = false // Tutup sheet setelah memilih
                        },
                        onClearSelection = {
                            viewModel.clearSelectedAllergen()
                        },
                        onSearch = { query ->
                            viewModel.searchAllergens(query)
                        }
                    )
                }
            }

            // Severity Selection Bottom Sheet tetap sama
            if (showSeverityDialog) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    SeveritySelectionBottomSheet(
                        currentSeverity = state.severityLevel,
                        onDismiss = { showSeverityDialog = false },
                        onSeveritySelected = { severity ->
                            viewModel.updateSeverityLevel(severity)
                            showSeverityDialog = false
                        }
                    )
                }
            }

            // Loading overlay tetap sama
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AllergenSelectionBottomSheet(
    allergens: List<Allergen>,
    selectedAllergenId: Int?, // Berubah dari Set menjadi single ID
    onDismiss: () -> Unit,
    onAllergenSelected: (Allergen) -> Unit, // Diubah untuk single selection
    onClearSelection: () -> Unit, // Diganti dari clearAll
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Debounce functionality tetap sama
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val processSearch = { query: String ->
        searchJob?.cancel()
        searchQuery = query
        searchJob = scope.launch {
            delay(300)  // 300ms debounce delay
            onSearch(query)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Close button tetap sama
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
                text = "Pilih Alergen",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search field tetap sama
            TextField(
                value = searchQuery,
                onValueChange = { processSearch(it) },
                placeholder = { Text("Cari alergen...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Gray,
                    unfocusedIndicatorColor = Color.Gray
                ),
                singleLine = true
            )

            // Loading indicator sama
            if (allergens.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "Tidak ditemukan alergen",
                            color = Color.Gray
                        )
                    } else {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else {
                // List allergen dengan RadioButton untuk single select
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(allergens) { allergen ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Hanya set allergen baru
                                    onAllergenSelected(allergen)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ganti Checkbox dengan RadioButton
                            RadioButton(
                                selected = allergen.id == selectedAllergenId,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.Black
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = allergen.name)
                        }
                    }
                }
            }

            // Clear selection button
            if (selectedAllergenId != null) {
                Text(
                    text = "Hapus Pilihan",
                    color = Color.Blue,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { onClearSelection() }
                )
            }
        }
    }
}