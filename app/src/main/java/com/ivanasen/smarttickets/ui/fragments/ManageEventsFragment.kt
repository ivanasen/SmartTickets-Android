package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.activities.CreateEventActivity
import com.ivanasen.smarttickets.ui.activities.DiscoverEventDetailActivity
import com.ivanasen.smarttickets.ui.activities.ManageEventDetailActivity
import com.ivanasen.smarttickets.ui.adapters.EventAdapter
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.launchActivity
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_manage_events.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh


class ManageEventsFragment : Fragment() {

    private val mViewModel: AppViewModel by lazy {
        if (activity != null)
            ViewModelProviders.of(activity as FragmentActivity).get(AppViewModel::class.java)
        else
            ViewModelProviders.of(this).get(AppViewModel::class.java)
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
        fetchEventsAndObserve()
    }

    private fun fetchEventsAndObserve() {
        mViewModel.fetchMyEvents().observe(this, Observer {
            when (it) {
                Utility.Companion.TransactionStatus.PENDING -> {
                    manageEventsRefreshLayout.isRefreshing = true
                }
                Utility.Companion.TransactionStatus.SUCCESS -> {
                    emptyViewLayout.visibility = View.GONE
                    eventsView.visibility = View.VISIBLE
                    manageEventsRefreshLayout.isRefreshing = false
                }
                Utility.Companion.TransactionStatus.ERROR,
                Utility.Companion.TransactionStatus.FAILURE -> {
                    emptyViewLayout.visibility = View.VISIBLE
                    eventsView.visibility = View.GONE
                    manageEventsRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    private fun setupViews() {
        addEventBtn.onClick {
            launchActivity(this@ManageEventsFragment.context!!, CreateEventActivity::class.java)
        }

        manageEventsRefreshLayout.setColorSchemeColors(resources.getColor(R.color.pink),
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.pink))
        manageEventsRefreshLayout.onRefresh {
            fetchEventsAndObserve()
        }

        eventsView.layoutManager = LinearLayoutManager(context)
        eventsView.adapter = EventAdapter(activity!!, mViewModel.myEvents, { eventId, sharedView ->
            val intent = Intent(context, ManageEventDetailActivity::class.java)
            intent.putExtra(DiscoverEventDetailActivity.EXTRA_EVENT_ID, eventId)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity!!, sharedView,
                    activity!!.getString(R.string.event_transition))
            context?.startActivity(intent, options.toBundle())
        })
    }
}
