package com.blueray.marasy.api

import com.blueray.marasy.model.MastercardCardUpdateRequest
import com.blueray.marasy.model.MastercardInitiateCheckoutRequest
import com.blueray.marasy.model.MastercardPayRequest
import com.blueray.marasy.model.MastercardSessionResponse
import com.blueray.marasy.model.MastercardUpdateSessionRequest
import com.blueray.marasy.model.MastercardUpdateSessionResponse
import okhttp3.ResponseBody
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface MastercardApi {
    
    @POST("api/rest/version/100/merchant/{merchantId}/session")
    suspend fun initiateCheckout(
        @Path("merchantId") merchantId: String,
        @Body request: MastercardInitiateCheckoutRequest
    ): Response<MastercardSessionResponse>
    
    @POST("api/rest/version/100/merchant/{merchantId}/session")
    suspend fun createSession(
        @Path("merchantId") merchantId: String
    ): Response<MastercardSessionResponse>
    
    @PUT("api/rest/version/100/merchant/{merchantId}/session/{sessionId}")
    suspend fun updateSession(
        @Path("merchantId") merchantId: String,
        @Path("sessionId") sessionId: String,
        @Body request: MastercardUpdateSessionRequest
    ): Response<MastercardUpdateSessionResponse>
    
    @PUT("api/rest/version/100/merchant/{merchantId}/session/{sessionId}")
    suspend fun updateSessionWithCard(
        @Path("merchantId") merchantId: String,
        @Path("sessionId") sessionId: String,
        @Body cardData: MastercardCardUpdateRequest
    ): Response<MastercardUpdateSessionResponse>

    @GET("api/rest/version/100/merchant/{merchantId}/session/{sessionId}")
    suspend fun retrieveSession(
        @Path("merchantId") merchantId: String,
        @Path("sessionId") sessionId: String
    ): Response<ResponseBody>

    @PUT("api/rest/version/100/merchant/{merchantId}/order/{orderId}/transaction/{transactionId}")
    suspend fun payWithSession(
        @Path("merchantId") merchantId: String,
        @Path("orderId") orderId: String,
        @Path("transactionId") transactionId: String,
        @Body request: MastercardPayRequest
    ): Response<ResponseBody>
}

object MastercardApiClient {
    const val GATEWAY_HOST = "https://ap-gateway.mastercard.com"
    const val MERCHANT_ID = "9587188648EP"

    private const val BASE_URL = "$GATEWAY_HOST/"
    private const val API_USERNAME = "merchant.9587188648EP"
    private const val API_PASSWORD = "e58521fc6ddf5517069e11f188bcadc5"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Authorization", Credentials.basic(API_USERNAME, API_PASSWORD))
                .header("Content-Type", "application/json")
            
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: MastercardApi = retrofit.create(MastercardApi::class.java)
}




