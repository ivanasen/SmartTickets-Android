package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Ticket
import com.ivanasen.smarttickets.ui.adapters.TicketsAdapter
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_my_tickets.*
import org.jetbrains.anko.support.v4.onRefresh


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MyTicketsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MyTicketsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyTicketsFragment : Fragment() {

    private val mViewModel: AppViewModel by lazy {
        ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = getString(R.string.title_my_tickets)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_my_tickets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeLiveData()
        populateViews()
    }

    private fun populateViews() {
        ticketsRefreshLayout.setColorSchemeColors(resources.getColor(R.color.appOrangePink),
                resources.getColor(R.color.appOrange),
                resources.getColor(R.color.appOrangePink))
        ticketsRefreshLayout.isRefreshing = true
        ticketsRefreshLayout.onRefresh {
            mViewModel.refreshTickets()
        }

        val adapter = TicketsAdapter(context, mViewModel.tickets, mViewModel.events)
        ticketsRecyclerView.layoutManager = LinearLayoutManager(context)
        ticketsRecyclerView.adapter = adapter
    }

    private fun observeLiveData() {
        mViewModel.tickets.observe(this, Observer {
            if ((it ?: emptyList<Ticket>()).isNotEmpty()) {
                emptyViewLayout.visibility = View.GONE
                ticketsRecyclerView.visibility = View.VISIBLE
            } else {
                emptyViewLayout.visibility = View.VISIBLE
                ticketsRecyclerView.visibility = View.GONE
            }
            ticketsRefreshLayout.isRefreshing = false
        })
    }
}
