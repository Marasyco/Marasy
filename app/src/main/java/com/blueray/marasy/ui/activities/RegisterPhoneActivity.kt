package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityRegisterPhoneBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class RegisterPhoneActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterPhoneBinding
    private val viewmodel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterPhoneBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)


        binding.backButton.setOnClickListener {
            finish()
        }

        binding.checkPhoneButton.setOnClickListener {
            if (binding.phoneEt.text.isEmpty() || binding.phoneEt.text.length > 10) {
                binding.phoneEt.error = getString(R.string.please_enter_a_valid_phone_number)
            } else {
                viewmodel.retrieveCheckPhone(binding.phoneEt.text.toString(), "0")
            }
        }
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        getCheckPhone()
    }

    private fun getCheckPhone() {
        viewmodel.getCheckPhone().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {

                    if (result.data.msg.status == 200) {
                        showToast(this, result.data.msg.message)
                        val intent = Intent(this ,RegisterInfoActivity::class.java )
                        RegisterLocationActivity.PHONE = binding.phoneEt.text.toString()
                        startActivity(intent)

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