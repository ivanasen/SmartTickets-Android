package com.ivanasen.smarttickets.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.ivanasen.smarttickets.BuildConfig
import com.ivanasen.smarttickets.models.IPFSEvent
import io.ipfs.kotlin.IPFS
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface IPFSApi {
    @GET("ipfs/{hash}")
    fun getEvent(@Path("hash") hash: String): Call<IPFSEvent>

    @POST("ipfs/")
    fun postEvent(@Body event: IPFSEvent): Call<Void>

    @POST("ipfs/")
    fun postImage(@Body image: RequestBody): Call<Void>

    companion object {
        private val LOG_TAG = IPFSApi::class.simpleName
        val instance: IPFSApi = create()

        private fun create(): IPFSApi {
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
                    .create(IPFSApi::class.java)
        }
    }
}