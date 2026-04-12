package com.blueray.marasy.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.blueray.marasy.R
import com.blueray.marasy.helpers.ContextWrapper
import com.blueray.marasy.helpers.HelperUtils.getLang
import java.util.Locale

abstract class BaseActivity : AppCompatActivity(){
    protected var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        // Hide status bar
//        WindowInsetsControllerCompat(window, window.decorView).apply {
//            hide(WindowInsetsCompat.Type.statusBars()) // hide status bar
//            systemBarsBehavior =
//                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
    }
    // to save the context of the resources and save the language if destroyed
    override fun attachBaseContext(newBase: Context?) {
        val lang = getLang(newBase!!)
        val local = Locale(lang)
        val newContext = ContextWrapper.wrap(newBase, local)
        super.attachBaseContext(newContext)
    }


    protected fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    protected fun hideProgressBar() {
        progressBar?.visibility = View.INVISIBLE
    }

    protected fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(R.string.next, null)
            .setCancelable(false)
            .show()
    }

    protected fun showAlertDialog(message: String) {
        showAlert(message)
    }

    protected fun showAlertDialog(messageId: Int) {
        showAlert(getString(messageId))
    }


}