package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.blueray.marasy.adapters.CustomSpinnerAdapter
import com.blueray.marasy.databinding.ActivityRegisterLocationBinding
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class RegisterLocationActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterLocationBinding
    private val viewmodel by viewModels<AppViewModel>()

    companion object {
        var PHONE = ""
        var LAT = ""
        var LONG = ""
        var CITY = ""
        var AREA = ""
        var AREATEXT = ""
        var NAME = ""
        var SECTOR = ""
        var EMAIL = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterLocationBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.mapIcon.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        binding.continueBtn.setOnClickListener {

            viewmodel.retrieveAddUser(
                phone = PHONE,
                area = AREA,
                full_name = NAME,
                city = CITY,
                lat = LAT,
                lon = LONG,
                detailed_address = binding.addressInDetailEt.text.toString(),
                playerId = "12345",
                sector = SECTOR,
                email = EMAIL,
                address_line1 = binding.titleEt.text.toString()
            )

        }

        viewmodel.retrieveCities()


        getCities()
        getAreas()
        getAddUser()
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
                                        AREA = selectedArea.tid
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

    private fun getAddUser() {
        viewmodel.getAddUser().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        Toast.makeText(
                            this,
                            result.data.msg.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this, OtpActivity::class.java)
                        intent.putExtra("phoneNumber", PHONE)
                        intent.putExtra("flag", "1")
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            result.data.msg.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is NetworkResults.Error -> {

                }

                else -> {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("ADDREESSS", LAT)
        Log.d("ADDREESSS", LONG)
        binding.mapLocation.text = AREATEXT
    }
}