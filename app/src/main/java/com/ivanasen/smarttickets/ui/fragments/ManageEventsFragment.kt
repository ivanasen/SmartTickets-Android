package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.ui.activities.CreateEventActivity
import com.ivanasen.smarttickets.ui.adapters.EventAdapter
import com.ivanasen.smarttickets.util.Utility.Companion.launchActivity
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_manage_events.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh


class ManageEventsFragment : Fragment() {

    val mViewModel: AppViewModel by lazy {
        ViewModelProviders.of(activity as FragmentActivity).get(AppViewModel::class.java)
    }

    private val mEvents: MutableLiveData<MutableList<Event>> = MutableLiveData()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = getString(R.string.title_manage)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_manage_events, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.manage_events, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeLiveData()
    }

    private fun observeLiveData() {
        mViewModel.fetchMyEvents().observe(this, Observer {
            manageEventsRefreshLayout.isRefreshing = false
            mEvents.postValue(it)
        })
    }

    private fun setupViews() {
        addEventBtn.onClick {
            launchActivity(this@ManageEventsFragment.context!!, CreateEventActivity::class.java)
        }

        manageEventsRefreshLayout.isRefreshing = true
        manageEventsRefreshLayout.onRefresh {
            mViewModel.fetchMyEvents()
        }

        eventsView.layoutManager = LinearLayoutManager(context)
        eventsView.adapter = EventAdapter(activity!!, mEvents)
    }
}
