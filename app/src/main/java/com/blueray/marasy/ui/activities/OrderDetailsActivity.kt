package com.blueray.marasy.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueray.marasy.R
import com.blueray.marasy.adapters.BestSellingAdapter
import com.blueray.marasy.adapters.MyOrderDetailsItemsAdapter
import com.blueray.marasy.databinding.ActivityOrderDetailsBinding
import com.blueray.marasy.helpers.HelperUtils.showToast
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.interfaces.OnBestSellingClick
import com.blueray.marasy.model.Item
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.viewmodel.AppViewModel

class OrderDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    private var orderId = ""
    private var orderNumber = ""
    private var fromCheckout = false
    private lateinit var relatedProductsAdapter: MyOrderDetailsItemsAdapter
    private val viewmodel by viewModels<AppViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        orderId = intent.getStringExtra("order_id").toString()
        fromCheckout = intent.getBooleanExtra("fromCheckout", false)
        //init bar
        binding.includeTab.title.text = getString(R.string.my_order)
        binding.includeTab.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.rateButton.setOnClickListener {
            // TODO: Implement RateActivity if needed
            // val intent = Intent(this, RateActivity::class.java)
            // intent.putExtra("order_id", orderId)
            // startActivity(intent)
            showToast(this, "Rating feature coming soon")
        }
        binding.reOrderButton.hide()

//        binding.reOrderButton.setOnClickListener {
//            viewmodel.retrieveReOrder(orderId)
//        }

        viewmodel.retrieveOrderDetails(orderId)

        getOrderDetails()
        getReOrder()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (fromCheckout == true) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }


    private fun getOrderDetails() {
        viewmodel.getOrderDetails().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        binding.orderId.text = "#" + result.data.data.order_number
                        orderNumber = result.data.data.order_number
                        //assign the general data
                        binding.orderTimeTv.text = result.data.data.order_date
                        binding.totalPriceTv.text = result.data.data.total_order_price + " JD"
                        binding.deliveryFees.text =
                            "${result.data.data.shiping_fees.amount} JD"

                        //adapter init

                        //handling every state of the order
                        when (result.data.data.state) {

                            //waiting prepare
                            2 -> {
                                binding.receivedImage.setImageResource(R.drawable.order_progress_done)
                                binding.receivedTv.setTextColor(getResources().getColor(R.color.orange))
                            }

                            //by prepare
                            3 -> {
                                binding.receivedImage.setImageResource(R.drawable.order_progress_done)
                                binding.receivedTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.makingTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.makingImage.setImageResource(R.drawable.order_progress_done)
                                binding.line1.setBackgroundColor(getColor(R.color.orange))
                                binding.orderText1.visibility = View.GONE
                                binding.employeeLayout.visibility = View.VISIBLE
                                binding.employeeStatusText.text =
                                    "يقوم ${result.data.data.shopper_name} بتسوق طلبك"
                                binding.callButton.setOnClickListener {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data =
                                            Uri.parse("tel:${result.data.data.shopper_phone_number}")
                                    }
                                    startActivity(intent)
                                }
                                binding.whatsAppButton.setOnClickListener {
                                    openWhatsApp(result.data.data.shopper_phone_number.toString())
                                }
                            }

                            //waiting delivery
                            4 -> {
                                binding.receivedImage.setImageResource(R.drawable.order_progress_done)
                                binding.receivedTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.makingTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.makingImage.setImageResource(R.drawable.order_progress_done)
                                binding.line1.setBackgroundColor(getColor(R.color.orange))
                                binding.orderText1.visibility = View.GONE
                                binding.employeeLayout.visibility = View.VISIBLE
                                binding.employeeStatusText.text =
                                    "يقوم ${result.data.data.shopper_name} بتسوق طلبك"

                                binding.callButton.setOnClickListener {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data =
                                            Uri.parse("tel:${result.data.data.shopper_phone_number}")
                                    }
                                    startActivity(intent)
                                }
                            }

                            //by delivery
                            5 -> {
                                binding.receivedImage.setImageResource(R.drawable.order_progress_done)
                                binding.receivedTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.makingTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.makingImage.setImageResource(R.drawable.order_progress_done)
                                binding.deliveringTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.deliveringImage.setImageResource(R.drawable.order_progress_done)
                                binding.line1.setBackgroundColor(getColor(R.color.orange))
                                binding.line2.setBackgroundColor(getColor(R.color.orange))
                                binding.orderText1.visibility = View.GONE
                                binding.employeeLayout.visibility = View.VISIBLE
                                binding.ourStatusText.text = "الطلب في الطريق اليك"
                                binding.employeeStatusText.text =
                                    "يقوم ${result.data.data.driver_name} بتسوق طلبك"

                                binding.callButton.setOnClickListener {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data =
                                            Uri.parse("tel:${result.data.data.driver_phone_number}")
                                    }
                                    startActivity(intent)
                                }
                                binding.whatsAppButton.setOnClickListener {
                                    openWhatsApp(result.data.data.driver_phone_number.toString())
                                }
                            }

                            //finished
                            6 -> {
                                binding.receivedImage.setImageResource(R.drawable.order_progress_done)
                                binding.receivedTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.makingTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.makingImage.setImageResource(R.drawable.order_progress_done)
                                binding.deliveringTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.deliveringImage.setImageResource(R.drawable.order_progress_done)
                                binding.deliveredTv.setTextColor(getResources().getColor(R.color.orange))
                                binding.deliveredImage.setImageResource(R.drawable.order_progress_done)
                                binding.line1.setBackgroundColor(getColor(R.color.orange))
                                binding.line2.setBackgroundColor(getColor(R.color.orange))
                                binding.line3.setBackgroundColor(getColor(R.color.orange))
                                binding.orderText1.visibility = View.GONE
                                binding.employeeLayout.visibility = View.VISIBLE
                                binding.rateButton.visibility = View.VISIBLE
                                binding.reOrderButton.visibility = View.GONE
                                binding.ourStatusText.text = "تم تسليم الطلب"
                                binding.employeeStatusText.text =
                                    "السائق ${result.data.data.driver_name} "

                                binding.callButton.setOnClickListener {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data =
                                            Uri.parse("tel:${result.data.data.driver_phone_number}")
                                    }
                                    startActivity(intent)
                                }
                                binding.whatsAppButton.setOnClickListener {
                                    openWhatsApp(result.data.data.driver_phone_number.toString())
                                }


                            }


                        }


                        relatedProductsAdapter = MyOrderDetailsItemsAdapter(object : OnBestSellingClick {
                            override fun onProductDetailsClick(id: String) {
//                                val intent = Intent(
//                                    this@OrderDetailsActivity,
//                                    ProductDetailsActivity::class.java
//                                )
//                                intent.putExtra("pid", id)
//                                startActivity(intent)
                            }
                        })
                        relatedProductsAdapter.submitList(result.data.data.items as MutableList<Item>)
                        binding.productsRv.adapter = relatedProductsAdapter
                        binding.productsRv.layoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    }
                }

                is NetworkResults.Error -> {

                }

                else -> {

                }
            }
        }
    }

    private fun getReOrder() {
        viewmodel.getReOrder().observe(this) { results ->
            when (results) {
                is NetworkResults.Success -> {
                    if (results.data.status == 200) {
                        showToast(this, results.data.message)
                        finish()
                    } else {
                        showToast(this, results.data.message)
                    }
                }

                is NetworkResults.Error -> {
                    showToast(this, results.exception.localizedMessage.toString())
                }

                else -> {

                }
            }
        }
    }


    private fun showConfirmDialog(message: String, onConfirm: () -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_action, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val tvMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        tvMessage.text = message

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun openWhatsApp(localNumber: String) {
        val internationalNumber = formatPhoneNumberToInternational(localNumber)

        try {
            val uri = Uri.parse("https://wa.me/$internationalNumber")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPhoneNumberToInternational(
        localNumber: String,
        countryCode: String = "962"
    ): String {
        return if (localNumber.startsWith("0")) {
            countryCode + localNumber.drop(1)
        } else {
            localNumber // already formatted
        }
    }

    override fun onResume() {
        super.onResume()
        viewmodel.retrieveOrderDetails(orderId)
        getOrderDetails()
    }
}
