package com.proyek.foolens.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Extension function untuk implementasi double back press to exit
 * @param context Konteks aplikasi
 * @param onFirstBackPress Callback saat back press pertama
 * @param onSecondBackPress Callback saat back press kedua (biasanya exit)
 */
fun Activity.onDoubleBackPressToExit(
    context: Context,
    onFirstBackPress: () -> Unit = {
        Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
    },
    onSecondBackPress: () -> Unit = { finish() }
) {
    var doubleBackToExitPressedOnce = false

    val backDispatcher = CoroutineScope(Dispatchers.Main)

    // Pastikan ini adalah FragmentActivity untuk menggunakan OnBackPressedCallback
    if (this is FragmentActivity) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    onSecondBackPress()
                    return
                }

                doubleBackToExitPressedOnce = true
                onFirstBackPress()

                backDispatcher.launch {
                    delay(2000)
                    doubleBackToExitPressedOnce = false
                }
            }
        }

        // Tambahkan callback ke OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }
}

/**
 * Composable untuk menangani double back press di Compose
 * @param onFirstBackPress Callback saat back press pertama
 * @param onSecondBackPress Callback saat back press kedua
 */
@Composable
fun DoubleBackPressHandler(
    onFirstBackPress: () -> Unit = {},
    onSecondBackPress: () -> Unit = {}
) {
    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }

    BackHandler {
        if (doubleBackToExitPressedOnce) {
            onSecondBackPress()
            return@BackHandler
        }

        doubleBackToExitPressedOnce = true
        onFirstBackPress()

        // Reset flag setelah 2 detik
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            doubleBackToExitPressedOnce = false
        }
    }
}