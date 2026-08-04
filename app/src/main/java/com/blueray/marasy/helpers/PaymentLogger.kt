package com.blueray.marasy.helpers

import android.util.Log

object PaymentLogger {
    const val TAG = "MarasyPayment*******"

    fun d(step: String, message: String) {
        Log.d(TAG, "[$step] $message")
    }

    fun w(step: String, message: String) {
        Log.w(TAG, "[$step] $message")
    }

    fun e(step: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, "[$step] $message", throwable)
        } else {
            Log.e(TAG, "[$step] $message")
        }
    }

    fun logGatewayResponse(step: String, httpCode: Int, body: String?) {
        Log.d(TAG, "[$step] http=$httpCode body=${body ?: "null"}")
    }
}
