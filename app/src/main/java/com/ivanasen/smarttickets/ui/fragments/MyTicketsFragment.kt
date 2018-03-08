package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.transition.Fade
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.db.models.Ticket
import com.ivanasen.smarttickets.ui.adapters.TicketsAdapter
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_my_tickets.*
import kotlinx.android.synthetic.main.list_item_ticket.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import java.text.DateFormat


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
        setupViews()
    }

    private fun setupViews() {
        ticketsRefreshLayout.setColorSchemeColors(resources.getColor(R.color.appOrangePink),
                resources.getColor(R.color.appOrange),
                resources.getColor(R.color.appOrangePink))
        ticketsRefreshLayout.isRefreshing = true
        ticketsRefreshLayout.onRefresh {
            mViewModel.refreshTickets()
        }

        val adapter = TicketsAdapter(context, mViewModel.tickets, mViewModel.events,
                { event, ticket -> showTicketDetailView(event, ticket) })
        ticketsRecyclerView.layoutManager = LinearLayoutManager(context)
        ticketsRecyclerView.adapter = adapter

        ticketDetailView.onClick {
            TransitionManager.beginDelayedTransition(view as ViewGroup, Fade())
            ticketDetailView.visibility = View.GONE
        }
    }

    private fun showTicketDetailView(event: Event, ticket: Ticket) {
        TransitionManager.beginDelayedTransition(view as ViewGroup, Fade())
        ticketDetailView.visibility = View.VISIBLE

        val imageUrl = Utility.getIpfsImageUrl(event.images[0])
        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(ticketEventImageView)

        ticketEventName.text = event.name
        ticketEventLocation.text = event.locationName
        ticketEventDate.text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(event.timestamp * 1000)
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
