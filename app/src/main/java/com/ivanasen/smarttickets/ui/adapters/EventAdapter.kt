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
import com.ivanasen.smarttickets.util.Utility
import java.text.DateFormat


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

        val eventName = eventsData.value!![position].name
        val eventId = eventsData.value!![position].eventId
        val location = eventsData.value!![position].locationAddress
        val timestamp = eventsData.value!![position].timestamp * 1000
        val formattedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(timestamp)
        val cheapestTicket = eventsData.value!![position].tickets.minBy { it.priceInUSDCents }
        holder.eventTicketView.text = String.format(activity.getString(R.string.starting_from_text),
                (cheapestTicket?.priceInUSDCents!!.toDouble() / 100))
        val imageHash = eventsData.value!![position].images[0]
        val imageUrl = Utility.getIpfsImageUrl(imageHash)

        holder.eventNameView.text = eventName
        holder.eventLocation.text = location
        holder.eventDateView.text = formattedDate
        Glide.with(activity)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(holder.eventImageView)

        holder.view.onClick {
            val intent = Intent(this@EventAdapter.activity, DiscoverEventDetailActivity::class.java)
            intent.putExtra(DiscoverEventDetailActivity.EXTRA_EVENT_ID, eventId)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this@EventAdapter.activity, holder.eventImageView,
                    this@EventAdapter.activity.getString(R.string.event_transition))
            this@EventAdapter.activity.startActivity(intent, options.toBundle())
        }
    }


    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val eventImageView: ImageView = view.findViewById(R.id.eventImageView)
        val eventNameView: TextView = view.findViewById(R.id.eventNameView)
        val eventDateView: TextView = view.findViewById(R.id.eventDateView)
        val eventTicketView: TextView = view.findViewById(R.id.eventTicketPriceView)
        val eventLocation: TextView = view.findViewById(R.id.eventLocationView)
    }
}