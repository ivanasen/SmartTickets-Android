package com.ivanasen.smarttickets.ui.fragments

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.adapters.EventAdapter
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_discover.*
import org.jetbrains.anko.recyclerview.v7.coroutines.onItemTouchListener

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

        val searchManager = context?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))

        super.onCreateOptionsMenu(menu, inflater)
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
        mAdapter = EventAdapter(activity!!, mViewModel.events)
        eventsView.layoutManager = LinearLayoutManager(context)
        eventsView.adapter = mAdapter
    }
}
