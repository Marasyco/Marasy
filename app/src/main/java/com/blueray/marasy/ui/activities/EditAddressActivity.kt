package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import com.blueray.marasy.R
import com.blueray.marasy.adapters.CustomSpinnerAdapter
import com.blueray.marasy.databinding.ActivityEditAddressBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.showToast
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

    private var selectedCityIdFromApi: String? = null
    private var selectedAreaIdFromApi: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.edit_address)
        binding.includedTab.backButton.setOnClickListener { finish() }

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
        getCities()
        getAreas()
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
                        EDIT_AREA = fullAddress.city_and_area_id

                        selectedCityIdFromApi = fullAddress.city_id
                        selectedAreaIdFromApi = fullAddress.city_and_area_id

                        Log.d("EDIT_ADDRESS", "city_id=$selectedCityIdFromApi area_id=$selectedAreaIdFromApi")

                        viewmodel.retrieveCities()
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

    private fun getCities() {
        viewmodel.getCities().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val cities = result.data.data
                    if (!cities.isNullOrEmpty()) {
                        val customAdapter = CustomSpinnerAdapter(this, cities)
                        binding.sectorCity.adapter = customAdapter

                        binding.sectorCity.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedCity = cities[position]
                                    viewmodel.retrieveAreas(selectedCity.tid)
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {}
                            }

                        // Pre-select the city that matches the saved address
                        val cityIndex = cities.indexOfFirst { it.tid == selectedCityIdFromApi }
                        if (cityIndex >= 0) {
                            binding.sectorCity.setSelection(cityIndex)
                        } else {
                            // Fallback: trigger first city to load its areas
                            viewmodel.retrieveAreas(cities[0].tid)
                        }
                    } else {
                        Toast.makeText(this, "No cities available", Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResults.Error -> {}
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
                        binding.sectorArea.adapter = customAdapter

                        binding.sectorArea.onItemSelectedListener =
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
                                        Log.d("EDIT_ADDRESS", "Area selected: ${selectedArea.name}")
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {}
                            }

                        // Pre-select the area that matches the saved address
                        val areaIndex = areas.indexOfFirst { it.tid == selectedAreaIdFromApi }
                        if (areaIndex >= 0) {
                            binding.sectorArea.setSelection(areaIndex)
                        }
                    } else {
                        Toast.makeText(this, "No areas found", Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResults.Error -> {}
                else -> {}
            }
        }
    }

    private fun getEditAddress() {
        viewmodel.getEditAddress().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    showToast(this, result.data.msg.message)
                    finish()
                }

                is NetworkResults.Error -> {
                    showErrorToast(this, result.exception.localizedMessage ?: result.exception.message ?: "Unknown error")
                }

                else -> {}
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("EDIT_ADDRESS", "LAT=${EDIT_LAT ?: "null"} LONG=${EDIT_LONG ?: "null"}")
        binding.addressEt.setText(EDIT_AREA_TEXT)
    }
}
