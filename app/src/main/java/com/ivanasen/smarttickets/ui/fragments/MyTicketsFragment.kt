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
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
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
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import java.text.DateFormat

class MyTicketsFragment : Fragment() {

    private val QR_CODE_SIZE: Int = 550

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

        ticketCardView.onClick {
            // Don't hide the view
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

        val ticketIdString = ticket.ticketId.toString()
        val ticketBitmap = QRCode.from(ticketIdString).withSize(QR_CODE_SIZE, QR_CODE_SIZE).bitmap()
        ticketQrCode.imageBitmap = ticketBitmap

        ticketEventName.text = event.name
        ticketEventLocation.text = event.locationName
        ticketEventDate.text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(event.timestamp * 1000)

        sellTicketBtn.isEnabled = ticket.ticketType.refundable
        sellTicketBtn.onClick {
            mViewModel.attemptSellTicket(ticket)
                    .observe(this@MyTicketsFragment, Observer {
                        when(it) {
                            Utility.Companion.TransactionStatus.PENDING -> {
                                Toast.makeText(this@MyTicketsFragment.context,
                                        getString(R.string.selling_ticket_text),
                                        Toast.LENGTH_LONG)
                                        .show()
                            }
                            Utility.Companion.TransactionStatus.COMPLETE -> {
                                MaterialDialog.Builder(this@MyTicketsFragment.context!!)
                                        .title(R.string.ticket_sell_success_title)
                                        .content(R.string.ticket_sell_success_message)
                                        .positiveText(R.string.OK)
                                        .show()
                            }
                            Utility.Companion.TransactionStatus.ERROR -> {
                                Toast.makeText(this@MyTicketsFragment.context,
                                        getString(R.string.selling_ticket_error),
                                        Toast.LENGTH_LONG)
                                        .show()
                            }
                        }
                    })
        }
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
