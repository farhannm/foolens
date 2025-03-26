package com.proyek.foolens.ui.landing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class LandingViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LandingState())
    val state: StateFlow<LandingState> = _state.asStateFlow()

    // Untuk saat ini, landing screen sangat sederhana tanpa banyak logika,
    // tapi kita tetap mempertahankan struktur MVVM untuk konsistensi
    // dan mempermudah penambahan fitur di masa depan
}