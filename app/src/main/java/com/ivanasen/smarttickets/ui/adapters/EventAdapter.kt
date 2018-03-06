package com.ivanasen.smarttickets.ui.adapters

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ivanasen.smarttickets.BuildConfig
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Event
import java.text.SimpleDateFormat

internal class EventAdapter(val context: Context, val eventsData: LiveData<MutableList<Event>>)
    : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private val LOG_TAG = EventAdapter::class.java.simpleName

    init {
        eventsData.observe(context as LifecycleOwner, Observer {
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_event,
                parent, false)
        return ViewHolder(v)
    }


    override fun getItemCount(): Int = if (eventsData.value == null) 0 else eventsData.value!!.size


    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (eventsData.value == null) return

        holder.eventLocation.text = eventsData.value!![position].locationAddress

        holder.eventNameView.text = eventsData.value!![position].name

        val timestamp = eventsData.value!![position].timestamp
        val formatDate = SimpleDateFormat(context.getString(R.string.date_format))
        holder.eventDateView.text = formatDate.format(timestamp)

        val formatTime = SimpleDateFormat(context.getString(R.string.time_format))
        holder.eventTimeView.text = formatTime.format(timestamp)

        val cheapestTicket = eventsData.value!![position].tickets.minBy { it.priceInUSDCents }
        holder.eventTicketView.text = String.format(context.getString(R.string.starting_from_text),
                (cheapestTicket?.priceInUSDCents!!.toDouble() / 100))

        val imageHash = eventsData.value!![position].images[0]
        val imageUrl = "${BuildConfig.IPFS_GATEWAY_URL}/ipfs/$imageHash"

        Glide.with(context)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(holder.eventImageView)

    }


    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val eventImageView = view.findViewById<ImageView>(R.id.eventImageView)
        val eventNameView = view.findViewById<TextView>(R.id.eventNameView)
        val eventDateView = view.findViewById<TextView>(R.id.eventDateView)
        val eventTimeView = view.findViewById<TextView>(R.id.eventTimeView)
        val eventTicketView = view.findViewById<TextView>(R.id.eventTicketPriceView)
        val eventLocation = view.findViewById<TextView>(R.id.eventLocationView)
    }

}