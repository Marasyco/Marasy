package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivityNativeCardPaymentBinding
import com.blueray.marasy.helpers.CardInputFormatter
import com.blueray.marasy.helpers.PaymentLogger
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
        private const val STEP = "NativeCard"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeCardPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        amount = intent.getStringExtra(EXTRA_AMOUNT) ?: ""
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: ""

        PaymentLogger.d(
            STEP,
            "Started -> orderId=$orderId, amount=$amount JOD, sessionId=$sessionId, " +
                "parsedAmount=${MastercardPaymentService.parseAmount("$amount JD")}"
        )

        setupUI()
        setupCardInputFormatters()
        setupPayButton()
    }

    private fun setupUI() {
        binding.includedTab.title.text = getString(R.string.pay_online)
        binding.includedTab.backButton.setOnClickListener {
            PaymentLogger.w(STEP, "User cancelled payment from toolbar")
            setResult(RESULT_PAYMENT_FAILED)
            finish()
        }

        binding.amountTv.text = "$amount JD"
    }

    private fun setupCardInputFormatters() {
        binding.cardNumberEt.addTextChangedListener(
            CardInputFormatter.CardNumberTextWatcher(binding.cardNumberEt)
        )

        binding.expiryDateEt.addTextChangedListener(
            CardInputFormatter.ExpiryDateTextWatcher(binding.expiryDateEt)
        )

        binding.cvvEt.addTextChangedListener(
            CardInputFormatter.CvvTextWatcher(binding.cvvEt)
        )

        binding.cardNumberEt.doAfterTextChanged { text ->
            PaymentLogger.d(STEP, "Card type detected: ${CardInputFormatter.getCardType(text.toString())}")
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

        if (!CardInputFormatter.validateCardNumber(cardNumber)) {
            binding.cardNumberEt.error = getString(R.string.invalid_card_number)
            binding.cardNumberEt.requestFocus()
            return false
        }

        if (!CardInputFormatter.validateCardholderName(cardholderName)) {
            binding.cardholderNameEt.error = getString(R.string.invalid_cardholder_name)
            binding.cardholderNameEt.requestFocus()
            return false
        }

        if (!CardInputFormatter.validateExpiryDate(expiryDate)) {
            binding.expiryDateEt.error = getString(R.string.invalid_expiry_date)
            binding.expiryDateEt.requestFocus()
            return false
        }

        if (!CardInputFormatter.validateCvv(cvv)) {
            binding.cvvEt.error = getString(R.string.invalid_cvv)
            binding.cvvEt.requestFocus()
            return false
        }

        return true
    }

    private fun processPayment() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.payButton.isEnabled = false

        val cardNumber = CardInputFormatter.getCardNumberDigitsOnly(binding.cardNumberEt.text.toString())
        val cardholderName = binding.cardholderNameEt.text.toString().trim()
        val expiryMonth = CardInputFormatter.getExpiryMonth(binding.expiryDateEt.text.toString())
        val expiryYear = CardInputFormatter.getExpiryYear(binding.expiryDateEt.text.toString())
        val cvv = binding.cvvEt.text.toString()

        PaymentLogger.d(STEP, "Step 0 -> Starting gateway payment flow")
        PaymentLogger.d(STEP, "Step 0 -> Card=${cardNumber.take(4)}****${cardNumber.takeLast(4)}, expiry=$expiryMonth/$expiryYear")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                PaymentLogger.d(STEP, "Step 1 -> updateSession with orderId=$orderId, amount=$amount")
                val updateResult = withContext(Dispatchers.IO) {
                    MastercardPaymentService.updateSession(
                        sessionId = sessionId,
                        orderId = orderId,
                        amount = amount,
                        currency = "JOD"
                    )
                }

                if (updateResult.isFailure) {
                    val error = updateResult.exceptionOrNull()
                    PaymentLogger.e(STEP, "Step 1 FAILED -> updateSession", error)
                    throw error ?: Exception("Failed to update session")
                }

                val orderUpdate = updateResult.getOrNull()!!
                PaymentLogger.d(
                    STEP,
                    "Step 1 OK -> updateStatus=${orderUpdate.session.updateStatus}, " +
                        "gatewayAmount=${orderUpdate.order.amount}, gatewayOrderId=${orderUpdate.order.id}"
                )

                PaymentLogger.d(STEP, "Step 2 -> updateSessionWithCard")
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

                if (cardResult.isFailure) {
                    val error = cardResult.exceptionOrNull() ?: Exception("Unknown error")
                    PaymentLogger.e(STEP, "Step 2 FAILED -> updateSessionWithCard", error)
                    handlePaymentFailure(error.message ?: "Payment failed")
                    return@launch
                }

                val cardUpdate = cardResult.getOrNull()!!
                PaymentLogger.d(
                    STEP,
                    "Step 2 OK -> updateStatus=${cardUpdate.session.updateStatus}, sessionId=${cardUpdate.session.id}"
                )
                PaymentLogger.w(
                    STEP,
                    "Steps 1-2 only store card on gateway session. Card is NOT charged yet until PAY runs."
                )

                PaymentLogger.d(STEP, "Step 3 -> retrieveSession")
                val retrieveResult = withContext(Dispatchers.IO) {
                    MastercardPaymentService.retrieveSession(sessionId)
                }
                if (retrieveResult.isSuccess) {
                    PaymentLogger.d(STEP, "Step 3 OK -> session snapshot received")
                } else {
                    PaymentLogger.w(
                        STEP,
                        "Step 3 WARN -> retrieveSession failed: ${retrieveResult.exceptionOrNull()?.message}"
                    )
                }

                PaymentLogger.d(STEP, "Step 4 -> payWithSession (actual charge attempt)")
                val payResult = withContext(Dispatchers.IO) {
                    MastercardPaymentService.payWithSession(
                        orderId = orderId,
                        sessionId = sessionId,
                        amount = amount,
                        currency = "JOD"
                    )
                }

                if (payResult.isSuccess) {
                    val pay = payResult.getOrNull()!!
                    PaymentLogger.d(
                        STEP,
                        "Step 4 OK -> PAY APPROVED, transactionId=${pay.transactionId}, " +
                            "authorizationCode=${pay.authorizationCode}, orderStatus=${pay.orderStatus}"
                    )
                    returnGatewaySuccess(pay.transactionId.orEmpty())
                } else {
                    val error = payResult.exceptionOrNull() ?: Exception("PAY failed")
                    PaymentLogger.e(STEP, "Step 4 FAILED -> payWithSession", error)
                    val message = when (error) {
                        is MastercardPaymentService.ThreeDSRequiredException ->
                            getString(R.string.payment_3ds_required)
                        else -> error.message ?: getString(R.string.payment_failed)
                    }
                    handlePaymentFailure(message)
                }
            } catch (e: Exception) {
                PaymentLogger.e(STEP, "Payment flow error", e)
                handlePaymentFailure(e.message ?: "An error occurred")
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.payButton.isEnabled = true
            }
        }
    }

    private fun returnGatewaySuccess(transactionId: String) {
        val resultIntent = Intent().apply {
            putExtra("orderId", orderId)
            putExtra("paymentSuccess", true)
            putExtra("sessionId", sessionId)
            putExtra("amount", amount)
            putExtra("transactionId", transactionId)
        }

        PaymentLogger.d(
            STEP,
            "Finishing RESULT_PAYMENT_SUCCESS -> orderId=$orderId, sessionId=$sessionId, transactionId=$transactionId"
        )
        setResult(RESULT_PAYMENT_SUCCESS, resultIntent)
        finish()
    }

    private fun handlePaymentFailure(errorMessage: String) {
        PaymentLogger.e(STEP, "Showing payment failure to user: $errorMessage")
        Toast.makeText(
            this,
            "${getString(R.string.payment_failed)}\n$errorMessage",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        PaymentLogger.w(STEP, "User pressed back")
        setResult(RESULT_PAYMENT_FAILED)
    }
}
