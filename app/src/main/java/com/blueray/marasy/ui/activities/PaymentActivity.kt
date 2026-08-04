package com.blueray.marasy.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.blueray.marasy.R
import com.blueray.marasy.adapters.AddressesSpinnerAdapter
import com.blueray.marasy.databinding.ActivityPaymentBinding
import com.blueray.marasy.helpers.PaymentLogger
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
    private var deliveryTime = ""
    private var progressDialog: ProgressDialog? = null
    private var isCompletingOnlineCheckout = false
    private var isCheckoutPending = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        orderId = intent.getStringExtra("orderId").toString()
        totalPrice = intent.getStringExtra("totalPrice").toString()
        deliveryFees = intent.getStringExtra("deliveryFees").toString()
        deliveryTime = intent.getStringExtra("deliveryTime").toString()

        binding.totalItemsPrice.text = totalPrice
        binding.deliveryFeesTv.text = deliveryFees
        binding.deliveryTimeTv.text = deliveryTime

        PaymentLogger.d(
            "PaymentScreen",
            "Opened -> orderId=$orderId, totalPrice=$totalPrice, deliveryFees=$deliveryFees, " +
                "parsedAmount=${MastercardPaymentService.parseAmount(totalPrice)}"
        )
        
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
            startBackendCheckout("1")
        }
        
        binding.onlinePaymentButton.setOnClickListener {
            initiateOnlinePayment()
        }
        
        viewmodel.retrieveMyAddresses()
        viewmodel.retrieveViewProfile()
        
        viewmodel.clearCheckoutResult()
        getMyAddresses()
        getProfile()
        getCheckout()
    }

    private fun startBackendCheckout(paymentMethod: String, checkoutOrderId: String = orderId) {
        isCheckoutPending = true
        isCompletingOnlineCheckout = paymentMethod == "2"
        showProgressDialog(getString(R.string.processing_payment))
        viewmodel.retrieveCheckout(checkoutOrderId, paymentMethod, binding.noteEt.text.toString())
    }

    override fun onResume() {
        super.onResume()
        viewmodel.retrieveMyAddresses()
    }
    
    private fun initiateOnlinePayment() {
        val parsedAmount = MastercardPaymentService.parseAmount(totalPrice)
        val gatewayAmount = MastercardPaymentService.extractAmount(totalPrice)
        Log.d(
            TAG,
            "Initiating online payment -> orderId=$orderId, totalPrice=$totalPrice, " +
                "parsedAmount=$parsedAmount, gatewayAmount=$gatewayAmount"
        )
        PaymentLogger.d(
            "PaymentScreen",
            "Initiating online payment -> orderId=$orderId, totalPrice=$totalPrice, " +
                "parsedAmount=$parsedAmount, gatewayAmount=$gatewayAmount"
        )

        if (parsedAmount == null) {
            Log.e(TAG, "Invalid payment amount: totalPrice=$totalPrice")
            Toast.makeText(this, getString(R.string.payment_failed), Toast.LENGTH_SHORT).show()
            return
        }

        if (parsedAmount < MastercardPaymentService.MIN_ONLINE_PAYMENT_JOD) {
            Log.w(
                TAG,
                "Online payment blocked: amount $parsedAmount JOD is below minimum " +
                    "${MastercardPaymentService.MIN_ONLINE_PAYMENT_JOD} JOD"
            )
            Toast.makeText(this, getString(R.string.minimum_payment_amount), Toast.LENGTH_LONG).show()
            return
        }

        showProgressDialog(getString(R.string.processing_payment))
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Extract amount from price string
                val amount = gatewayAmount
                
                // Initiate hosted checkout session (supports 3D Secure)
                val checkoutResult = MastercardPaymentService.initiateCheckout(
                    orderId = orderId,
                    amount = amount,
                    currency = "JOD"
                )
                
                if (checkoutResult.isSuccess) {
                    val sessionResponse = checkoutResult.getOrNull()!!
                    hideProgressDialog()
                    Log.d(
                        TAG,
                        "Hosted checkout session created -> sessionId=${sessionResponse.session.id}, " +
                            "result=${sessionResponse.result}, updateStatus=${sessionResponse.session.updateStatus}"
                    )
                    PaymentLogger.d(
                        "PaymentScreen",
                        "Opening hosted checkout (3DS supported) -> sessionId=${sessionResponse.session.id}"
                    )
                    
                    val intent = Intent(this@PaymentActivity, MastercardPaymentActivity::class.java)
                    intent.putExtra(MastercardPaymentActivity.EXTRA_ORDER_ID, orderId)
                    intent.putExtra(MastercardPaymentActivity.EXTRA_AMOUNT, amount)
                    intent.putExtra(MastercardPaymentActivity.EXTRA_SESSION_ID, sessionResponse.session.id)
                    startActivityForResult(intent, PAYMENT_REQUEST_CODE)
                } else {
                    hideProgressDialog()
                    val error = checkoutResult.exceptionOrNull() ?: Exception("Unknown error")
                    Toast.makeText(
                        this@PaymentActivity,
                        "Failed to initialize payment: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "Failed to create gateway session", error)
                }
            } catch (e: Exception) {
                hideProgressDialog()
                Toast.makeText(
                    this@PaymentActivity,
                    "Payment error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Payment initialization error", e)
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PAYMENT_REQUEST_CODE) {
            Log.d(TAG, "Payment activity result -> resultCode=$resultCode, data=$data")
            when (resultCode) {
                MastercardPaymentActivity.RESULT_PAYMENT_SUCCESS -> {
                    val checkoutOrderId = data?.getStringExtra("orderId") ?: orderId
                    val sessionId = data?.getStringExtra("sessionId").orEmpty()
                    val gatewayAmount = data?.getStringExtra("amount").orEmpty()
                    val transactionId = data?.getStringExtra("transactionId").orEmpty()
                    Log.d(
                        TAG,
                        "Gateway PAY approved. Starting backend checkout -> " +
                            "orderId=$checkoutOrderId, sessionId=$sessionId, amount=$gatewayAmount, " +
                            "transactionId=$transactionId"
                    )
                    PaymentLogger.d(
                        "PaymentScreen",
                        "Gateway PAY approved. Starting backend checkout -> " +
                            "orderId=$checkoutOrderId, sessionId=$sessionId, amount=$gatewayAmount, " +
                            "transactionId=$transactionId, paymentMethod=2"
                    )
                    startBackendCheckout("2", checkoutOrderId)
                }
                MastercardPaymentActivity.RESULT_PAYMENT_FAILED -> {
                    Log.w(TAG, "Gateway payment cancelled or failed")
                    Toast.makeText(this, getString(R.string.payment_failed), Toast.LENGTH_SHORT).show()
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

    private fun navigateToCartWithSuccess(message: String) {
        PaymentLogger.d("PaymentScreen", "Navigating to cart with success -> message=$message")
        val intent = Intent(this, CartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(CartActivity.EXTRA_CHECKOUT_SUCCESS, true)
            putExtra(CartActivity.EXTRA_SUCCESS_MESSAGE, message)
        }
        startActivity(intent)
        finish()
    }
    
    companion object {
        private const val TAG = "PaymentActivity***********"
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
            if (result == null || !isCheckoutPending) {
                return@observe
            }

            when (result) {
                is NetworkResults.Success -> {
                    val status = result.data.msg.status
                    val message = result.data.msg.message
                    Log.d(
                        TAG,
                        "Backend checkout response -> httpSuccess=true, status=$status, message=$message, " +
                            "isCompletingOnlineCheckout=$isCompletingOnlineCheckout"
                    )
                    PaymentLogger.d(
                        "BackendCheckout",
                        "Response -> status=$status, message=$message, online=$isCompletingOnlineCheckout"
                    )

                    hideProgressDialog()

                    val wasOnlineCheckout = isCompletingOnlineCheckout
                    isCheckoutPending = false

                    if (status == 200) {
                        isCompletingOnlineCheckout = false
                        if (wasOnlineCheckout) {
                            Log.d(TAG, "Online payment completed successfully after backend confirmation")
                            navigateToCartWithSuccess(getString(R.string.payment_success_message))
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }
                    } else {
                        if (wasOnlineCheckout) {
                            Log.e(
                                TAG,
                                "Gateway succeeded but backend checkout failed -> status=$status, message=$message"
                            )
                        }
                        isCompletingOnlineCheckout = false
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                    viewmodel.clearCheckoutResult()
                }

                is NetworkResults.Error -> {
                    hideProgressDialog()
                    isCheckoutPending = false
                    isCompletingOnlineCheckout = false
                    Log.e(TAG, "Backend checkout network error", result.exception)
                    Toast.makeText(
                        this,
                        getString(R.string.something_wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewmodel.clearCheckoutResult()
                }

                is NetworkResults.ErrorMessage -> {
                    hideProgressDialog()
                    isCheckoutPending = false
                    val message = result.data?.msg?.message.orEmpty()
                    val status = result.data?.msg?.status
                    Log.e(
                        TAG,
                        "Backend checkout error message -> status=$status, message=$message, " +
                            "isCompletingOnlineCheckout=$isCompletingOnlineCheckout"
                    )
                    if (isCompletingOnlineCheckout) {
                        Log.e(TAG, "Gateway succeeded but backend checkout returned error response")
                    }
                    isCompletingOnlineCheckout = false
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    viewmodel.clearCheckoutResult()
                }

                else -> {

                }
            }
        }
    }
}