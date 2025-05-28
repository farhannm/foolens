package com.proyek.foolens.ui.auth.password

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.proyek.foolens.domain.repository.ProfileRepository
import com.proyek.foolens.data.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _state = MutableStateFlow(ChangePasswordState())
    val state: StateFlow<ChangePasswordState> = _state.asStateFlow()

    // Mulai verifikasi nomor telepon dengan perbaikan format
    fun startPhoneVerification(phoneNumber: String, activity: Activity) {
        Timber.d("startPhoneVerification: phoneNumber=%s", phoneNumber)
        val formattedPhoneNumber = formatPhoneNumber(phoneNumber)
        Timber.d("Formatted phone number: %s", formattedPhoneNumber)

        if (!validatePhoneNumber(formattedPhoneNumber)) {
            Timber.e("Phone number validation failed: %s", _state.value.validationErrors)
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null, phoneNumber = formattedPhoneNumber) }
        Timber.d("State updated: isLoading=true, phoneNumber=%s", formattedPhoneNumber)

        Log.d("ChangePasswordViewModel", "Starting verification for: $formattedPhoneNumber")

        // Gunakan PhoneAuthOptions untuk konfigurasi yang lebih lengkap
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhoneNumber)
            .setTimeout(120L, TimeUnit.SECONDS) // Perpanjang timeout menjadi 2 menit
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Timber.d("onVerificationCompleted: credential=%s", credential.smsCode)
                    Log.d("ChangePasswordViewModel", "Verification completed: smsCode=${credential.smsCode}")
                    viewModelScope.launch {
                        signInWithCredential(credential)
                    }
                }

                override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                    Timber.e(exception, "Verification failed: %s", exception.message)
                    Log.e("ChangePasswordViewModel", "Verification failed: ${exception.message}", exception)

                    val errorMessage = when {
                        exception.message?.contains("invalid phone number", ignoreCase = true) == true ->
                            "Nomor telepon tidak valid. Pastikan format nomor benar."
                        exception.message?.contains("quota", ignoreCase = true) == true ->
                            "Batas pengiriman SMS telah tercapai. Coba lagi nanti."
                        exception.message?.contains("network", ignoreCase = true) == true ->
                            "Masalah koneksi jaringan. Periksa koneksi internet Anda."
                        else -> "Gagal mengirim kode verifikasi: ${exception.message}"
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMessage
                        )
                    }
                    Timber.d("State updated: isLoading=false, errorMessage=%s", _state.value.errorMessage)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Timber.d("onCodeSent: verificationId=%s, token=%s", verificationId, token)
                    Log.d("ChangePasswordViewModel", "Code sent: verificationId=$verificationId")
                    storedVerificationId = verificationId
                    resendToken = token
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isCodeSent = true,
                            phoneNumber = formattedPhoneNumber,
                            errorMessage = null,
                            lastResetAttempt = Date()
                        )
                    }
                    Timber.d("State updated: isCodeSent=true, phoneNumber=%s", formattedPhoneNumber)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun updateState(transform: (ChangePasswordState) -> ChangePasswordState) {
        _state.update(transform)
    }

    // Kirim ulang kode verifikasi dengan perbaikan
    fun resendVerificationCode(activity: Activity) {
        val phoneNumber = _state.value.phoneNumber
        Timber.d("resendVerificationCode: phoneNumber=%s, resendToken=%s", phoneNumber, resendToken)
        Log.d("ChangePasswordViewModel", "Resending code for: $phoneNumber, resendToken=$resendToken")

        if (phoneNumber.isEmpty() || resendToken == null) {
            _state.update { it.copy(errorMessage = "Tidak dapat mengirim ulang kode. Silakan mulai dari awal.") }
            Timber.e("Resend failed: phoneNumber or resendToken is null")
            Log.e("ChangePasswordViewModel", "Resend failed: phoneNumber=$phoneNumber, resendToken=$resendToken")
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        Timber.d("State updated: isLoading=true")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setForceResendingToken(resendToken!!)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Timber.d("Resend: onVerificationCompleted: credential=%s", credential.smsCode)
                    Log.d("ChangePasswordViewModel", "Resend: Verification completed: smsCode=${credential.smsCode}")
                    viewModelScope.launch {
                        signInWithCredential(credential)
                    }
                }

                override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                    Timber.e(exception, "Resend: Verification failed: %s", exception.message)
                    Log.e("ChangePasswordViewModel", "Resend: Verification failed: ${exception.message}", exception)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Gagal mengirim ulang kode: ${exception.message}"
                        )
                    }
                    Timber.d("State updated: isLoading=false, errorMessage=%s", _state.value.errorMessage)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Timber.d("Resend: onCodeSent: verificationId=%s, token=%s", verificationId, token)
                    Log.d("ChangePasswordViewModel", "Resend: Code sent: verificationId=$verificationId")
                    storedVerificationId = verificationId
                    resendToken = token
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isCodeSent = true,
                            errorMessage = null,
                            lastResetAttempt = Date()
                        )
                    }
                    Timber.d("State updated: isCodeSent=true")
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Verifikasi kode SMS yang dimasukkan pengguna
    fun verifyCode(phoneNumber: String, code: String) {
        Timber.d("verifyCode: phoneNumber=%s, code=%s", phoneNumber, code)
        Log.d("ChangePasswordViewModel", "Verifying code: phoneNumber=$phoneNumber, code=$code")

        if (!validateVerificationCode(code)) {
            Timber.e("Verification code validation failed: %s", _state.value.validationErrors)
            Log.e("ChangePasswordViewModel", "Verification code validation failed: ${_state.value.validationErrors}")
            return
        }

        val verificationId = storedVerificationId ?: run {
            _state.update { it.copy(errorMessage = "Verifikasi tidak valid. Silakan mulai dari awal.") }
            Timber.e("Verification failed: storedVerificationId is null")
            Log.e("ChangePasswordViewModel", "Verification failed: storedVerificationId is null")
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        _state.update { it.copy(isLoading = true) }
        Timber.d("State updated: isLoading=true for code verification")
        Log.d("ChangePasswordViewModel", "State updated: isLoading=true for code verification")

        viewModelScope.launch {
            signInWithCredential(credential)
        }
    }

    // Ubah kata sandi menggunakan API backend
    fun changePassword(phoneNumber: String, resetToken: String, newPassword: String, confirmPassword: String) {
        Timber.d("changePassword: phoneNumber=%s, resetToken=%s, newPasswordLength=%d", phoneNumber, resetToken, newPassword.length)
        Log.d("ChangePasswordViewModel", "Changing password: phoneNumber=$phoneNumber, resetToken=$resetToken, newPasswordLength=${newPassword.length}")

        if (!validatePasswords(newPassword, confirmPassword)) {
            Timber.e("Password validation failed: %s", _state.value.validationErrors)
            Log.e("ChangePasswordViewModel", "Password validation failed: ${_state.value.validationErrors}")
            return
        }

        if (!_state.value.isResetTokenValid()) {
            _state.update { it.copy(errorMessage = "Token telah kedaluwarsa. Silakan mulai dari awal.") }
            Timber.e("Reset token invalid or expired")
            Log.e("ChangePasswordViewModel", "Reset token invalid or expired")
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null, newPassword = newPassword) }
        Timber.d("State updated: isLoading=true, newPasswordLength=%d", newPassword.length)
        Log.d("ChangePasswordViewModel", "State updated: isLoading=true, newPasswordLength=${newPassword.length}")

        // Validasi resetToken
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        Timber.d("Validating resetToken: provided=%s, currentUserUid=%s", resetToken, currentUserUid)
        Log.d("ChangePasswordViewModel", "Validating resetToken: provided=$resetToken, currentUserUid=$currentUserUid")
        if (resetToken != currentUserUid) {
            _state.update { it.copy(isLoading = false, errorMessage = "Token reset tidak valid.") }
            Timber.e("Invalid reset token: provided=%s, expected=%s", resetToken, currentUserUid)
            Log.e("ChangePasswordViewModel", "Invalid reset token: provided=$resetToken, expected=$currentUserUid")
            return
        }

        viewModelScope.launch {
            Timber.d("Calling profileRepository.changePassword")
            Log.d("ChangePasswordViewModel", "Calling profileRepository.changePassword")
            profileRepository.changePassword(resetToken, "", newPassword).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isPasswordChanged = true,
                                errorMessage = null
                            )
                        }
                        Timber.d("Password change successful")
                        Log.d("ChangePasswordViewModel", "Password change successful")
                    }
                    is NetworkResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.errorMessage,
                                isPasswordChanged = false
                            )
                        }
                        Timber.e("Password change failed: %s", result.errorMessage)
                        Log.e("ChangePasswordViewModel", "Password change failed: ${result.errorMessage}")
                    }
                    is NetworkResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                        Timber.d("Password change in progress")
                        Log.d("ChangePasswordViewModel", "Password change in progress")
                    }
                }
            }
        }
    }

    // Helper function untuk sign in dengan kredensial Firebase
    private suspend fun signInWithCredential(credential: PhoneAuthCredential) {
        Timber.d("signInWithCredential: smsCode=%s", credential.smsCode)
        Log.d("ChangePasswordViewModel", "signInWithCredential: smsCode=${credential.smsCode}")
        try {
            val result = auth.signInWithCredential(credential).await()
            _state.update {
                it.copy(
                    isLoading = false,
                    isVerified = true,
                    resetToken = result.user?.uid, // Gunakan UID sebagai resetToken
                    errorMessage = null,
                    lastResetAttempt = Date()
                )
            }
            Timber.d("Sign-in successful: userUid=%s", result.user?.uid)
            Log.d("ChangePasswordViewModel", "Sign-in successful: userUid=${result.user?.uid}")
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Verifikasi gagal: ${e.message}",
                    isVerified = false,
                    verificationCode = ""
                )
            }
            Timber.e(e, "Sign-in failed: %s", e.message)
            Log.e("ChangePasswordViewModel", "Sign-in failed: ${e.message}", e)
        }
    }

    // PERBAIKAN: Fungsi untuk memformat nomor telepon Indonesia
    private fun formatPhoneNumber(phoneNumber: String): String {
        var cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "") // Hapus karakter non-digit kecuali +

        when {
            cleanNumber.startsWith("+62") -> {
                // Sudah benar, tidak perlu diubah
                return cleanNumber
            }
            cleanNumber.startsWith("62") -> {
                // Tambahkan + di depan
                return "+$cleanNumber"
            }
            cleanNumber.startsWith("08") -> {
                // Nomor Indonesia yang dimulai dengan 08, ganti dengan +628
                return "+62${cleanNumber.substring(1)}"
            }
            cleanNumber.startsWith("8") -> {
                // Nomor yang dimulai dengan 8 tanpa 0
                return "+62$cleanNumber"
            }
            cleanNumber.startsWith("0") -> {
                // Nomor lain yang dimulai dengan 0
                return "+62${cleanNumber.substring(1)}"
            }
            else -> {
                // Default: tambahkan +62
                return "+62$cleanNumber"
            }
        }
    }

    // Fungsi validasi yang diperbaiki
    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        val errors = mutableMapOf<ChangePasswordState.Field, String>()

        when {
            phoneNumber.isEmpty() -> {
                errors[ChangePasswordState.Field.PHONE] = "Nomor telepon tidak boleh kosong"
            }
            !phoneNumber.startsWith("+62") -> {
                errors[ChangePasswordState.Field.PHONE] = "Nomor telepon harus dimulai dengan +62"
            }
            phoneNumber.length < 10 || phoneNumber.length > 16 -> {
                errors[ChangePasswordState.Field.PHONE] = "Panjang nomor telepon tidak valid"
            }
            !phoneNumber.substring(3).all { it.isDigit() } -> {
                errors[ChangePasswordState.Field.PHONE] = "Nomor telepon hanya boleh berisi angka setelah +62"
            }
        }

        return if (errors.isEmpty()) {
            _state.update { it.copy(validationErrors = emptyMap()) }
            Timber.d("Phone number validation passed: %s", phoneNumber)
            Log.d("ChangePasswordViewModel", "Phone number validation passed: $phoneNumber")
            true
        } else {
            _state.update { it.copy(validationErrors = errors) }
            Timber.e("Phone number validation failed: %s", errors)
            Log.e("ChangePasswordViewModel", "Phone number validation failed: $errors")
            false
        }
    }

    private fun validateVerificationCode(code: String): Boolean {
        val errors = mutableMapOf<ChangePasswordState.Field, String>()
        if (code.length != 6) {
            errors[ChangePasswordState.Field.VERIFICATION_CODE] = "Kode harus 6 digit"
        } else if (!code.all { it.isDigit() }) {
            errors[ChangePasswordState.Field.VERIFICATION_CODE] = "Kode harus berupa angka"
        }

        return if (errors.isEmpty()) {
            _state.update { it.copy(validationErrors = emptyMap()) }
            Timber.d("Verification code validation passed: %s", code)
            Log.d("ChangePasswordViewModel", "Verification code validation passed: $code")
            true
        } else {
            _state.update { it.copy(validationErrors = errors) }
            Timber.e("Verification code validation failed: %s", errors)
            Log.e("ChangePasswordViewModel", "Verification code validation failed: $errors")
            false
        }
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        val errors = mutableMapOf<ChangePasswordState.Field, String>()
        if (newPassword.length < 8) {
            errors[ChangePasswordState.Field.NEW_PASSWORD] = "Kata sandi harus minimal 8 karakter"
        }
        if (newPassword != confirmPassword) {
            errors[ChangePasswordState.Field.CONFIRM_PASSWORD] = "Kata sandi tidak cocok"
        }

        return if (errors.isEmpty()) {
            _state.update { it.copy(validationErrors = emptyMap()) }
            Timber.d("Password validation passed")
            Log.d("ChangePasswordViewModel", "Password validation passed")
            true
        } else {
            _state.update { it.copy(validationErrors = errors) }
            Timber.e("Password validation failed: %s", errors)
            Log.e("ChangePasswordViewModel", "Password validation failed: $errors")
            false
        }
    }
}