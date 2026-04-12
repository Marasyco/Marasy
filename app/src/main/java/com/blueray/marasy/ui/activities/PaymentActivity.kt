package com.blueray.marasy.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.blueray.marasy.adapters.AddressesSpinnerAdapter
import com.blueray.marasy.databinding.ActivityPaymentBinding
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.services.MastercardPaymentService
import com.blueray.marasy.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PaymentActivity : BaseActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private val viewmodel by viewModels<AppViewModel>()
    
    private var orderId = ""
    private var totalPrice = ""
    private var deliveryFees = ""
    private var progressDialog: ProgressDialog? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        orderId = intent.getStringExtra("orderId").toString()
        totalPrice = intent.getStringExtra("totalPrice").toString()
        deliveryFees = intent.getStringExtra("deliveryFees").toString()
        
        binding.totalItemsPrice.text = totalPrice
        binding.deliveryFeesTv.text = deliveryFees
        
        binding.includedTab.backButton.setOnClickListener {
            finish()
        }
        
        binding.addNewAddressTv.setOnClickListener {
            val intent = Intent(this, AddAddressActivity::class.java)
            startActivity(intent)
        }
        
        binding.addProductsButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
        
        binding.CashOnDeliveryButton.setOnClickListener {
            viewmodel.retrieveCheckout(orderId, "1")
        }
        
        binding.onlinePaymentButton.setOnClickListener {
            initiateOnlinePayment()
        }
        
        viewmodel.retrieveMyAddresses()
        viewmodel.retrieveViewProfile()
        
        getMyAddresses()
        getProfile()
        getCheckout()
    }
    
    private fun initiateOnlinePayment() {
        showProgressDialog("Processing payment...")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Extract amount from price string
                val amount = MastercardPaymentService.extractAmount(totalPrice)
                
                // Create a session for native card collection
                val sessionResult = MastercardPaymentService.createSession()
                
                if (sessionResult.isSuccess) {
                    val sessionResponse = sessionResult.getOrNull()!!
                    hideProgressDialog()
                    Log.d("Payment", "Session created: ${sessionResponse.session.id}")
                    
                    // Open native card payment activity with session ID
                    val intent = Intent(this@PaymentActivity, NativeCardPaymentActivity::class.java)
                    intent.putExtra(NativeCardPaymentActivity.EXTRA_ORDER_ID, orderId)
                    intent.putExtra(NativeCardPaymentActivity.EXTRA_AMOUNT, amount)
                    intent.putExtra(NativeCardPaymentActivity.EXTRA_SESSION_ID, sessionResponse.session.id)
                    startActivityForResult(intent, PAYMENT_REQUEST_CODE)
                } else {
                    hideProgressDialog()
                    val error = sessionResult.exceptionOrNull() ?: Exception("Unknown error")
                    Toast.makeText(
                        this@PaymentActivity,
                        "Failed to initialize payment: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("Payment", "Failed to create session", error)
                }
            } catch (e: Exception) {
                hideProgressDialog()
                Toast.makeText(
                    this@PaymentActivity,
                    "Payment error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("Payment", "Payment error", e)
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PAYMENT_REQUEST_CODE) {
            when (resultCode) {
                NativeCardPaymentActivity.RESULT_PAYMENT_SUCCESS -> {
                    // Payment successful, complete checkout with payment method "2"
                    data?.getStringExtra("orderId")?.let {
                        viewmodel.retrieveCheckout(it, "2") // "2" for online payment
                    } ?: run {
                        viewmodel.retrieveCheckout(orderId, "2")
                    }
                }
                NativeCardPaymentActivity.RESULT_PAYMENT_FAILED -> {
                    Toast.makeText(this, "Payment was cancelled or failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showProgressDialog(message: String) {
        progressDialog = ProgressDialog(this).apply {
            setMessage(message)
            setCancelable(false)
            show()
        }
    }
    
    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
    
    companion object {
        private const val PAYMENT_REQUEST_CODE = 1001
    }


    private fun getMyAddresses() {
        viewmodel.getMyAddresses().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    val list = result.data.data
                    val adapter = AddressesSpinnerAdapter(this, list ?: listOf())
                    binding.addressesSpinner.adapter = adapter
                }

                else -> {

                }
            }
        }
    }

    private fun getProfile() {
        viewmodel.getViewProfile().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    binding.nameTv.text = result.data.data.personal.full_name
                    binding.phoneTv.text = result.data.data.personal.phone
                }

                else -> {

                }
            }

        }
    }

    private fun getCheckout() {
        viewmodel.getCheckout().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    if (result.data.msg.status == 200) {
                        Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(this, result.data.msg.message, Toast.LENGTH_SHORT).show()
                    }
                }

                is NetworkResults.Error -> {
                    Log.d("ErrorCheckOut", result.exception.localizedMessage.toString())
                    Toast.makeText(
                        this,
                        result.exception.localizedMessage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is NetworkResults.ErrorMessage -> {
                    Toast.makeText(this, result.data?.msg?.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                    Log.d("ERORRSAS", result.data.toString())
                }

                else -> {

                }
            }
        }
    }
}