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
import com.blueray.marasy.databinding.ActivityAddAddressBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.ui.activities.RegisterLocationActivity.Companion.CITY
import com.blueray.marasy.viewmodel.AppViewModel

class AddAddressActivity : BaseActivity() {

    private lateinit var binding: ActivityAddAddressBinding
    private val viewmodel by viewModels<AppViewModel>()

    companion object {
        var ADD_LAT: String? = null
        var ADD_LONG: String? = null
        var ADD_AREA_TEXT: String? = null
    }

    private var city = ""
    private var area = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.includedTab.title.text = getString(R.string.add_new_address1)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }

        binding.mapIcon.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(MapsActivity.FROM_ADD_LOCATION, true)
            startActivity(intent)
        }

        binding.saveButton.setOnClickListener {
            viewmodel.retrieveAddAddress(
                //address line 1 should be the title of the address
                address_line1 = binding.titleEt.text.toString(),
                lat = ADD_LAT.toString(),
                lon = ADD_LONG.toString(),
                cityAndArea = area,
                detailed_address = binding.addressInDetailEt.text.toString(),
            )
        }

        viewmodel.retrieveCities()

        getCities()
        getAreas()
        getAddAddress()
    }


    private fun getCities() {
        viewmodel.getCities().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val cities =
                        result.data.data // Assuming result.data contains the parsed list of cities
                    if (cities != null && cities.isNotEmpty()) {

                        val cityNames = cities.map { it.name }

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
                                    CITY = selectedCity.name
                                    viewmodel.retrieveAreas(selectedCity.tid)
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {
                                    // Optional: Handle case when no item is selected
                                }
                            }
                    } else {
                        Toast.makeText(this, "No cities available", Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResults.Error -> {

                }

                else -> {

                }
            }

        }
    }

    private fun getAreas() {
        viewmodel.getAreas().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val areas = result.data.data
                    if (areas != null && areas.isNotEmpty()) {
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
                                        area = selectedArea.tid
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }
                            }
                    }
                }

                is NetworkResults.Error -> {

                }

                else -> {

                }
            }
        }
    }

    private fun getAddAddress() {
        viewmodel.getAddAddress().observe(this) { result ->

            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        showToast(this, result.data.msg.message)
                        finish()
                    } else {
                        showErrorToast(this, result.data.msg.message)
                    }
                }

                is NetworkResults.Error -> {
                    showErrorToast(this, result.exception.message.toString())
                }

                else -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("ADDREESSS", ADD_LAT ?: "LAT null")
        Log.d("ADDREESSS", ADD_LONG ?: "LONG null")
        binding.mapLocation.text = ADD_AREA_TEXT
    }
}