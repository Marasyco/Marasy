package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.MyLocationsAdapter
import com.blueray.marasy.databinding.ActivityMyLocationsBinding
import com.blueray.marasy.interfaces.AddressItemListener
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class MyLocationsActivity : BaseActivity() {
    private lateinit var binding: ActivityMyLocationsBinding
    private lateinit var adapter: MyLocationsAdapter
    private val viewmodel by viewModels<AppViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.my_addresses)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }

        binding.addAddress.setOnClickListener {
            val intent = Intent(this, AddAddressActivity::class.java)
            startActivity(intent)
        }

        viewmodel.retrieveMyAddresses()
        getAddresses()
        getSetDefaultAddress()
        getDeleteAddress()

    }

    private fun getAddresses() {
        viewmodel.getMyAddresses().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val addresses = result.data.data
                    if (addresses.isNullOrEmpty()) {
                        Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                        binding.locationsRv.adapter = null
                        return@observe
                    }
                    adapter = MyLocationsAdapter(addresses, object : AddressItemListener {
                        override fun onDefaultClick(id: String) {
                            viewmodel.retrieveSetDefaultAddress(id)
                        }

                        override fun onOptionsClick(id: String) {
                            val intent =
                                Intent(this@MyLocationsActivity, EditAddressActivity::class.java)
                            intent.putExtra("profile_id", id)
                            startActivity(intent)
                        }

                        override fun onDeleteClick(id: String) {
                            viewmodel.retrieveDeleteAddress(id)
                        }
                    })
                    binding.locationsRv.adapter = adapter
                    binding.locationsRv.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        this,
                        result.exception.localizedMessage ?: result.exception.message ?: "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    private fun getSetDefaultAddress() {
        viewmodel.getSetDefaultAddress().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                    viewmodel.retrieveMyAddresses()
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        this,
                        result.exception.localizedMessage ?: result.exception.message ?: "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
    }

    private fun getDeleteAddress() {
        viewmodel.getDeleteAddress().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                    viewmodel.retrieveMyAddresses()
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        this,
                        result.exception.localizedMessage ?: result.exception.message ?: "Error",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewmodel.retrieveMyAddresses()
    }
}