package com.blueray.marasy.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityEditProfileBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class EditProfileActivity : BaseActivity() {
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.edit_information)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }

        viewmodel.retrieveViewProfile()

        binding.saveButton.setOnClickListener {
            viewmodel.retrieveUpdateUser(
                binding.nameEt.text.toString(),
                binding.emailEt.text.toString()
            )
        }

        getProfile()
        getUpdateProfile()
    }

    private fun getProfile() {
        viewmodel.getViewProfile().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.nameEt.setText(result.data.data.personal.full_name)
                    binding.emailEt.setText(result.data.data.personal.mail)
                }

                is NetworkResults.Error -> {
                    showErrorToast(this, result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }

    private fun getUpdateProfile() {
        viewmodel.getUpdateUser().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {

                        showToast(this, result.data.msg.message)
                        finish()
                    } else {
                        showErrorToast(this, result.data.msg.message)
                    }
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