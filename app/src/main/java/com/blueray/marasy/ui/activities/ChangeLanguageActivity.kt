package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityChangeLanguageBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.setLang
import com.blueray.marasy.ui.driver.DriverHomeActivity
import com.blueray.marasy.ui.shopper.ShopperHomeActivity

class ChangeLanguageActivity : BaseActivity() {

    private lateinit var binding: ActivityChangeLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.language)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }

        binding.arabicButton.setOnClickListener {
            changeLanguage("ar")
        }

        binding.englishButton.setOnClickListener {
            changeLanguage("en")
        }
    }

    private fun changeLanguage(lang: String) {
        setLang(this, lang)

        val dest = resolveRestartDestination()
        val restart = Intent(this, dest).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(restart)
        finishAffinity()
    }

    /**
     * [attachBaseContext] only runs when an activity is created, so after changing the saved
     * language we must start a fresh task root. Logged-in users are routed by [HelperUtils.getRole];
     * guest (uid "0") needs [EXTRA_RESTART] to tell Home (drawer) apart from the login screen.
     */
    private fun resolveRestartDestination(): Class<*> {
        val uid = HelperUtils.getUID(this)
        val role = HelperUtils.getRole(this)
        val openedFromHome = intent.getStringExtra(EXTRA_RESTART) == RESTART_FROM_HOME

        return when {
            uid != "0" && role == "driver" -> DriverHomeActivity::class.java
            uid != "0" && role == "shoper" -> ShopperHomeActivity::class.java
            uid != "0" && role == "customer" -> HomeActivity::class.java
            uid == "0" && openedFromHome -> HomeActivity::class.java
            else -> LoginActivity::class.java
        }
    }

    companion object {
        const val EXTRA_RESTART = "extra_restart"
        const val RESTART_FROM_HOME = "from_home"
        const val RESTART_FROM_LOGIN = "from_login"
    }
}
