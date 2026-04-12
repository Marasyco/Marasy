package com.blueray.marasy.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityMapsBinding
import com.blueray.marasy.helpers.HelperUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class MapsActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentCameraPosition: LatLng? = null
    private var isEditMode = false
    private var isAddMode = false

    companion object {
        const val FROM_EDIT_ADDRESS = "from_edit_address"
        const val FROM_ADD_LOCATION = "from_add_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra(FROM_EDIT_ADDRESS, false)
        isAddMode = intent.getBooleanExtra(FROM_ADD_LOCATION, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.locationCard.visibility = View.GONE

        binding.getLocationButton.setOnClickListener {
            getCameraCenterLocation()
        }
        binding.includeTab.title.text = getString(R.string.choose_your_location)
        binding.includeTab.backButton.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermissionAndGetLocation()

        mMap.setOnCameraMoveListener {
            binding.centerMarker.alpha = 0.7f
        }

        mMap.setOnCameraIdleListener {
            binding.centerMarker.alpha = 1.0f
            currentCameraPosition = mMap.cameraPosition.target
            updateLocationText(currentCameraPosition)
        }
        binding.locationCard.postDelayed({
            binding.locationCard.visibility = View.VISIBLE
        }, 3000)

    }

    private fun checkLocationPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                currentCameraPosition = currentLatLng
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationText(location: LatLng?) {
        location?.let {
            val geocoder = Geocoder(this, Locale(HelperUtils.getLang(this)))
            try {
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    binding.locationText.text = address
                    if (!isEditMode) {
//                        RegisterInfoActivity.AREATEXT = address
                    }
                } else {
                    binding.locationText.text = "No address found"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.locationText.text = "Unable to get address"
            }
        }
    }

    private fun getCameraCenterLocation() {
        currentCameraPosition?.let { position ->
            val lat = position.latitude.toString()
            val lng = position.longitude.toString()
            val addressText = binding.locationText.text.toString()

            if (isEditMode) {
//                EditAddressActivity.EDIT_LAT = lat
//                EditAddressActivity.EDIT_LONG = lng
//                EditAddressActivity.EDIT_AREA_TEXT = addressText
            }else if (isAddMode) {
                AddAddressActivity.ADD_LAT = lat
                AddAddressActivity.ADD_LONG = lng
                AddAddressActivity.ADD_AREA_TEXT = addressText
            }  else {
                RegisterLocationActivity.LAT = lat
                RegisterLocationActivity.LONG = lng
                RegisterLocationActivity.AREATEXT = addressText
            }

            finish()
        } ?: Toast.makeText(this, "Unable to get camera position", Toast.LENGTH_SHORT).show()
    }
}