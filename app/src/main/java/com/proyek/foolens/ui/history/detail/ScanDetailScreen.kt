package com.proyek.foolens.ui.history.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.ui.history.ScanHistoryViewModel

@Composable
fun ScanDetailScreen (
    scanId: String,
    onBack: () -> Unit,
    viewModel: ScanDetailViewModel = hiltViewModel()
){
    Text(
        text = "Detail untuk scan ID: $scanId (Belum diimplementasikan)"
    )
}