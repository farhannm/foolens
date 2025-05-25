package com.proyek.foolens.ui.allergens.detail

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.R
import com.proyek.foolens.domain.model.UserAllergen
import com.proyek.foolens.ui.component.ConfirmationDialog
import com.proyek.foolens.ui.component.SeveritySelectionBottomSheet

private const val TAG = "AllergenDetailScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllergenDetailScreen(
    allergen: UserAllergen,
    viewModel: AllergenDetailViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showSeverityDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Initialize ViewModel with allergen
    LaunchedEffect(allergen) {
        Log.d(TAG, "Initializing ViewModel with allergen: ${allergen.name}")
        viewModel.initWithAllergen(allergen)
    }

    // Handle success cases
    LaunchedEffect(state.isUpdated, state.isDeleted) {
        if (state.isUpdated) {
            Log.d(TAG, "Allergen updated successfully")
            Toast.makeText(context, "Allergen updated successfully", Toast.LENGTH_SHORT).show()
            onSave()
        }
        if (state.isDeleted) {
            Log.d(TAG, "Allergen deleted successfully")
            Toast.makeText(context, "Allergen deleted successfully", Toast.LENGTH_SHORT).show()
            onDelete()
        }
    }

    // Handle error
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Log.e(TAG, "Error: $error")
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        ConfirmationDialog(
            title = "Delete Allergen",
            message = "Are you sure you want to delete this allergen? This action cannot be undone.",
            icon = painterResource(id = R.drawable.ilustration_sticker),
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                viewModel.deleteAllergen()
                showDeleteConfirmation = false
            },
            confirmText = "Delete",
            dismissText = "Cancel"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Allergen Detail") },
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
                // Allergen information
                Text(
                    text = "Allergen(s)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = allergen.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                if (allergen.description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = allergen.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Severity field styled like AddAllergenScreen
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
//                        // Severity indicator dots
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
//
//                Spacer(modifier = Modifier.height(16.dp))

                // Notes field styled like AddAllergenScreen
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

                Spacer(modifier = Modifier.weight(1f))

                // Buttons with updated style matching the screenshot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Save button - green with black text like in the screenshot
                    Button(
                        onClick = { viewModel.saveAllergenDetails() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCEEB44),
                            contentColor = Color.Black
                        ),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Gray
                        ),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Gray,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                    }
                }
            }

            // Severity Selection Bottom Sheet dengan animasi yang baru
            if (showSeverityDialog) {
                SeveritySelectionBottomSheet(
                    currentSeverity = state.severityLevel,
                    onDismiss = { showSeverityDialog = false },
                    onSeveritySelected = { severity ->
                        viewModel.updateSeverityLevel(severity)
                    }
                )
            }

            // Overlay loading indicator
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