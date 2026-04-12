package com.blueray.marasy.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityAboutUsBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class AboutUsActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutUsBinding
    private val viewmodel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.about_us)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        viewmodel.retrieveAboutUs()
        getAboutUs()
    }

    private fun getAboutUs() {
        viewmodel.getAboutUs().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.title1.text = result.data.data[0].title
                    binding.body1.text = result.data.data[0].body
                    binding.title2.text = result.data.data[1].title
                    binding.body2.text = result.data.data[1].body
                    binding.title3.text = result.data.data[2].title
                    binding.body3.text = result.data.data[2].body
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