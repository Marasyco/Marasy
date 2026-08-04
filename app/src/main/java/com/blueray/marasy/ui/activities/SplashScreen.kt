package com.blueray.marasy.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.blueray.marasy.databinding.ActivitySplashBinding
import com.blueray.marasy.helpers.HelperUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

@SuppressLint("CustomSplashScreen")
class SplashScreen : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set Arabic as default language (only language supported)
        HelperUtils.setLang(this, "ar")

        lifecycleScope.launch {
            delay(1000)
            binding.splash2.apply {
                alpha = 0f       // start invisible
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)   // fade to fully visible
                    .setDuration(500) // animation duration in ms
                    .start()
            }
            delay(2000)


                if (HelperUtils.getUID(this@SplashScreen) == "0") {
                    openUser()
                } else {
                    openMainLogin()
                }


        }


    }

    private fun openUser() {
        // Set Arabic as default language and skip language selection
        HelperUtils.setLang(this, "ar")
        val intentHome = Intent(this, OnBoardingActivity::class.java)
        startActivity(intentHome)
        finish()
    }
    private fun openMainLogin() {
        val intentHome = Intent(this, LoginActivity::class.java)
        startActivity(intentHome)
        finish()
    }

}