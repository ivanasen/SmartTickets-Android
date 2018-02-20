package com.ivanasen.smarttickets.api

import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface SmartTicketsIPFSApi {
    @GET("{hash}")
    fun getData(@Path("hash") hash: String): String

    @POST
    @FormUrlEncoded
    fun postData(data: String)
}