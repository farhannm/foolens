package com.proyek.foolens.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.foolens.data.util.NetworkResult
import com.proyek.foolens.domain.usecases.ScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanHistoryViewModel @Inject constructor(
    private val scanHistoryUseCase: ScanHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScanHistoryState())
    val state: StateFlow<ScanHistoryState> = _state

    init {
        fetchScanHistory()
    }

    fun fetchScanHistory(page: Int = 1) {
        viewModelScope.launch {
            scanHistoryUseCase.getScanHistory(page = page).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is NetworkResult.Success -> {
                        _state.value = _state.value.copy(
                            scanHistories = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                    is NetworkResult.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.errorMessage ?: "Terjadi kesalahan"
                        )
                    }
                }
            }
        }
    }

    fun deleteScan(scanId: String) {
        viewModelScope.launch {
            scanHistoryUseCase.deleteScan(scanId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.value = _state.value.copy(deleteSuccess = true)
                        fetchScanHistory() // Refresh daftar
                    }
                    is NetworkResult.Error -> {
                        _state.value = _state.value.copy(
                            error = result.errorMessage ?: "Gagal menghapus riwayat"
                        )
                    }
                    is NetworkResult.Loading -> {} // Loading tidak perlu ditangani
                }
            }
        }
    }

    fun resetDeleteSuccess() {
        _state.value = _state.value.copy(deleteSuccess = false)
    }
}