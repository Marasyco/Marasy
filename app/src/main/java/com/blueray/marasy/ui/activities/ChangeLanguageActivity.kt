package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityChangeLanguageBinding
import com.blueray.marasy.helpers.HelperUtils.setDefaultLanguage
import com.blueray.marasy.helpers.HelperUtils.setLang
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        lifecycleScope.launch {
            delay(300)
            setDefaultLanguage(this@ChangeLanguageActivity, lang)
            setLang(this@ChangeLanguageActivity, lang)
            startActivity(Intent(this@ChangeLanguageActivity, HomeActivity::class.java))
            finish()

        }
    }
}