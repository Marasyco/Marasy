package com.blueray.marasy.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
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
import com.blueray.marasy.databinding.ActivityMastercardPaymentBinding
import com.blueray.marasy.services.MastercardPaymentService

class MastercardPaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMastercardPaymentBinding
    
    private var orderId = ""
    private var amount = ""
    private var sessionId = ""
    
    companion object {
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_SESSION_ID = "session_id"
        const val RESULT_PAYMENT_SUCCESS = 1
        const val RESULT_PAYMENT_FAILED = 2
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
        
        binding.includedTab.title.text = "Payment"
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
        
        // Add JavaScript interface to handle callbacks from checkout.js
        binding.webView.addJavascriptInterface(CheckoutCallbackInterface(), "AndroidCallback")
        
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
                Log.d("PaymentWebView", "Page started loading: $url")
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                
                // Check if payment was successful by examining URL
                url?.let {
                    Log.d("PaymentWebView", "Page finished loading: $it")
                    // Also check for success indicators in the page content
                    view?.evaluateJavascript(
                        "(function() { " +
                        "  var url = window.location.href; " +
                        "  if (url.indexOf('success') !== -1 || url.indexOf('SUCCESS') !== -1) { " +
                        "    if (window.AndroidCallback) window.AndroidCallback.onPaymentSuccess('success'); " +
                        "    return 'success'; " +
                        "  } " +
                        "  return null; " +
                        "})();",
                        null
                    )
                    checkPaymentStatus(it)
                }
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                binding.progressBar.visibility = View.GONE
                
                val errorCode = error?.errorCode ?: -1
                val errorDescription = error?.description?.toString() ?: "Unknown error"
                val url = request?.url?.toString() ?: "Unknown URL"
                
                Log.e("PaymentWebView", "Error loading page: $url - Code: $errorCode - $errorDescription")
                
                when (errorCode) {
                    WebViewClient.ERROR_HOST_LOOKUP,
                    WebViewClient.ERROR_CONNECT,
                    WebViewClient.ERROR_TIMEOUT -> {
                        Toast.makeText(
                            this@MastercardPaymentActivity,
                            "Network error. Please check your internet connection.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    WebViewClient.ERROR_BAD_URL,
                    WebViewClient.ERROR_FILE_NOT_FOUND -> {
                        Toast.makeText(
                            this@MastercardPaymentActivity,
                            "Payment gateway URL not found (404). Please contact support.",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e("PaymentWebView", "404 Error - URL might be incorrect: $url")
                    }
                    else -> {
                        Toast.makeText(
                            this@MastercardPaymentActivity,
                            "Error loading payment page: $errorDescription",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            
            @Suppress("DEPRECATION")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                binding.progressBar.visibility = View.GONE
                
                Log.e("PaymentWebView", "Error (deprecated): $failingUrl - Code: $errorCode - $description")
                
                if (errorCode == WebViewClient.ERROR_FILE_NOT_FOUND || errorCode == WebViewClient.ERROR_BAD_URL) {
                    Toast.makeText(
                        this@MastercardPaymentActivity,
                        "Payment gateway URL not found (404). Please contact support.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            
            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    Log.d("PaymentWebView", "shouldOverrideUrlLoading (deprecated): $it")
                    return checkPaymentStatus(it)
                }
                return false
            }
            
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request?.url?.toString()?.let {
                        Log.d("PaymentWebView", "shouldOverrideUrlLoading: $it")
                        return checkPaymentStatus(it)
                    }
                }
                return false
            }
        }
    }
    
    private fun checkPaymentStatus(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return when {
            lowerUrl.contains("success") || lowerUrl.contains("approved") || 
            lowerUrl.contains("complete") || lowerUrl.contains("result=success") -> {
                handlePaymentSuccess()
                true
            }
            lowerUrl.contains("fail") || lowerUrl.contains("declined") || 
            lowerUrl.contains("cancel") || lowerUrl.contains("error") || 
            lowerUrl.contains("result=fail") || lowerUrl.contains("result=cancel") -> {
                handlePaymentFailure()
                true
            }
            else -> false
        }
    }
    
    private fun loadPaymentPage() {
        binding.progressBar.visibility = View.VISIBLE
        
        // Create HTML page with Mastercard checkout.js library
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="https://test-network.mtf.gateway.mastercard.com/static/checkout/checkout.min.js" 
                        data-afterRedirect="Checkout.restoreFormFields" 
                        data-error="errorCallback" 
                        data-cancel="cancelCallback"></script>
                <script type="text/javascript">
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
                    
                    // Configure checkout when page loads
                    window.onload = function() {
                        try {
                            Checkout.configure({
                                session: {
                                    id: '$sessionId'
                                }
                            });
                            
                            // Automatically show payment page
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
        
        Log.d("PaymentWebView", "Loading checkout page with session ID: $sessionId")
        binding.webView.loadDataWithBaseURL(
            "https://test-network.mtf.gateway.mastercard.com",
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }
    
    /**
     * JavaScript interface to handle callbacks from checkout.js
     */
    inner class CheckoutCallbackInterface {
        @JavascriptInterface
        fun onPaymentSuccess(result: String) {
            Log.d("PaymentWebView", "Payment success callback: $result")
            runOnUiThread {
                handlePaymentSuccess()
            }
        }
        
        @JavascriptInterface
        fun onPaymentError(error: String) {
            Log.e("PaymentWebView", "Payment error callback: $error")
            runOnUiThread {
                Toast.makeText(this@MastercardPaymentActivity, "Payment error: $error", Toast.LENGTH_LONG).show()
                handlePaymentFailure()
            }
        }
        
        @JavascriptInterface
        fun onPaymentCancel() {
            Log.d("PaymentWebView", "Payment cancelled callback")
            runOnUiThread {
                handlePaymentFailure()
            }
        }
    }
    
    private fun handlePaymentSuccess() {
        Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show()
        val resultIntent = Intent().apply {
            putExtra("orderId", orderId)
            putExtra("paymentSuccess", true)
        }
        setResult(RESULT_PAYMENT_SUCCESS, resultIntent)
        finish()
    }
    
    private fun handlePaymentFailure() {
        Toast.makeText(this, "Payment failed or cancelled", Toast.LENGTH_SHORT).show()
        setResult(RESULT_PAYMENT_FAILED)
        finish()
    }
}




