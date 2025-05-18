package com.proyek.foolens.ui.history

import com.proyek.foolens.domain.model.ScanHistory

/**
 * State class untuk ScanHistoryScreen
 */
data class ScanHistoryState(
    val scanHistories: List<ScanHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)