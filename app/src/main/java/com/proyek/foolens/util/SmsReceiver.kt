package com.proyek.foolens.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import timber.log.Timber

class SmsReceiver : BroadcastReceiver() {

    interface SmsReceivedListener {
        fun onSmsReceived(sms: String)
        fun onTimeout()
    }

    companion object {
        var listener: SmsReceivedListener? = null
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called with action: ${intent.action}")
        Timber.d("SmsReceiver: Received intent with action: ${intent.action}")

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            Log.d(TAG, "Intent extras: $extras")
            Timber.d("SmsReceiver: Extras: $extras")

            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
            if (status == null) {
                Log.e(TAG, "Status is null")
                return
            }

            Log.d(TAG, "Status code: ${status.statusCode}, message: ${status.statusMessage}")
            Timber.d("SmsReceiver: Status code: ${status.statusCode}, message: ${status.statusMessage}")

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    Log.d(TAG, "SMS received: $message")
                    Timber.d("SmsReceiver: Full SMS: $message")

                    message?.let { sms ->
                        // Pattern yang lebih fleksibel untuk menangkap OTP
                        val otpPatterns = listOf(
                            Regex("\\b\\d{6}\\b"),                    // 6 digit angka
                            Regex("(?:code|kode|otp)\\s*:?\\s*(\\d{4,8})"),  // dengan kata kunci
                            Regex("(\\d{4,8})\\s*(?:adalah|is)"),    // format lain
                            Regex("verification\\s*:?\\s*(\\d{4,8})", RegexOption.IGNORE_CASE)
                        )

                        var otp: String? = null
                        for (pattern in otpPatterns) {
                            val match = pattern.find(sms)
                            if (match != null) {
                                otp = if (match.groupValues.size > 1) {
                                    match.groupValues[1]
                                } else {
                                    match.value
                                }
                                break
                            }
                        }

                        if (otp != null && otp.length in 4..8) {
                            Log.d(TAG, "OTP extracted: $otp")
                            Timber.d("SmsReceiver: OTP extracted: $otp")
                            listener?.onSmsReceived(otp)
                        } else {
                            Log.w(TAG, "Could not extract valid OTP from SMS: $sms")
                            Timber.w("SmsReceiver: Could not extract OTP from SMS: $sms")
                            // Tetap kirim SMS lengkap jika OTP tidak bisa diekstrak
                            listener?.onSmsReceived(sms)
                        }
                    } ?: run {
                        Log.e(TAG, "SMS message is null")
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    Log.w(TAG, "SMS retrieval timeout")
                    Timber.w("SmsReceiver: SMS retrieval timeout")
                    listener?.onTimeout()
                }
                else -> {
                    Log.e(TAG, "SMS retrieval failed with status: ${status.statusCode}, message: ${status.statusMessage}")
                    Timber.e("SmsReceiver: SMS retrieval failed with status: ${status.statusCode}, message: ${status.statusMessage}")
                }
            }
        } else {
            Log.d(TAG, "Received intent with different action: ${intent.action}")
        }
    }
}