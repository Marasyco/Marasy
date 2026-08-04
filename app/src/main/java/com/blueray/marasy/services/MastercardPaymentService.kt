package com.blueray.marasy.services

import com.blueray.marasy.api.MastercardApiClient
import com.blueray.marasy.helpers.PaymentLogger
import com.blueray.marasy.model.MastercardCard
import com.blueray.marasy.model.MastercardCardExpiry
import com.blueray.marasy.model.MastercardCardUpdateRequest
import com.blueray.marasy.model.MastercardInitiateCheckoutRequest
import com.blueray.marasy.model.MastercardInteraction
import com.blueray.marasy.model.MastercardMerchant
import com.blueray.marasy.model.MastercardOrder
import com.blueray.marasy.model.MastercardPayRequest
import com.blueray.marasy.model.MastercardPaySessionReference
import com.blueray.marasy.model.MastercardPaySourceOfFunds
import com.blueray.marasy.model.MastercardProvidedCard
import com.blueray.marasy.model.MastercardSessionResponse
import com.blueray.marasy.model.MastercardSourceOfFunds
import com.blueray.marasy.model.MastercardUpdateSessionRequest
import com.blueray.marasy.model.MastercardUpdateSessionResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MastercardPaymentService {
    private const val STEP = "GatewayService"
    private val MERCHANT_ID = MastercardApiClient.MERCHANT_ID
    private val gson = Gson()

    data class GatewayPayResult(
        val rawBody: String,
        val result: String?,
        val gatewayCode: String?,
        val gatewayRecommendation: String?,
        val authenticationStatus: String?,
        val orderStatus: String?,
        val transactionId: String?,
        val authorizationCode: String?,
        val acquirerMessage: String?
    ) {
        val isApproved: Boolean
            get() = result.equals("SUCCESS", ignoreCase = true) &&
                gatewayCode.equals("APPROVED", ignoreCase = true)

        val isBlockedByThreeDS: Boolean
            get() = gatewayCode.equals("BLOCKED", ignoreCase = true) &&
                (
                    authenticationStatus.equals("AUTHENTICATION_NOT_IN_EFFECT", ignoreCase = true) ||
                        rawBody.contains("MERCHANT_3D_SECURE") ||
                        rawBody.contains("NO_LIABILITY_SHIFT")
                    )
    }

    private fun isSuccessfulUpdateStatus(status: String?): Boolean {
        return status.equals("SUCCESS", ignoreCase = true)
    }

    private fun readResponseBody(response: retrofit2.Response<okhttp3.ResponseBody>): String {
        val successBody = response.body()?.string().orEmpty()
        if (successBody.isNotBlank()) {
            return successBody
        }
        return response.errorBody()?.string().orEmpty()
    }

    private fun parsePayResult(rawBody: String): GatewayPayResult {
        if (rawBody.isBlank()) {
            PaymentLogger.w(STEP, "PAY response body is empty")
            return emptyPayResult(rawBody)
        }
        return try {
            val json = gson.fromJson(rawBody, JsonObject::class.java) ?: return emptyPayResult(rawBody)
            val responseObject = json.get("response")?.takeIf { it.isJsonObject }?.asJsonObject
            val orderObject = json.get("order")?.takeIf { it.isJsonObject }?.asJsonObject
            val transactionObject = json.get("transaction")?.takeIf { it.isJsonObject }?.asJsonObject
            val errorObject = json.get("error")?.takeIf { it.isJsonObject }?.asJsonObject
            GatewayPayResult(
                rawBody = rawBody,
                result = json.get("result")?.takeIf { !it.isJsonNull }?.asString,
                gatewayCode = responseObject?.get("gatewayCode")?.takeIf { !it.isJsonNull }?.asString,
                gatewayRecommendation = responseObject?.get("gatewayRecommendation")?.takeIf { !it.isJsonNull }?.asString,
                authenticationStatus = orderObject?.get("authenticationStatus")?.takeIf { !it.isJsonNull }?.asString
                    ?: transactionObject?.get("authenticationStatus")?.takeIf { !it.isJsonNull }?.asString,
                orderStatus = orderObject?.get("status")?.takeIf { !it.isJsonNull }?.asString,
                transactionId = transactionObject?.get("id")?.takeIf { !it.isJsonNull }?.asString,
                authorizationCode = transactionObject?.get("authorizationCode")?.takeIf { !it.isJsonNull }?.asString,
                acquirerMessage = responseObject?.get("acquirerMessage")?.takeIf { !it.isJsonNull }?.asString
                    ?: errorObject?.get("explanation")?.takeIf { !it.isJsonNull }?.asString
                    ?: errorObject?.get("cause")?.takeIf { !it.isJsonNull }?.asString
            )
        } catch (e: Exception) {
            PaymentLogger.e(STEP, "Failed to parse PAY response: $rawBody", e)
            emptyPayResult(rawBody)
        }
    }

    private fun emptyPayResult(rawBody: String) = GatewayPayResult(
        rawBody = rawBody,
        result = null,
        gatewayCode = null,
        gatewayRecommendation = null,
        authenticationStatus = null,
        orderStatus = null,
        transactionId = null,
        authorizationCode = null,
        acquirerMessage = null
    )

    private fun logSessionResponse(step: String, response: MastercardUpdateSessionResponse) {
        PaymentLogger.d(
            STEP,
            "$step -> sessionId=${response.session.id}, updateStatus=${response.session.updateStatus}, " +
                "orderId=${response.order.id}, amount=${response.order.amount}, currency=${response.order.currency}"
        )
    }
    
    /**
     * Initiates checkout with order details (creates session and configures it in one call)
     */
    suspend fun initiateCheckout(
        orderId: String,
        amount: String,
        currency: String = "JOD",
        description: String = "Order payment"
    ): Result<MastercardSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = MastercardInitiateCheckoutRequest(
                    apiOperation = "INITIATE_CHECKOUT",
                    interaction = MastercardInteraction(
                        operation = "PURCHASE",
                        merchant = MastercardMerchant(name = "Network")
                    ),
                    order = MastercardOrder(
                        id = orderId,
                        currency = currency,
                        amount = amount,
                        reference = orderId,
                        description = description
                    )
                )
                
                val response = MastercardApiClient.api.initiateCheckout(MERCHANT_ID, request)
                if (response.isSuccessful && response.body() != null) {
                    PaymentLogger.d(STEP, "initiateCheckout -> sessionId=${response.body()!!.session.id}")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    PaymentLogger.logGatewayResponse("initiateCheckout", response.code(), errorBody)
                    Result.failure(Exception("Failed to initiate checkout: $errorBody"))
                }
            } catch (e: Exception) {
                PaymentLogger.e(STEP, "Error initiating checkout", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Creates a new payment session for native card collection
     */
    suspend fun createSession(): Result<MastercardSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = MastercardApiClient.api.createSession(MERCHANT_ID)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    PaymentLogger.d(
                        STEP,
                        "createSession -> http=${response.code()}, sessionId=${body.session.id}, " +
                            "result=${body.result}, updateStatus=${body.session.updateStatus}"
                    )
                    Result.success(body)
                } else {
                    val errorBody = response.errorBody()?.string()
                    PaymentLogger.logGatewayResponse("createSession", response.code(), errorBody)
                    Result.failure(Exception("Failed to create session: $errorBody"))
                }
            } catch (e: Exception) {
                PaymentLogger.e(STEP, "Error creating session", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Updates the session with card details using REST API
     */
    suspend fun updateSessionWithCard(
        sessionId: String,
        cardNumber: String,
        cardholderName: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String
    ): Result<MastercardUpdateSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Build the request with proper data classes
                val cardRequest = MastercardCardUpdateRequest(
                    sourceOfFunds = MastercardSourceOfFunds(
                        provided = MastercardProvidedCard(
                            card = MastercardCard(
                                number = cardNumber,
                                nameOnCard = cardholderName,
                                securityCode = cvv,
                                expiry = MastercardCardExpiry(
                                    month = expiryMonth,
                                    year = expiryYear
                                )
                            )
                        )
                    )
                )
                
                PaymentLogger.d(STEP, "updateSessionWithCard -> sessionId=$sessionId")
                
                val response = MastercardApiClient.api.updateSessionWithCard(
                    MERCHANT_ID,
                    sessionId,
                    cardRequest
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    logSessionResponse("updateSessionWithCard", body)
                    if (isSuccessfulUpdateStatus(body.session.updateStatus)) {
                        Result.success(body)
                    } else {
                        PaymentLogger.e(
                            STEP,
                            "Card session update returned non-success status: ${body.session.updateStatus}"
                        )
                        Result.failure(
                            Exception("Gateway rejected card update: ${body.session.updateStatus}")
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    PaymentLogger.logGatewayResponse("updateSessionWithCard", response.code(), errorBody)
                    Result.failure(Exception("Failed to update session: $errorBody"))
                }
            } catch (e: Exception) {
                PaymentLogger.e(STEP, "Error updating session with card", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Updates the session with order details
     */
    suspend fun updateSession(
        sessionId: String,
        orderId: String,
        amount: String,
        currency: String = "JOD"
    ): Result<MastercardUpdateSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = MastercardUpdateSessionRequest(
                    order = MastercardOrder(
                        id = orderId,
                        currency = currency,
                        amount = amount
                    )
                )
                
                PaymentLogger.d(
                    STEP,
                    "updateSession -> sessionId=$sessionId, orderId=$orderId, amount=$amount, currency=$currency"
                )

                val response = MastercardApiClient.api.updateSession(
                    MERCHANT_ID,
                    sessionId,
                    request
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    logSessionResponse("updateSession", body)
                    if (isSuccessfulUpdateStatus(body.session.updateStatus)) {
                        Result.success(body)
                    } else {
                        PaymentLogger.e(
                            STEP,
                            "Order session update returned non-success status: ${body.session.updateStatus}"
                        )
                        Result.failure(
                            Exception("Gateway rejected order update: ${body.session.updateStatus}")
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    PaymentLogger.logGatewayResponse("updateSession", response.code(), errorBody)
                    Result.failure(Exception("Failed to update session: $errorBody"))
                }
            } catch (e: Exception) {
                PaymentLogger.e(STEP, "Error updating session", e)
                Result.failure(e)
            }
        }
    }

    suspend fun retrieveSession(sessionId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                PaymentLogger.d(STEP, "retrieveSession -> sessionId=$sessionId")
                val response = MastercardApiClient.api.retrieveSession(MERCHANT_ID, sessionId)
                val body = readResponseBody(response)
                PaymentLogger.logGatewayResponse("retrieveSession", response.code(), body)
                if (response.isSuccessful && body.isNotBlank()) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Failed to retrieve session: $body"))
                }
            } catch (e: Exception) {
                PaymentLogger.e(STEP, "Error retrieving session", e)
                Result.failure(e)
            }
        }
    }

    suspend fun payWithSession(
        orderId: String,
        sessionId: String,
        amount: String,
        currency: String = "JOD",
        transactionId: String = System.currentTimeMillis().toString()
    ): Result<GatewayPayResult> {
        return withContext(Dispatchers.IO) {
            try {
                val request = MastercardPayRequest(
                    session = MastercardPaySessionReference(id = sessionId),
                    sourceOfFunds = MastercardPaySourceOfFunds(type = "CARD")
                )
                PaymentLogger.d(
                    STEP,
                    "payWithSession PUT -> orderId=$orderId (URL only), sessionId=$sessionId, " +
                        "transactionId=$transactionId, sourceOfFunds.type=CARD, " +
                        "sessionAmount=$amount, sessionCurrency=$currency"
                )
                val response = MastercardApiClient.api.payWithSession(
                    MERCHANT_ID,
                    orderId,
                    transactionId,
                    request
                )
                val body = readResponseBody(response)
                PaymentLogger.logGatewayResponse("payWithSession", response.code(), body)
                val payResult = parsePayResult(body)
                PaymentLogger.d(
                    STEP,
                    "payWithSession parsed -> http=${response.code()}, result=${payResult.result}, " +
                        "gatewayCode=${payResult.gatewayCode}, gatewayRecommendation=${payResult.gatewayRecommendation}, " +
                        "authenticationStatus=${payResult.authenticationStatus}, orderStatus=${payResult.orderStatus}, " +
                        "transactionId=${payResult.transactionId}, authorizationCode=${payResult.authorizationCode}, " +
                        "acquirerMessage=${payResult.acquirerMessage}, isApproved=${payResult.isApproved}, " +
                        "isBlockedByThreeDS=${payResult.isBlockedByThreeDS}"
                )
                if (response.isSuccessful && payResult.isApproved) {
                    Result.success(payResult)
                } else {
                    val failure = when {
                        payResult.isBlockedByThreeDS -> ThreeDSRequiredException(payResult)
                        else -> Exception(
                            "PAY not approved (http=${response.code()}) -> result=${payResult.result}, " +
                                "gatewayCode=${payResult.gatewayCode}, orderStatus=${payResult.orderStatus}, " +
                                "recommendation=${payResult.gatewayRecommendation}, body=$body"
                        )
                    }
                    Result.failure(failure)
                }
            } catch (e: Exception) {
                PaymentLogger.e(STEP, "Error executing PAY", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Extracts numeric value from price string (e.g., "5.00 JD" -> "5.00")
     */
    fun extractAmount(priceString: String): String {
        return priceString.replace(" JD", "", ignoreCase = true)
            .replace(" jd", "", ignoreCase = true)
            .replace(" د.أ", "")
            .replace(" دينار", "")
            .replace(" ", "")
            .trim()
    }

    fun parseAmount(priceString: String): Double? {
        return extractAmount(priceString).replace(",", ".").toDoubleOrNull()
    }

    const val MIN_ONLINE_PAYMENT_JOD = 1.0

    class ThreeDSRequiredException(val payResult: GatewayPayResult) :
        Exception("3D Secure authentication required")
}


