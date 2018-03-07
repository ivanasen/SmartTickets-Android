package com.ivanasen.smarttickets.ui.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
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
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.SimpleDateFormat
import android.support.v4.app.ActivityOptionsCompat
import android.content.Intent
import com.ivanasen.smarttickets.ui.activities.DiscoverEventDetailActivity


internal class EventAdapter(val activity: Activity, val eventsData: LiveData<MutableList<Event>>)
    : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private val LOG_TAG = EventAdapter::class.java.simpleName

    init {
        eventsData.observe(activity as LifecycleOwner, Observer {
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
        val eventId = eventsData.value!![position].eventId

        holder.eventLocation.text = eventsData.value!![position].locationAddress

        val eventName = eventsData.value!![position].name
        holder.eventNameView.text = eventName

        val timestamp = eventsData.value!![position].timestamp * 1000
        val formatDate = SimpleDateFormat(activity.getString(R.string.date_format))
        holder.eventDateView.text = formatDate.format(timestamp)

        val formatTime = SimpleDateFormat(activity.getString(R.string.time_format))
        holder.eventTimeView.text = formatTime.format(timestamp)

        val cheapestTicket = eventsData.value!![position].tickets.minBy { it.priceInUSDCents }
        holder.eventTicketView.text = String.format(activity.getString(R.string.starting_from_text),
                (cheapestTicket?.priceInUSDCents!!.toDouble() / 100))

        val imageHash = eventsData.value!![position].images[0]
        val imageUrl = "${BuildConfig.IPFS_GATEWAY_URL}/ipfs/$imageHash"

        Glide.with(activity)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(holder.eventImageView)

        holder.view.onClick {
            val intent = Intent(this@EventAdapter.activity, DiscoverEventDetailActivity::class.java)
            intent.putExtra(DiscoverEventDetailActivity.EXTRA_EVENT_IMAGE, imageUrl)
            intent.putExtra(DiscoverEventDetailActivity.EXTRA_EVENT_NAME, eventName)
            intent.putExtra(DiscoverEventDetailActivity.EXTRA_EVENT_ID, eventId)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@EventAdapter.activity, holder.eventImageView,
                    this@EventAdapter.activity.getString(R.string.event_transition))
            this@EventAdapter.activity.startActivity(intent, options.toBundle())
        }
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