package com.ivanasen.smarttickets.api

import android.util.Log
import com.ivanasen.smarttickets.BuildConfig
import com.ivanasen.smarttickets.db.models.Event
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface SmartTicketsIPFSApi {
    @GET("{hash}")
    fun getEventMetaData(@Path("hash") hash: String): Call<Event>

    @POST
    @FormUrlEncoded
    fun postData(data: String): Call<String>

    companion object {
        private val LOG_TAG = SmartTicketsIPFSApi::class.simpleName

        fun create(): SmartTicketsIPFSApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d(LOG_TAG, it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(BuildConfig.IPFS_GATEWAY_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SmartTicketsIPFSApi::class.java)
        }
    }
}