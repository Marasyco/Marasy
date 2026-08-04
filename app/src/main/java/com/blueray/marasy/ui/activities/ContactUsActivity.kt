package com.blueray.marasy.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityContactUsBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class ContactUsActivity : BaseActivity() {

    private lateinit var binding: ActivityContactUsBinding
    private val viewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        binding.includedTab.title.text = getString(R.string.support)

        val phoneNumber = "0798208089"

        binding.circleImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
        }

        binding.whats.setOnClickListener {
            openWhatsApp(phoneNumber)
        }

        binding.sendBtn.setOnClickListener {
            submitForm()
        }

        observeContactUs()
    }

    private fun submitForm() {
        val phone = binding.phoness.text?.toString()?.trim().orEmpty()
        val fullName = binding.naemofEns.text?.toString()?.trim().orEmpty()
        val notes = binding.messages.text?.toString()?.trim().orEmpty()

        if (phone.isEmpty() || fullName.isEmpty() || notes.isEmpty()) {
            Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show()
            return
        }

        binding.sendBtn.isEnabled = false
        viewModel.submitContactUs(phone, notes, fullName)
    }

    private fun observeContactUs() {
        viewModel.getContactUs().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.sendBtn.isEnabled = true
                    val message = result.data.msg.message
                    Log.d(TAG, "contact-us: $message")
                    if (result.data.msg.status == 200) {
                        showToast(this, message)
                        binding.phoness.text?.clear()
                        binding.naemofEns.text?.clear()
                        binding.messages.text?.clear()
                    } else {
                        showErrorToast(this, message)
                    }
                }

                is NetworkResults.ErrorMessage -> {
                    binding.sendBtn.isEnabled = true
                    val message = result.data?.msg?.message.orEmpty()
                    Log.d(TAG, "contact-us error body: $message")
                    if (message.isNotEmpty()) {
                        showErrorToast(this, message)
                    } else {
                        Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResults.Error -> {
                    binding.sendBtn.isEnabled = true
                    Log.e(TAG, "contact-us", result.exception)
                    showErrorToast(
                        this,
                        result.exception.localizedMessage ?: getString(R.string.something_wrong)
                    )
                }

                else -> {}
            }
        }
    }

    private fun openWhatsApp(phoneNumber: String) {
        val internationalNumber = if (phoneNumber.startsWith("0")) {
            "962${phoneNumber.substring(1)}"
        } else {
            phoneNumber
        }

        try {
            val uri = Uri.parse("https://wa.me/$internationalNumber")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val uri = Uri.parse("https://wa.me/$internationalNumber")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "ContactUsActivity"
    }
}
