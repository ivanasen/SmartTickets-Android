package com.ivanasen.smarttickets.ui.adapters

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.TicketType
import com.ivanasen.smarttickets.repositories.SmartTicketsRepository
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.DecimalFormat

internal class TicketTypeAdapter(val context: Context, val data: LiveData<MutableList<TicketType>>,
                                 val onTicketBuyListener: (ticketType: TicketType) -> Unit)
    : RecyclerView.Adapter<TicketTypeAdapter.ViewHolder>() {

    private val ethFormat = DecimalFormat(context.getString(R.string.eth_format))
    private val usdFormat = DecimalFormat(context.getString(R.string.usd_format))

    init {
        data.observe(context as LifecycleOwner, Observer {
            notifyDataSetChanged()
        })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ticket_type,
                parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = if (data.value != null) data.value!!.size else 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.value == null) return
        val ticketType = data.value!![position]

        val ticketPriceInUsd = ticketType.priceInUSDCents
        val remainingTickets = ticketType.currentSupply
        val refundable = ticketType.refundable

        SmartTicketsRepository.fetchEtherValueOfUsd(ticketPriceInUsd.toBigDecimal())
                .observe(context as LifecycleOwner, Observer {
                    holder.ticketPriceInEtherView.text = ethFormat.format(it)
                })
        holder.ticketPriceInUsdView.text = usdFormat.format(ticketPriceInUsd.toDouble() / 100)
        holder.ticketSupplyTextView.text =
                String.format(context.getString(R.string.tickets_remaining_text), remainingTickets)
        holder.ticketRefundable.text =
                if (refundable)
                    context.getString(R.string.refundable_text)
                else
                    context.getString(R.string.not_refundable_text)

        holder.buyTicketBtn.onClick {
            onTicketBuyListener(ticketType)
        }
    }


    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var ticketPriceInEtherView = view.findViewById<TextView>(R.id.ticketPriceInEtherView)
        var ticketPriceInUsdView = view.findViewById<TextView>(R.id.ticketPriceInUsdView)
        var ticketSupplyTextView = view.findViewById<TextView>(R.id.ticketSupplyTextView)
        var ticketRefundable = view.findViewById<TextView>(R.id.ticketRefundableTextView)
        var buyTicketBtn = view.findViewById<Button>(R.id.buyTicketButton)
    }
}