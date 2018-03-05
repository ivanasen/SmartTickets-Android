package com.ivanasen.smarttickets.ui.adapters

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.Bitmap
import android.support.annotation.Nullable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ivanasen.smarttickets.BuildConfig
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.api.SmartTicketsIPFSApi
import com.ivanasen.smarttickets.db.models.Event
import org.jetbrains.anko.imageBitmap
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

internal class EventAdapter(val context: Context, val data: LiveData<MutableList<Event>>)
    : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private val LOG_TAG = EventAdapter::class.java.simpleName

    init {
        data.observe(context as LifecycleOwner, Observer {
            Log.d(LOG_TAG, "Ebaniee" + it?.size)
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_event,
                parent, false)
        return ViewHolder(v)
    }


    override fun getItemCount(): Int = if (data.value == null) 0 else data.value!!.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.value == null) return

        holder.eventLocation.text = data.value!![position].locationAddress

        val imageHash = data.value!![position].images[0]
        val imageUrl = "${BuildConfig.IPFS_GATEWAY_URL}/ipfs/$imageHash"

        Glide.with(context)
                .load(imageUrl)
                .into(holder.eventImageView)
    }


    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val eventImageView = view.findViewById<ImageView>(R.id.eventImageView)
        val eventDateView = view.findViewById<TextView>(R.id.eventDateView)
        // TODO: add lowest costing event ticket to event preview
        // val eventTicketView = view.findViewById<ImageView>(R.id.eventTicketPriceView)
        val eventLocation = view.findViewById<TextView>(R.id.eventLocationView)
    }

}