package com.ivanasen.smarttickets.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.ivanasen.smarttickets.BuildConfig
import com.ivanasen.smarttickets.models.Event
import com.ivanasen.smarttickets.models.Transaction
import com.ivanasen.smarttickets.models.TransactionResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface SmartTicketsApi {

    @GET("api/events")
    fun getEvents(@Query("order") order: String,
                  @Query("page") page: Int,
                  @Query("limit") limit: Int): Call<List<Event>>

    @GET("api/history")
    fun getTxHistory(@Query("address") address: String,
                     @Query("page") page: Int,
                     @Query("offset") limit: Int,
                     @Query("sort") sort: String): Call<TransactionResponse>

    companion object {
        private val LOG_TAG = SmartTicketsApi::class.simpleName

        val EVENT_ORDER_POPULARITY = "popular"
        val EVENT_ORDER_RECENT = "recent"
        val EVENT_ORDER_OLD = "old"
        val EVENT_PAGE_DEFAULT = 0
        val EVENT_LIMIT_DEFAULT = 10

        val TX_HISTORY_PAGE_DEFAULT = 0
        val TX_HISTORY_LIMIT_DEFAULT = 10
        val TX_HISTORY_SORT_DSC = "desc"
        val TX_HISTORY_SORT_ASC = "asc"


        val instance: SmartTicketsApi = create()

        private fun create(): SmartTicketsApi {
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
                    .baseUrl(BuildConfig.API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(SmartTicketsApi::class.java)
        }
    }
}