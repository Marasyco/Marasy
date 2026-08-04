package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.blueray.marasy.databinding.ActivityProfileBinding
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding

    private val viewmodel by viewModels<AppViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        setContentView(binding.root)
        
        if (com.blueray.marasy.helpers.HelperUtils.isGuest(this)) {
            com.blueray.marasy.helpers.HelperUtils.showLoginRequiredDialog(this) {
                val intent = Intent(this, com.blueray.marasy.ui.activities.LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            return
        }
        
        binding.includedTap.title.text = "Profile"
        binding.includedTap.backButton.setOnClickListener {
            finish()
        }

        binding.addressBtn.setOnClickListener {
            startActivity(Intent(this, MyLocationsActivity::class.java))
        }
        binding.editInfo.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        viewmodel.retrieveViewProfile()

        getProfile()
    }

    private fun getProfile() {
        viewmodel.getViewProfile().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {

                        Log.d("PORFFFSX", result.data.toString())


                        binding.establishmentNameLabel.text = result.data.data.personal?.mail.toString()
                        binding.name.text = result.data.data.personal?.full_name.toString()
                        binding.phoneNumberTV.text = result.data.data.personal?.phone.toString()







                    } else {


                    }
                }

                is NetworkResults.Error -> {
                    result.exception.printStackTrace()

                }

                else -> {

                }

            }
        }
    }
}