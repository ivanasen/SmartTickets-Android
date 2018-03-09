package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = getString(R.string.title_manage)
        return inflater.inflate(R.layout.fragment_manage_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeLiveData()

        showTestDialog()
    }

    private fun showTestDialog() {
        MaterialDialog.Builder(context!!)
                .title(getString(R.string.test_dialog_title))
                .content(getString(R.string.event_creation_test_message))
                .positiveText(R.string.OK)
                .show()
    }

    private fun observeLiveData() {
        mViewModel.myEvents.observe(this, Observer {
            if ((it ?: emptyList<Event>()).isNotEmpty()) {
                emptyViewLayout.visibility = View.GONE
                eventsView.visibility = View.VISIBLE
            } else {
                emptyViewLayout.visibility = View.VISIBLE
                eventsView.visibility = View.GONE
            }
            manageEventsRefreshLayout.isRefreshing = false
        })
    }

    private fun setupViews() {
        addEventBtn.onClick {
            launchActivity(this@ManageEventsFragment.context!!, CreateEventActivity::class.java)
        }

        manageEventsRefreshLayout.setColorSchemeColors(resources.getColor(R.color.appOrangePink),
                resources.getColor(R.color.appOrange),
                resources.getColor(R.color.appOrangePink))
        manageEventsRefreshLayout.isRefreshing = true
        manageEventsRefreshLayout.onRefresh {
            mViewModel.fetchMyEvents()
        }

        eventsView.layoutManager = LinearLayoutManager(context)
        eventsView.adapter = EventAdapter(activity!!, mViewModel.myEvents)
    }
}
