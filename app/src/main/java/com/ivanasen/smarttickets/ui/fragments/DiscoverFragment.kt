package com.ivanasen.smarttickets.ui.fragments

import android.app.SearchManager
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.ImageView
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.ui.activities.DiscoverEventDetailActivity
import com.ivanasen.smarttickets.ui.adapters.EventAdapter
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_discover.*
import org.jetbrains.anko.support.v4.onRefresh


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
        activity?.title = getString(R.string.title_discover)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.discover, menu)

//        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//
//                return true
//            }
//        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeLiveData()
        setupViews()
    }

    private fun observeLiveData() {
        mViewModel.events.observe(this, Observer {
            if ((it ?: emptyList<Event>()).isNotEmpty()) {
                emptyViewLayout.visibility = View.GONE
                eventsView.visibility = View.VISIBLE
            } else {
                emptyViewLayout.visibility = View.VISIBLE
                eventsView.visibility = View.GONE
            }
            eventRefreshLayout.isRefreshing = false
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

        eventRefreshLayout.setColorSchemeColors(resources.getColor(R.color.appOrangePink),
                resources.getColor(R.color.appOrange),
                resources.getColor(R.color.appOrangePink))

        eventRefreshLayout.isRefreshing = true
        eventRefreshLayout.onRefresh {
            mViewModel.refreshEvents()
        }
    }
}
