package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.adapters.EventAdapter
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_discover.*

/**
 * A placeholder fragment containing a simple view.
 */
class DiscoverFragment : Fragment() {

    private val LOG_TAG = DiscoverFragment::class.java.simpleName

    private val mViewModel: AppViewModel by lazy {
        ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    private lateinit var mAdapter: EventAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeLiveData()
        setupViews()
    }

    private fun observeLiveData() {
        mViewModel.fetchEvents()

        mViewModel.events.observe(this, Observer {
            it?.forEach {
                Log.d(LOG_TAG, "Fetched event: ${it.name}")
            }
        })
    }

    private fun setupViews() {
        mAdapter = EventAdapter(context!!, mViewModel.events)
        eventsView.layoutManager = LinearLayoutManager(context)
        eventsView.adapter = mAdapter
    }
}
