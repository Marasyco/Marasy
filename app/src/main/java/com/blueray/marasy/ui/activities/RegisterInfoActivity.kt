package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityRegisterInfoBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast

class RegisterInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.continueBtn.setOnClickListener {
            if (binding.ownerNameET.text.toString().isEmpty()) {
                showErrorToast(this, getString(R.string.fealidsRequred))
            } else {
                RegisterLocationActivity.NAME = binding.ownerNameET.text.toString()
                RegisterLocationActivity.EMAIL = binding.emailEt.text.toString()
                val intent = Intent(this, RegisterLocationActivity::class.java)
                startActivity(intent)
            }

        }
    }
}