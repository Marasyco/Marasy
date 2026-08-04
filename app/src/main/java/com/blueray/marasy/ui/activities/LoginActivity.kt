package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.blueray.marasy.MarasyApp
import com.onesignal.OneSignal
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityLoginBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.driver.DriverHomeActivity
import com.blueray.marasy.ui.shopper.ShopperHomeActivity
import com.blueray.marasy.viewmodel.AppViewModel

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewmodel by viewModels<AppViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request push notification permission on login screen
        OneSignal.promptForPushNotifications()

        // If user is already logged in, redirect to appropriate home
        if (HelperUtils.getUID(this) != "0" && HelperUtils.getUID(this) != null) {
            if (HelperUtils.getRole(this) == "customer") {
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
            } else if (HelperUtils.getRole(this) == "driver") {
                startActivity(Intent(this, DriverHomeActivity::class.java))
                finishAffinity()

            } else if (HelperUtils.getRole(this) == "shoper") {
                startActivity(Intent(this, ShopperHomeActivity::class.java))
                finishAffinity()

            }
        }
        binding.langIcon.setOnClickListener {
            val intent = Intent(this, ChangeLanguageActivity::class.java).apply {
                putExtra(ChangeLanguageActivity.EXTRA_RESTART, ChangeLanguageActivity.RESTART_FROM_LOGIN)
            }
            startActivity(intent)
        }
        // If guest (uid is "0"), stay on login page - don't redirect
        binding.loginButton.setOnClickListener {

            validateFields()
        }

        binding.createAccountButton.setOnClickListener {
            val intent = Intent(this, RegisterPhoneActivity::class.java)
            startActivity(intent)
        }

        binding.employeePortalButton.setOnClickListener {
            val intent = Intent(this, EmployeeLoginActivity::class.java)
            startActivity(intent)
        }

        binding.guestButton.setOnClickListener {
            // Set uid to 0 for guest mode
            HelperUtils.saveUID(this, "0")
            HelperUtils.saveRole(this, "0")

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        binding.contactText.setOnClickListener {
            val intent = Intent(this, ContactUsActivity::class.java)
            startActivity(intent)
        }

        getLogin()
    }


    private fun setLoginLoading(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.loginButton.text = if (isLoading) "" else getString(R.string.continue2)
        binding.loginProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun validateFields() {
        if (binding.editTextPhone.text.isEmpty() || binding.editTextPhone.text.length > 10) {
            binding.editTextPhone.error = getString(R.string.please_enter_a_valid_phone_number)
        } else {
            setLoginLoading(true)
            val deviceId = MarasyApp.getDeviceId(this)
            Log.d("****LoginActivity", "device_id: $deviceId")
            viewmodel.retrieveLoginUser(binding.editTextPhone.text.toString(), deviceId)
        }
    }

    private fun getLogin() {
        viewmodel.getLoginUser().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    setLoginLoading(false)
                    if (result.data.msg.status == 200) {
                        showToast(this, result.data.msg.message)
                        val intent = Intent(this, OtpActivity::class.java)
                        intent.putExtra("phoneNumber", binding.editTextPhone.text.toString())
                        startActivity(intent)
                    } else {
                        showErrorToast(this, result.data.msg.message)
                    }
                }

                is NetworkResults.Error -> {
                    setLoginLoading(false)
                    showErrorToast(this, result.exception.localizedMessage.toString())
                }

                else -> {}
            }
        }
    }
}