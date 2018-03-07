package com.ivanasen.smarttickets.ui.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.db.models.TicketType
import com.ivanasen.smarttickets.ui.adapters.TicketTypeAdapter
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.activity_discover_event_detail.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.DateFormat
import java.text.DateFormat.*
import java.text.SimpleDateFormat

class DiscoverEventDetailActivity : AppCompatActivity() {

    companion object {
        val EXTRA_EVENT_IMAGE = "ExtraEventImage"
        val EXTRA_EVENT_NAME = "ExtraEventName"
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
        observeLiveData()
    }

    private fun observeLiveData() {
        val eventId = intent.extras.getLong(EXTRA_EVENT_ID)
        mViewModel.fetchEvent(eventId).observe(this, Observer {
            it?.let { populateViews(it) }
        })

    }

    private fun populateViews(event: Event) {
        eventLocationView.text = event.locationAddress
        eventLocationView.onClick {
            Toast.makeText(this@DiscoverEventDetailActivity, event.locationAddress, Toast.LENGTH_SHORT)
                    .show()
        }


        eventDescriptionView.text = event.description

        val formatDate = getDateTimeInstance(MEDIUM, SHORT).format(event.timestamp * 1000)
        eventTimeView.text = formatDate
    }

    private fun setupViews() {
        val imageUrl = intent.extras.get(EXTRA_EVENT_IMAGE)
        title = intent.extras.getString(EXTRA_EVENT_NAME)

        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(eventImage)

        val attemptBuyTicket: (ticketType: TicketType) -> Unit = {
            mViewModel.attemptToBuyTicket(it)
        }

        val eventId = intent.extras.getLong(EXTRA_EVENT_ID)
        val ticketTypesLiveData = mViewModel.fetchTicketTypesForEvent(eventId)
        val adapter = TicketTypeAdapter(this, ticketTypesLiveData, attemptBuyTicket)
        ticketTypesRecyclerView.adapter = adapter
        ticketTypesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

}
