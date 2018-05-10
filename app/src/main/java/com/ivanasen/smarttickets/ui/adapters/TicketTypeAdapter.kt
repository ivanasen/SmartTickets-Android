package com.ivanasen.smarttickets.ui.adapters

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.models.TicketType
import com.ivanasen.smarttickets.repositories.ApplicationRepository
import com.ivanasen.smarttickets.util.Utility.Companion.CONTRACT_TRUE
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.DecimalFormat

internal class TicketTypeAdapter(private val context: Context,
                                 private val data: List<TicketType>,
                                 private val usdBalance: Double,
                                 private val onTicketBuyListener: (ticketType: TicketType) -> Unit)
    : RecyclerView.Adapter<TicketTypeAdapter.ViewHolder>() {

    private val ethFormat = DecimalFormat(context.getString(R.string.eth_format))
    private val usdFormat = DecimalFormat(context.getString(R.string.usd_format))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ticket_type,
                parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticketType = data[position]

        val (_, _, priceInUSDCents, _, currentSupply, refundable) = ticketType

        ApplicationRepository.fetchEtherValueOfUsd(priceInUSDCents.toBigDecimal())
                .observe(context as LifecycleOwner, Observer {
                    holder.ticketPriceInEtherView.text = ethFormat.format(it)
                })

        holder.ticketPriceInUsdView.text = usdFormat.format(priceInUSDCents.toDouble() / 100)
        holder.ticketSupplyTextView.text =
                String.format(context.getString(R.string.tickets_remaining_text), currentSupply)
        holder.ticketRefundable.text =
                if (refundable == CONTRACT_TRUE.toBigInteger())
                    context.getString(R.string.refundable_text)
                else
                    context.getString(R.string.not_refundable_text)


        if (usdBalance > ticketType.priceInUSDCents.toInt()) {
            holder.insufficientFundsView.visibility = View.GONE
            holder.buyTicketBtn.visibility = View.VISIBLE
            holder.buyTicketBtn.onClick {
                onTicketBuyListener(ticketType)
            }
        } else {
            holder.insufficientFundsView.visibility = View.VISIBLE
            holder.buyTicketBtn.visibility = View.GONE
        }
    }


    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val ticketPriceInEtherView: TextView = view.findViewById(R.id.ticketPriceInEtherView)
        val ticketPriceInUsdView: TextView = view.findViewById(R.id.ticketPriceInUsdView)
        val ticketSupplyTextView: TextView = view.findViewById(R.id.ticketSupplyTextView)
        val ticketRefundable: TextView = view.findViewById(R.id.ticketRefundableTextView)
        val buyTicketBtn: Button = view.findViewById(R.id.buyTicketButton)
        val insufficientFundsView: TextView = view.findViewById(R.id.insufficientFundsView)
    }
}