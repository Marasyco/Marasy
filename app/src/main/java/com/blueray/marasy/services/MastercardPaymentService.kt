package com.blueray.marasy.services

import android.util.Log
import com.blueray.marasy.api.MastercardApiClient
import com.blueray.marasy.model.MastercardCard
import com.blueray.marasy.model.MastercardCardExpiry
import com.blueray.marasy.model.MastercardCardUpdateRequest
import com.blueray.marasy.model.MastercardInitiateCheckoutRequest
import com.blueray.marasy.model.MastercardInteraction
import com.blueray.marasy.model.MastercardMerchant
import com.blueray.marasy.model.MastercardOrder
import com.blueray.marasy.model.MastercardProvidedCard
import com.blueray.marasy.model.MastercardSessionResponse
import com.blueray.marasy.model.MastercardSourceOfFunds
import com.blueray.marasy.model.MastercardUpdateSessionRequest
import com.blueray.marasy.model.MastercardUpdateSessionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MastercardPaymentService {
    private const val TAG = "MastercardPayment"
    private const val MERCHANT_ID = "test12122024"
    
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
                    Log.d(TAG, "Checkout initiated: ${response.body()!!.session.id}")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to initiate checkout: $errorBody")
                    Result.failure(Exception("Failed to initiate checkout: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initiating checkout", e)
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
                    Log.d(TAG, "Session created: ${response.body()!!.session.id}")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to create session: $errorBody")
                    Result.failure(Exception("Failed to create session: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating session", e)
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
    ): Result<String> {
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
                
                Log.d(TAG, "Updating session $sessionId with card data")
                
                // Make API call to update session with card data
                val response = MastercardApiClient.api.updateSessionWithCard(
                    MERCHANT_ID,
                    sessionId,
                    cardRequest
                )
                
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "Session updated successfully with card data")
                    Result.success("Success")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to update session with card: $errorBody")
                    Result.failure(Exception("Failed to update session: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating session with card", e)
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
                
                val response = MastercardApiClient.api.updateSession(
                    MERCHANT_ID,
                    sessionId,
                    request
                )
                
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "Session updated: ${response.body()!!.session.updateStatus}")
                    Result.success(response.body()!!)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to update session: $errorBody")
                    Result.failure(Exception("Failed to update session: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating session", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Extracts numeric value from price string (e.g., "5.00 JD" -> "5.00")
     */
    fun extractAmount(priceString: String): String {
        return priceString.replace(" JD", "")
            .replace(" ", "")
            .trim()
    }
}




