package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityNativeCardPaymentBinding
import com.blueray.marasy.helpers.CardInputFormatter
import com.blueray.marasy.services.MastercardPaymentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NativeCardPaymentActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNativeCardPaymentBinding
    
    private var orderId = ""
    private var amount = ""
    private var sessionId = ""
    
    companion object {
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_SESSION_ID = "session_id"
        const val RESULT_PAYMENT_SUCCESS = 1
        const val RESULT_PAYMENT_FAILED = 2
        private const val TAG = "NativeCardPayment"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeCardPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get data from intent
        orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        amount = intent.getStringExtra(EXTRA_AMOUNT) ?: ""
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: ""
        
        Log.d(TAG, "Payment activity started - Order: $orderId, Amount: $amount, Session: $sessionId")
        
        setupUI()
        setupCardInputFormatters()
        setupPayButton()
    }
    
    private fun setupUI() {
        // Set toolbar title
        binding.includedTab.title.text = getString(R.string.pay_online)
        binding.includedTab.backButton.setOnClickListener {
            setResult(RESULT_PAYMENT_FAILED)
            finish()
        }
        
        // Display amount
        binding.amountTv.text = "$amount JD"
    }
    
    private fun setupCardInputFormatters() {
        // Card number formatter
        binding.cardNumberEt.addTextChangedListener(
            CardInputFormatter.CardNumberTextWatcher(binding.cardNumberEt)
        )
        
        // Expiry date formatter
        binding.expiryDateEt.addTextChangedListener(
            CardInputFormatter.ExpiryDateTextWatcher(binding.expiryDateEt)
        )
        
        // CVV formatter
        binding.cvvEt.addTextChangedListener(
            CardInputFormatter.CvvTextWatcher(binding.cvvEt)
        )
        
        // Show card type icon when typing
        binding.cardNumberEt.doAfterTextChanged { text ->
            val cardType = CardInputFormatter.getCardType(text.toString())
            // You can update the card type icon here based on the card type
            // For now, we'll just log it
            Log.d(TAG, "Card type detected: $cardType")
        }
    }
    
    private fun setupPayButton() {
        binding.payButton.setOnClickListener {
            if (validateInputs()) {
                processPayment()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val cardNumber = binding.cardNumberEt.text.toString()
        val cardholderName = binding.cardholderNameEt.text.toString()
        val expiryDate = binding.expiryDateEt.text.toString()
        val cvv = binding.cvvEt.text.toString()
        
        // Validate card number
        if (!CardInputFormatter.validateCardNumber(cardNumber)) {
            binding.cardNumberEt.error = getString(R.string.invalid_card_number)
            binding.cardNumberEt.requestFocus()
            return false
        }
        
        // Validate cardholder name
        if (!CardInputFormatter.validateCardholderName(cardholderName)) {
            binding.cardholderNameEt.error = getString(R.string.invalid_cardholder_name)
            binding.cardholderNameEt.requestFocus()
            return false
        }
        
        // Validate expiry date
        if (!CardInputFormatter.validateExpiryDate(expiryDate)) {
            binding.expiryDateEt.error = getString(R.string.invalid_expiry_date)
            binding.expiryDateEt.requestFocus()
            return false
        }
        
        // Validate CVV
        if (!CardInputFormatter.validateCvv(cvv)) {
            binding.cvvEt.error = getString(R.string.invalid_cvv)
            binding.cvvEt.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun processPayment() {
        // Show loading
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.payButton.isEnabled = false
        
        val cardNumber = CardInputFormatter.getCardNumberDigitsOnly(binding.cardNumberEt.text.toString())
        val cardholderName = binding.cardholderNameEt.text.toString().trim()
        val expiryMonth = CardInputFormatter.getExpiryMonth(binding.expiryDateEt.text.toString())
        val expiryYear = CardInputFormatter.getExpiryYear(binding.expiryDateEt.text.toString())
        val cvv = binding.cvvEt.text.toString()
        
        Log.d(TAG, "Processing payment...")
        Log.d(TAG, "Card: ${cardNumber.take(4)}****${cardNumber.takeLast(4)}")
        Log.d(TAG, "Expiry: $expiryMonth/$expiryYear")
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Step 1: Update session with order details
                val updateResult = withContext(Dispatchers.IO) {
                    MastercardPaymentService.updateSession(
                        sessionId = sessionId,
                        orderId = orderId,
                        amount = amount,
                        currency = "JOD"
                    )
                }
                
                if (updateResult.isFailure) {
                    throw updateResult.exceptionOrNull() ?: Exception("Failed to update session")
                }
                
                Log.d(TAG, "Session updated with order details")
                
                // Step 2: Update session with card details using REST API
                val cardResult = withContext(Dispatchers.IO) {
                    MastercardPaymentService.updateSessionWithCard(
                        sessionId = sessionId,
                        cardNumber = cardNumber,
                        cardholderName = cardholderName,
                        expiryMonth = expiryMonth,
                        expiryYear = expiryYear,
                        cvv = cvv
                    )
                }
                
                if (cardResult.isSuccess) {
                    Log.d(TAG, "Card details securely submitted to session")
                    handlePaymentSuccess()
                } else {
                    val error = cardResult.exceptionOrNull() ?: Exception("Unknown error")
                    Log.e(TAG, "Failed to process payment", error)
                    handlePaymentFailure(error.message ?: "Payment failed")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Payment error", e)
                handlePaymentFailure(e.message ?: "An error occurred")
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.payButton.isEnabled = true
            }
        }
    }
    
    private fun handlePaymentSuccess() {
        Toast.makeText(this, getString(R.string.payment_successful), Toast.LENGTH_SHORT).show()
        
        val resultIntent = Intent().apply {
            putExtra("orderId", orderId)
            putExtra("paymentSuccess", true)
            putExtra("sessionId", sessionId)
        }
        
        setResult(RESULT_PAYMENT_SUCCESS, resultIntent)
        finish()
    }
    
    private fun handlePaymentFailure(errorMessage: String) {
        Toast.makeText(
            this,
            "${getString(R.string.payment_failed)}\n$errorMessage",
            Toast.LENGTH_LONG
        ).show()
        
        // Don't finish activity, allow user to try again
        Log.e(TAG, "Payment failed: $errorMessage")
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_PAYMENT_FAILED)
    }
}
