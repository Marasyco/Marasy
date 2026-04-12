package com.blueray.marasy.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityPrivacyBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class PrivacyActivity : BaseActivity() {

    private lateinit var binding: ActivityPrivacyBinding
    private val viewmodel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.privacy)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        viewmodel.retrievePrivacyPolicy()
        getPrivacyPolicy()

    }

    private fun getPrivacyPolicy() {
        viewmodel.getPrivacyPolicy().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.title.text = result.data.data.title
                    binding.body.text = result.data.data.body
                }

                is NetworkResults.Error -> {
                    showErrorToast(this, result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }
}