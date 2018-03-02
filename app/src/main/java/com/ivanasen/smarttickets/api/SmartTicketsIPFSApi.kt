package com.ivanasen.smarttickets.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.ivanasen.smarttickets.BuildConfig
import com.ivanasen.smarttickets.db.models.Event
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface SmartTicketsIPFSApi {
    @GET("ipfs/{hash}")
    fun getEvent(@Path("hash") hash: String): Call<Event>

    @POST("ipfs/")
    fun postEvent(@Body event: Event): Call<Void>

    @POST("ipfs/")
    fun postImage(@Body byteArray: ByteArray): Call<Void>

    companion object {
        private val LOG_TAG = SmartTicketsIPFSApi::class.simpleName
        val instance: SmartTicketsIPFSApi = create()

        private fun create(): SmartTicketsIPFSApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d(LOG_TAG, it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            val gson = GsonBuilder()
                    .setLenient()
                    .create()

            return Retrofit.Builder()
                    .baseUrl(BuildConfig.IPFS_GATEWAY_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(SmartTicketsIPFSApi::class.java)
        }
    }
}