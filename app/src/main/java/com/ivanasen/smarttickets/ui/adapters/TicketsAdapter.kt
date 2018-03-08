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
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.db.models.Ticket
import com.ivanasen.smarttickets.util.Utility
import java.text.SimpleDateFormat

internal class TicketsAdapter(val context: Context?, private val tickets: LiveData<MutableList<Ticket>>,
                              private val events: LiveData<MutableList<Event>>)
    : RecyclerView.Adapter<TicketsAdapter.ViewHolder>() {

    init {
        tickets.observe(context as LifecycleOwner, Observer {
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ticket,
                parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = if (tickets.value != null) tickets.value!!.size else 0

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (tickets.value == null) return

        val ticket = tickets.value!![position]
        val eventId = ticket.ticketType.eventId.toLong()
        val event: Event = events.value?.filter { it.eventId == eventId }!![0]

        val imageUrl = Utility.getIpfsImageUrl(event.images[0])
        val eventName = event.name
        val formatDate = SimpleDateFormat(context?.getString(R.string.date_format))
        val eventTimestamp = event.timestamp
        val eventDate = formatDate.format(eventTimestamp)
        val location = event.locationName

        Glide.with(context!!)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(holder.eventImageView)
        holder.eventNameView.text = eventName
        holder.eventLocationView.text = location
        holder.eventDateView.text = eventDate
    }


    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val eventImageView: ImageView = view.findViewById(R.id.eventImageView)
        val eventNameView: TextView = view.findViewById(R.id.eventNameView)
        val eventDateView: TextView = view.findViewById(R.id.ticketEventDate)
        val eventLocationView: TextView = view.findViewById(R.id.ticketEventLocation)
    }
}
