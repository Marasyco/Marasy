package com.blueray.marasy.ui.driver

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.OrderImagesAdapter
import com.blueray.marasy.databinding.ActivityDriverOrderDetailsBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.helpers.HelperUtils.openGoogleMaps
import com.blueray.marasy.helpers.HelperUtils.showConfirmDialog
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.helpers.ViewUtils.show
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class DriverOrderDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDriverOrderDetailsBinding
    private val viewmodel by viewModels<AppViewModel>()
    private var orderId = ""
    private var orderState = ""
    private var lat = 0.00
    private var lon = 0.00
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        orderId = intent.getStringExtra("orderId").toString()
        orderState = intent.getStringExtra("orderState").toString()

        binding.includedTab.title.text = getString(R.string.order_details)
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }

        when (orderState) {
            "1" -> {
                binding.startDeliveryButton.show()
                binding.completeDeliveryButton.hide()
            }

            "2" -> {
                binding.startDeliveryButton.hide()
                binding.completeDeliveryButton.show()
            }

            else -> {
                binding.startDeliveryButton.hide()
                binding.completeDeliveryButton.hide()
            }

        }

        binding.startDeliveryButton.setOnClickListener {
            showConfirmDialog(this, "Start delivering order?") {
                viewmodel.retrieveStartEndEmployeeOrder(
                    orderId,
                    "1", // 1 for start
                    "2" // 2 for driver
                )
            }
        }

        binding.completeDeliveryButton.setOnClickListener {
            showConfirmDialog(this, getString(R.string.end_delivering_order)) {
                viewmodel.retrieveStartEndEmployeeOrder(
                    orderId,
                    "2", // 2 for end
                    "2" // 2 for driver
                )
            }
        }

        viewmodel.retrieveDriverOrderDetails(orderId)
        getOrderDetails()
        getStartEndOrder()
    }


    private fun getOrderDetails() {
        viewmodel.getDriverOrderDetails().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.paymentWayTv.text = result.data.data.payment_gateway
                    binding.itemCountTv.text = result.data.data.order_items_count.toString()
                    binding.orderTotalTv.text = result.data.data.total_order_price + " JD"
                    binding.notesTv.text = result.data.data.notes
                    binding.clientNameTv.text = result.data.data.customer_full_name
                    binding.phoneNumberTv.text = result.data.data.customer_phone_number

                    lat = result.data.data.billing_information.lat
                    lon = result.data.data.billing_information.lon

                    val adapter = OrderImagesAdapter(result.data.data.items)
                    binding.itemsRv.adapter = adapter
                    binding.itemsRv.layoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        this,
                        result.exception.localizedMessage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                }
            }
        }
    }

    private fun getStartEndOrder() {
        viewmodel.getStartEndEmployeeOrder().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (orderState == "1") {
                        Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                        openInGoogleMaps(this, lat, lon)
                        finish()
                    } else if (orderState == "2") {
                        Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(
                        this,
                        result.exception.localizedMessage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                }
            }
        }
    }

    private fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double) {
        HelperUtils.openGoogleMaps(context, latitude, longitude)
    }
}