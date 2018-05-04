package com.ivanasen.smarttickets.api

import com.google.gson.Gson
import com.ivanasen.smarttickets.BuildConfig
import com.ivanasen.smarttickets.models.Event
import io.ipfs.kotlin.IPFS

object IpfsApi {
    val instance = IPFS(BuildConfig.IPFS_API_URL)
    val gson = Gson()

    fun getEvent(hash: String): Event {
        val json = instance.get.cat(hash)
        return gson.fromJson(json, Event::class.java)
    }

    fun postEvent(event: Event): String {
        val json = gson.toJson(event)
        val hash = instance.add.string(json)
        return hash.Hash
    }


}