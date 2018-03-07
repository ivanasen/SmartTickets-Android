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
import com.ivanasen.smarttickets.db.models.Ticket
import com.ivanasen.smarttickets.repositories.SmartTicketsRepository
import org.w3c.dom.Text
import java.text.SimpleDateFormat

internal class TicketsAdapter(val context: Context?, val tickets: LiveData<List<Ticket>>)
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
        val ticketId = ticket.ticketId
        val eventId = ticket.ticketType.eventId

        SmartTicketsRepository.fetchEvent(eventId.toLong())
                .observe(context as LifecycleOwner, Observer {
                    it?.let {
                        val imageUrl = "${BuildConfig.IPFS_GATEWAY_URL}/ipfs/${it.images[0]}"
                        val eventName = it.name
                        val formatDate = SimpleDateFormat(context.getString(R.string.date_format))
                        val eventTimestamp = it.timestamp
                        val eventDate = formatDate.format(eventTimestamp)
                        val location = it.locationAddress

                        Glide.with(context)
                                .load(imageUrl)
                                .apply(RequestOptions()
                                        .centerCrop())
                                .into(holder.eventImageView)
                        holder.eventNameView.text = eventName
                        holder.eventLocationView.text = location
                        holder.eventDateView.text = eventDate
                    }
                })

        val ticketPrice = ticket.ticketType.priceInUSDCents

    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val eventImageView = view.findViewById<ImageView>(R.id.eventImageView)
        val eventNameView = view.findViewById<TextView>(R.id.eventNameView)
        val eventDateView = view.findViewById<TextView>(R.id.ticketEventDate)
        val eventLocationView = view.findViewById<TextView>(R.id.ticketEventLocation)
    }
}
