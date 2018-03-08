package com.ivanasen.smarttickets.ui.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.db.models.TicketType
import com.ivanasen.smarttickets.ui.adapters.TicketTypeAdapter
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.launchActivity
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.activity_discover_event_detail.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.DateFormat
import java.text.DateFormat.*
import java.text.SimpleDateFormat

class DiscoverEventDetailActivity : AppCompatActivity() {

    companion object {
        val EXTRA_EVENT_ID = "ExtraEventId"
    }

    private val LOG_TAG = DiscoverEventDetailActivity::class.java.simpleName

    private val mViewModel: AppViewModel by lazy {
        ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_event_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViews()
    }


    private fun startMapsActivity(latLong: LatLng) {
        val mapsIntent = Intent(this,
                MapsActivity::class.java)
        val extras = Bundle()
        extras.putParcelable(MapsActivity.LAT_LONG_EXTRA_KEY, latLong)
        mapsIntent.putExtras(extras)
        startActivity(mapsIntent)
    }

    private fun setupViews() {
        val eventId = intent.extras.getLong(EXTRA_EVENT_ID)
        val event = mViewModel.events.value?.filter { it.eventId == eventId }!![0]

        title = event.name

        val imageUrl = Utility.getIpfsImageUrl(event.images[0])
        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(eventImage)

        val attemptBuyTicket: (ticketType: TicketType) -> Unit = {
            mViewModel.attemptToBuyTicket(it)
        }

        val formatDate = getDateTimeInstance(MEDIUM, SHORT).format(event.timestamp * 1000)
        eventTimeView.text = formatDate

        eventLocationView.text = event.locationAddress
        eventLocationView.onClick { startMapsActivity(event.latLong) }

        eventDescriptionView.text = event.description

        val ticketTypes = event.tickets
        val adapter = TicketTypeAdapter(this, ticketTypes, attemptBuyTicket)
        ticketTypesRecyclerView.adapter = adapter
        ticketTypesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

}
