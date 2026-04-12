package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityEmployeeLoginBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.driver.DriverHomeActivity
import com.blueray.marasy.ui.shopper.ShopperHomeActivity
import com.blueray.marasy.viewmodel.AppViewModel

class EmployeeLoginActivity : BaseActivity() {

    private lateinit var binding: ActivityEmployeeLoginBinding
    private val viewmodel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.loginButton.setOnClickListener {
            validateFields()
        }

        //function for fields validation
        setupFieldListeners()

        //observers
        getEmployeeLogin()
    }

    private fun getEmployeeLogin() {
        viewmodel.getEmployeeLogin().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                        if (result.data.data.role == "driver") {
                            HelperUtils.saveUID(this, result.data.data.uid)
                            HelperUtils.saveRole(this, result.data.data.role)
                            val intent = Intent(this, DriverHomeActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else if (result.data.data.role == "shoper") {
                            HelperUtils.saveUID(this, result.data.data.uid)
                            HelperUtils.saveRole(this, result.data.data.role)
                            val intent = Intent(this, ShopperHomeActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }

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

    private fun setupFieldListeners() {
        binding.userNameEt.doAfterTextChanged {
            binding.userNameEt.clearErrorBorder()
        }
        binding.passwordEt.doAfterTextChanged {
            binding.passwordEt.clearErrorBorder()
        }
    }

    private fun validateFields() {
        if (binding.userNameEt.text.toString().trim().isEmpty()) {
            binding.userNameEt.setErrorBorder()
        } else {
            binding.userNameEt.clearErrorBorder()
        }
        if (binding.passwordEt.text.toString().trim().isEmpty()) {
            binding.passwordEt.setErrorBorder()
        } else {
            binding.passwordEt.clearErrorBorder()
        }
        if (
            binding.userNameEt.text.toString().isNotEmpty()
            && binding.passwordEt.text.toString().isNotEmpty()
        ) {
            viewmodel.retrieveEmployeeLogin(
                binding.userNameEt.text.toString(),
                binding.passwordEt.text.toString(),
                "12"
            )
        }
    }

    private fun View.setErrorBorder() {
        this.background = ContextCompat.getDrawable(
            context,
            R.drawable.edit_text_error_border_rounded
        )
    }

    private fun View.clearErrorBorder() {
        this.background =
            ContextCompat.getDrawable(
                context,
                R.drawable.edit_text_bg_rounded
            )
    }
}