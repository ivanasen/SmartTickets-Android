package com.ivanasen.smarttickets.ui.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.activity_discover_event_detail.*

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
    }

    private fun setupViews() {
        val imageUrl = intent.extras.get(EXTRA_EVENT_IMAGE)
        title = intent.extras.getString(EXTRA_EVENT_NAME)

        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(eventImage)
    }
}
