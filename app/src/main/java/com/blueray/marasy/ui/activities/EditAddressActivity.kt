package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blueray.marasy.R
import com.blueray.marasy.adapters.CustomSpinnerAdapter
import com.blueray.marasy.databinding.ActivityEditAddressBinding
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class EditAddressActivity : BaseActivity() {
    companion object {
        var EDIT_LAT: String? = null
        var EDIT_LONG: String? = null
        var EDIT_AREA_TEXT: String? = null
        var EDIT_AREA: String? = null
    }

    private var profile_id = ""
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var binding: ActivityEditAddressBinding
    private var selectedAreaIdFromApi: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.includedTab.title.text = getString(R.string.edit_address)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        profile_id = intent.getStringExtra("profile_id").toString()
        viewmodel.retrieveAddressDetails(profile_id)

        binding.addressEt.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(MapsActivity.FROM_EDIT_ADDRESS, true)
            startActivity(intent)
        }

        binding.saveButton.setOnClickListener {
            viewmodel.retrieveEditAddress(
                address_line1 = EDIT_AREA_TEXT.toString(),
                detailed_address = binding.locationDetailsEt.text.toString(),
                profile_id = profile_id,
                lon = EDIT_LONG.toString(),
                lat = EDIT_LAT.toString(),
                city_and_area = EDIT_AREA.toString()
            )
        }
        getAddressDetails()
        getEditAddress()
    }

    private fun getAddressDetails() {
        viewmodel.getAddressDetails().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val fullAddress = result.data.data.firstOrNull()?.FullAddress
                    if (fullAddress != null) {
                        binding.addressEt.setText(fullAddress.address_line1)
                        binding.locationDetailsEt.setText(fullAddress.detailed_address)
                        EDIT_LAT = fullAddress.lat.toString()
                        EDIT_LONG = fullAddress.lon.toString()
                        EDIT_AREA_TEXT = fullAddress.address_line1

                        selectedAreaIdFromApi = fullAddress.city_and_area_id
                        Log.d("ADDRESS", "Selected area ID: $selectedAreaIdFromApi")

                        viewmodel.retrieveAreas("1")
                        getAreas()
                    } else {
                        Toast.makeText(this, "Address details are empty", Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(this, "Error fetching address", Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    private fun getAreas() {
        viewmodel.getAreas().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val areas = result.data.data
                    if (!areas.isNullOrEmpty()) {
                        val customAdapter = CustomSpinnerAdapter(this, areas)
                        binding.areaSpinner.adapter = customAdapter

                        // Match selected area ID
                        val selectedIndex = areas.indexOfFirst { it.tid == selectedAreaIdFromApi }
                        Log.d("AREAS", "Matched index: $selectedIndex")

                        if (selectedIndex >= 0) {
                            binding.areaSpinner.setSelection(selectedIndex)
                        }

                        binding.areaSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedArea = areas[position]
                                    if (selectedArea.tid != "0") {
                                        EDIT_AREA = selectedArea.tid
                                        Log.d("AREAS", "User selected area: ${selectedArea.name}")
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {}
                            }
                    } else {
                        Toast.makeText(this, "No areas found", Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(this, "Failed to load areas", Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    private fun getEditAddress() {
        viewmodel.getEditAddress().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                    finish()
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        this,
                        result.exception.localizedMessage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else ->{}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("ADDREESSS", EDIT_LAT ?: "LAT null")
        Log.d("ADDREESSS", EDIT_LONG ?: "LONG null")
        binding.addressEt.setText(EDIT_AREA_TEXT)
    }
}