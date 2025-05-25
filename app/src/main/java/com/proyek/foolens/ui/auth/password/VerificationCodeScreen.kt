package com.proyek.foolens.ui.auth.password

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.proyek.foolens.ui.component.LoadingView
import com.proyek.foolens.ui.theme.OnBackground
import com.proyek.foolens.util.SmsReceiver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationCodeScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    onVerified: (String) -> Unit,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val codeLength = 6
    val digits = remember { mutableStateListOf(*Array(codeLength) { "" }) }
    val focusRequesters = remember { List(codeLength) { FocusRequester() } }

    // Combine digits into verification code
    val verificationCode = digits.joinToString("")

    // Autofocus field pertama saat screen ditampilkan
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    LaunchedEffect(Unit) {
        SmsReceiver.listener = object : SmsReceiver.SmsReceivedListener {
            override fun onSmsReceived(sms: String) {
                digits.forEachIndexed { index, _ ->
                    if (index < sms.length) {
                        digits[index] = sms[index].toString()
                    }
                }
                viewModel.verifyCode(phoneNumber, sms)
            }

            override fun onTimeout() {
                // Use the new updateState method
                viewModel.updateState { currentState ->
                    currentState.copy(errorMessage = "Timeout menunggu SMS. Silakan masukkan kode secara manual.")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            SmsReceiver.listener = null
        }
    }

    // Navigasi ke step berikutnya jika sudah verifikasi
    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            state.resetToken?.let { token ->
                onVerified(token)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "6-digit recovery",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = "code",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Kami mengirimkan 6 digit kode verifikasi ke nomor HP yang terdaftar pada akun.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackground,
                lineHeight = 20.sp,
                textAlign = TextAlign.Left
            )

            Spacer(modifier = Modifier.height(16.dp))
            if (state.hasError(ChangePasswordState.Field.VERIFICATION_CODE)) {
                Text(
                    text = state.getErrorFor(ChangePasswordState.Field.VERIFICATION_CODE) ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0 until codeLength) {
                    BasicTextField(
                        value = digits[i],
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                digits[i] = newValue
                                if (newValue.isNotEmpty() && i < codeLength - 1) {
                                    focusRequesters[i + 1].requestFocus()
                                }
                                if (newValue.isEmpty() && i > 0) {
                                    focusRequesters[i - 1].requestFocus()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .focusRequester(focusRequesters[i]),
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.Center) {
                                innerTextField()
                                if (digits[i].isEmpty()) {
                                    Text(
                                        text = "_",
                                        color = Color.Gray,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.verifyCode(phoneNumber, verificationCode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC7F131)
                ),
                enabled = !state.isLoading && verificationCode.length == codeLength
            ) {
                Text(
                    text = "Submit",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
            }
        }

        if (state.isLoading) {
            LoadingView()
        }
    }
}