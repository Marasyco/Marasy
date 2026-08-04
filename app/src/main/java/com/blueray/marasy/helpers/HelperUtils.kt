package com.blueray.marasy.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.blueray.marasy.R
import es.dmoral.toasty.Toasty
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

object HelperUtils {

    const val SHARED_PREF = "MARASY_KEY"
    const val BASE_URL = "http://demo2.marasy.com.dedi8785.your-server.de"

    fun String.toStringRequestBody(): RequestBody {
        return toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun showToast(context: Context, message: String) {
        Toasty.success(context, message, Toast.LENGTH_SHORT, true).show()
    }

    fun showErrorToast(context: Context, message: String) {
        Toasty.error(context, message, Toast.LENGTH_SHORT, true).show()
    }

    fun getLang(mContext: Context?): String {
        val sharedPreferences = mContext?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        return sharedPreferences?.getString("lang", "en")!!
    }

    fun getUID(mContext: Context?): String {
        val sharedPreferences = mContext?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        return sharedPreferences?.getString("uid", "0")!!
    }

    fun getRole(mContext: Context?): String {
        val sharedPreferences = mContext?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        return sharedPreferences?.getString("role", "0")!!
    }

    fun isGuest(mContext: Context?): Boolean {
        return getUID(mContext) == "0"
    }

    fun showLoginRequiredDialog(activity: Activity, onLoginClick: () -> Unit) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_confirm_action, null)
        val dialog = android.app.AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val tvMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        tvMessage.text = activity.getString(R.string.you_need_to_log_in)
        btnConfirm.text = activity.getString(R.string.login)
        btnCancel.text = activity.getString(R.string.cancel)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            onLoginClick()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    fun saveUID(mContext: Context?, uid: String) {
        val sharedPreferences = mContext?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("uid", uid)
        editor?.apply()
    }

    fun saveRole(mContext: Context?, role: String) {
        val sharedPreferences = mContext?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("role", role)
        editor?.apply()
    }

    fun showConfirmDialog(
        activity: Activity,
        message: String,
        onConfirm: () -> Unit
    ) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_confirm_action, null)
        val dialog = android.app.AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val tvMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        tvMessage.text = message

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    fun showPaymentSuccessDialog(
        activity: Activity,
        message: String,
        onDismiss: (() -> Unit)? = null
    ) {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_payment_success, null)
        val dialog = android.app.AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<TextView>(R.id.tvSuccessMessage).text = message
        dialogView.findViewById<Button>(R.id.btnSuccessDone).setOnClickListener {
            dialog.dismiss()
            onDismiss?.invoke()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    /**
     * Persists language synchronously so [BaseActivity.attachBaseContext] sees the new value
     * when activities are recreated after a locale change.
     */
    fun setLang(mContext: Context?, lang: String?) {
        mContext?.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString("lang", lang)
            ?.commit()
    }

    /**
     * Opens Google Maps with the given coordinates.
     * Validates coordinates and handles coordinate swapping if needed.
     *
     * @param context The context to use for opening maps
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     */
    fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
        // Validate coordinates
        if (latitude == 0.0 && longitude == 0.0) {
            showErrorToast(context, "Invalid location coordinates")
            return
        }
        
        // Ensure coordinates are within valid ranges
        var validLat = latitude.coerceIn(-90.0, 90.0)
        var validLon = longitude.coerceIn(-180.0, 180.0)
        
        // Check if coordinates might be swapped (common issue)
        // If lat is outside valid range but lon is within lat range, swap them
        if (latitude !in -90.0..90.0 && longitude in -90.0..90.0) {
            // Likely swapped - swap them back
            validLat = longitude.coerceIn(-90.0, 90.0)
            validLon = latitude.coerceIn(-180.0, 180.0)
        }
        
        // Use Google Maps URI format: geo:lat,lon?q=lat,lon
        val uri = Uri.parse("geo:$validLat,$validLon?q=$validLat,$validLon")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to web browser if Google Maps app is not installed
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$validLat,$validLon")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            try {
                context.startActivity(webIntent)
            } catch (e: Exception) {
                showErrorToast(context, "Unable to open maps")
            }
        }
    }

    fun logout(mContext: Context?) {
        val sharedPreferences = mContext?.getSharedPreferences(HelperUtils.SHARED_PREF, Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.apply {
            putString("uid", "0")
            putString("role", "0")
            apply()
        }
    }
}