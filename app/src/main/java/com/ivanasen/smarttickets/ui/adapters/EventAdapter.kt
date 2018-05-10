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
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.models.Event
import org.jetbrains.anko.sdk25.coroutines.onClick
import com.ivanasen.smarttickets.util.Utility
import java.text.DateFormat
import android.view.animation.AlphaAnimation
import com.ivanasen.smarttickets.util.Utility.Companion.CONTRACT_TRUE
import org.jetbrains.anko.find


internal class EventAdapter(val activity: Activity, private val eventsData: LiveData<MutableList<Event>>,
                            private val eventClickCallBack: (eventId: Long, sharedView: ImageView) -> Unit)
    : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private val LOG_TAG = EventAdapter::class.java.simpleName
    private val FADE_DURATION: Long = 200

    init {
        eventsData.observe(activity as LifecycleOwner, Observer {
            it?.let {
                if (it.size > itemCount) {
                    notifyItemInserted(it.size - 1)
                } else {
                    notifyDataSetChanged()
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_event,
                parent, false)
        return ViewHolder(v)
    }


    override fun getItemCount(): Int = eventsData.value?.size ?: 0


    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventsData.value!![position]
        val (eventId, _, name, _, timestamp, _, _,
                locationAddress, thumbnailHash, tickets, _) = event
        val formattedDate = DateFormat
                .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(timestamp)

        val cheapestTicket = tickets.minBy { it.priceInUSDCents }
        val priceInDollars = cheapestTicket?.priceInUSDCents!!.toDouble() / 100

        if (thumbnailHash.isNotEmpty()) {
            val imageUrl = Utility.getIpfsImageUrl(thumbnailHash)
            Glide.with(activity)
                    .load(imageUrl)
                    .apply(RequestOptions()
                            .centerCrop())
                    .into(holder.eventImageView)
        }

        if (event.cancelled.toInt() == CONTRACT_TRUE) {
            holder.eventCancelledView.visibility = View.VISIBLE
            holder.eventDescriptionView.visibility = View.GONE
        } else {
            holder.eventCancelledView.visibility = View.GONE
            holder.eventDescriptionView.visibility = View.VISIBLE

            holder.eventTicketView.text = String.format(activity.getString(R.string.starting_from_text), priceInDollars)
            holder.eventLocation.text = locationAddress
            holder.eventDateView.text = formattedDate
        }

        holder.eventNameView.text = name

        holder.view.onClick {
            eventClickCallBack(eventId, holder.eventImageView)
        }

        setFadeAnimation(holder.view)
    }

    private fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = FADE_DURATION
        view.startAnimation(anim)
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val eventImageView = view.find<ImageView>(R.id.eventImageView)
        val eventNameView = view.find<TextView>(R.id.eventNameView)
        val eventDateView = view.find<TextView>(R.id.eventDateView)
        val eventTicketView = view.find<TextView>(R.id.eventTicketPriceView)
        val eventLocation = view.find<TextView>(R.id.eventLocationView)
        val eventCancelledView = view.find<TextView>(R.id.eventCancelledView)
        val eventDescriptionView = view.find<View>(R.id.eventDescriptionView)
    }
}