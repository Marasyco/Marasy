package com.blueray.marasy.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.blueray.marasy.R
import com.blueray.marasy.api.MastercardApiClient
import com.blueray.marasy.databinding.ActivityMastercardPaymentBinding
import com.blueray.marasy.helpers.PaymentLogger
import org.json.JSONObject

class MastercardPaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMastercardPaymentBinding

    private var orderId = ""
    private var amount = ""
    private var sessionId = ""
    private var paymentHandled = false

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_SESSION_ID = "session_id"
        const val RESULT_PAYMENT_SUCCESS = 1
        const val RESULT_PAYMENT_FAILED = 2
        private const val TAG = "HostedCheckout"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMastercardPaymentBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        amount = intent.getStringExtra(EXTRA_AMOUNT) ?: ""
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: ""

        PaymentLogger.d(TAG, "Started -> orderId=$orderId, amount=$amount, sessionId=$sessionId")

        binding.includedTab.title.text = getString(R.string.pay_online)
        binding.includedTab.backButton.setOnClickListener {
            setResult(RESULT_PAYMENT_FAILED)
            finish()
        }

        setupWebView()
        loadPaymentPage()
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
        }

        binding.webView.addJavascriptInterface(CheckoutCallbackInterface(), "AndroidCallback")

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
                Log.d(TAG, "Page started: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "Page finished: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "WebView error: ${request?.url} -> ${error?.description}")
            }
        }
    }

    private fun loadPaymentPage() {
        binding.progressBar.visibility = View.VISIBLE

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="${MastercardApiClient.GATEWAY_HOST}/static/checkout/checkout.min.js"
                        data-afterRedirect="Checkout.restoreFormFields"
                        data-complete="completeCallback"
                        data-error="errorCallback"
                        data-cancel="cancelCallback"></script>
                <script type="text/javascript">
                    function completeCallback(response) {
                        console.log('Payment complete: ' + JSON.stringify(response));
                        if (window.AndroidCallback) {
                            window.AndroidCallback.onPaymentComplete(JSON.stringify(response));
                        }
                    }

                    function errorCallback(error) {
                        console.log('Payment error: ' + JSON.stringify(error));
                        if (window.AndroidCallback) {
                            window.AndroidCallback.onPaymentError(JSON.stringify(error));
                        }
                    }

                    function cancelCallback() {
                        console.log('Payment cancelled');
                        if (window.AndroidCallback) {
                            window.AndroidCallback.onPaymentCancel();
                        }
                    }

                    function restoreFormFields() {
                        console.log('Form fields restored');
                    }

                    window.onload = function() {
                        try {
                            Checkout.configure({
                                session: {
                                    id: '$sessionId'
                                }
                            });
                            Checkout.showPaymentPage();
                        } catch (e) {
                            console.error('Error configuring checkout: ' + e);
                            if (window.AndroidCallback) {
                                window.AndroidCallback.onPaymentError('Failed to initialize payment: ' + e.toString());
                            }
                        }
                    };
                </script>
            </head>
            <body>
                <div id="embed-target"></div>
            </body>
            </html>
        """.trimIndent()

        Log.d(TAG, "Loading checkout page with sessionId=$sessionId")
        binding.webView.loadDataWithBaseURL(
            MastercardApiClient.GATEWAY_HOST,
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    inner class CheckoutCallbackInterface {
        @JavascriptInterface
        fun onPaymentComplete(result: String) {
            PaymentLogger.d(TAG, "Payment complete callback: $result")
            runOnUiThread {
                if (isGatewayPaymentApproved(result)) {
                    handlePaymentSuccess()
                } else {
                    PaymentLogger.w(TAG, "Ignoring non-approved complete callback: $result")
                }
            }
        }

        @JavascriptInterface
        fun onPaymentError(error: String) {
            PaymentLogger.e(TAG, "Payment error callback: $error")
            runOnUiThread {
                Toast.makeText(
                    this@MastercardPaymentActivity,
                    getString(R.string.payment_failed),
                    Toast.LENGTH_LONG
                ).show()
                handlePaymentFailure()
            }
        }

        @JavascriptInterface
        fun onPaymentCancel() {
            PaymentLogger.w(TAG, "Payment cancelled callback")
            runOnUiThread {
                handlePaymentFailure()
            }
        }
    }

    private fun isGatewayPaymentApproved(resultJson: String): Boolean {
        if (resultJson.isBlank()) return false
        return try {
            val json = JSONObject(resultJson)
            val result = json.optString("result", "")
            val gatewayCode = json.optJSONObject("response")?.optString("gatewayCode", "").orEmpty()
            val approved = result.equals("SUCCESS", ignoreCase = true) ||
                gatewayCode.equals("APPROVED", ignoreCase = true)
            PaymentLogger.d(
                TAG,
                "Parsed complete callback -> result=$result, gatewayCode=$gatewayCode, approved=$approved"
            )
            approved
        } catch (e: Exception) {
            val approved = resultJson.contains("\"result\":\"SUCCESS\"", ignoreCase = true) ||
                resultJson.contains("\"gatewayCode\":\"APPROVED\"", ignoreCase = true)
            PaymentLogger.d(TAG, "Fallback parse complete callback -> approved=$approved")
            approved
        }
    }

    private fun handlePaymentSuccess() {
        if (paymentHandled) return
        paymentHandled = true

        PaymentLogger.d(TAG, "Returning success -> orderId=$orderId, sessionId=$sessionId")
        val resultIntent = Intent().apply {
            putExtra("orderId", orderId)
            putExtra("sessionId", sessionId)
            putExtra("amount", amount)
            putExtra("paymentSuccess", true)
        }
        setResult(RESULT_PAYMENT_SUCCESS, resultIntent)
        finish()
    }

    private fun handlePaymentFailure() {
        if (paymentHandled) return
        paymentHandled = true

        PaymentLogger.w(TAG, "Returning failure -> orderId=$orderId")
        setResult(RESULT_PAYMENT_FAILED)
        finish()
    }
}
