package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.api.ApplicationApi
import com.ivanasen.smarttickets.ui.activities.DiscoverEventDetailActivity
import com.ivanasen.smarttickets.ui.adapters.EventAdapter
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_discover.*
import org.jetbrains.anko.support.v4.onRefresh


class DiscoverFragment : Fragment() {

    private val LOG_TAG = DiscoverFragment::class.java.simpleName

    private val SORT_POPULAR_INDEX = 0
    private val SORT_RECENT_INDEX = 1
    private val SORT_OLD_INDEX = 2

    private val mContext: Context by lazy { requireContext() }
    private val mViewModel: AppViewModel by lazy {
        if (activity != null)
            ViewModelProviders.of(activity as FragmentActivity).get(AppViewModel::class.java)
        else
            ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    private lateinit var mAdapter: EventAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = getString(R.string.title_discover)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.discover, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.sort_events -> {
                showSortEventsDialog()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showSortEventsDialog() {
        MaterialDialog.Builder(mContext)
                .title(getString(R.string.sort_events_title))
                .items(R.array.event_sort_types)
                .itemsCallbackSingleChoice(0, { _, _, _, _ -> true })
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive({ dialog, which ->
                    when (dialog.selectedIndex) {
                        SORT_POPULAR_INDEX -> {
                            mViewModel.fetchEvents(ApplicationApi.EVENT_ORDER_POPULARITY)
                        }
                        SORT_RECENT_INDEX -> {
                            mViewModel.fetchEvents(ApplicationApi.EVENT_ORDER_RECENT)
                        }
                        SORT_OLD_INDEX -> {
                            mViewModel.fetchEvents(ApplicationApi.EVENT_ORDER_OLD)
                        }
                    }
                })
                .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeLiveData()
        setupViews()
    }

    private fun observeLiveData() {
        mViewModel.eventsFetchStatus.observe(this, Observer {
            when (it) {
                Utility.Companion.TransactionStatus.PENDING -> {
                    eventRefreshLayout.isRefreshing = true
                }

                Utility.Companion.TransactionStatus.SUCCESS -> {
                    emptyViewLayout.visibility = View.GONE
                    eventsView.visibility = View.VISIBLE
                    eventRefreshLayout.isRefreshing = false
                }

                Utility.Companion.TransactionStatus.FAILURE,
                Utility.Companion.TransactionStatus.ERROR -> {
                    emptyViewLayout.visibility = View.VISIBLE
                    eventsView.visibility = View.GONE
                    eventRefreshLayout.isRefreshing = false
                }
            }
        })
    }

    private fun setupViews() {
        eventsView.layoutManager = LinearLayoutManager(context)
        mAdapter = EventAdapter(activity!!, mViewModel.events, { eventId, sharedView ->
            val intent = Intent(context, DiscoverEventDetailActivity::class.java)
            intent.putExtra(DiscoverEventDetailActivity.EXTRA_EVENT_ID, eventId)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity!!, sharedView,
                    activity!!.getString(R.string.event_transition))
            context?.startActivity(intent, options.toBundle())
        })
        eventsView.adapter = mAdapter

        eventRefreshLayout.setColorSchemeColors(resources.getColor(R.color.pink),
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.pink))

        eventRefreshLayout.onRefresh {
            if (mViewModel.eventsFetchStatus.value != Utility.Companion.TransactionStatus.PENDING) {
                mViewModel.refreshEvents()
            }
        }
    }
}
