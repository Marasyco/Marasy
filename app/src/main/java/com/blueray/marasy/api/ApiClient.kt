package com.blueray.marasy.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
//    private const val USER_BASE_URL = "http://demo2.marasy.com.dedi8785.your-server.de/" //  demo
    private const val USER_BASE_URL = "https://marasy-jo.com/"   // live

    private fun getRetrofit(baseUrl: String): Retrofit {


        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .addInterceptor(provideInterceptor())
            .build()


        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    private fun provideInterceptor(): Interceptor {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return logging
    }

    val retrofitService: Api by lazy {
        getRetrofit(USER_BASE_URL).create(Api::class.java)
    }
}