package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityOtpBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.driver.DriverHomeActivity
import com.blueray.marasy.ui.shopper.ShopperHomeActivity
import com.blueray.marasy.viewmodel.AppViewModel

class OtpActivity : BaseActivity() {
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var binding: ActivityOtpBinding
    private var phoneNumber = ""
    private var flag = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        flag = intent.getStringExtra("flag").toString()

        binding.phoneNumberText.text = phoneNumber
        if (flag == "1") {
            viewmodel.retrieveCheckPhone(phoneNumber, "1")
        }
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.sendButton.setOnClickListener {
            if (binding.otpBox.otp.toString() == "") {
                showErrorToast(this, getString(R.string.please_fill_the_otp_code))
            } else {
//                binding.loadingLayout.show()
                viewmodel.retrieveLoginUserOtp(
                    phoneNumber,
                    binding.otpBox.otp.toString()
                )
            }
        }

        getLoginOtp()
    }

    private fun getLoginOtp() {
        viewmodel.getLoginUserOtp().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
//                    binding.loadingLayout.hide()
                    if (result.data.msg.status == 200) {
                        showToast(this, result.data.msg.message)
                        val sharedPreferences =
                            getSharedPreferences(HelperUtils.SHARED_PREF, MODE_PRIVATE)

                        val role = result.data.data.role
                        sharedPreferences.edit().apply {
                            putString("uid", result.data.data.id)
                            putString("role", role)
                        }.apply()
                        
                        // Redirect based on role
                        val intent = when (role) {
                            "driver" -> Intent(this, DriverHomeActivity::class.java)
                            "shoper" -> Intent(this, ShopperHomeActivity::class.java)
                            else -> Intent(this, HomeActivity::class.java) // customer or default
                        }
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        showErrorToast(this, result.data.msg.message)
                    }
                }

                is NetworkResults.Error -> {
//                    binding.loadingLayout.hide()
                    showErrorToast(this, result.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }
}