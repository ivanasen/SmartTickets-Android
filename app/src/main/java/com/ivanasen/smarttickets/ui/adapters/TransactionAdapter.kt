package com.ivanasen.smarttickets.ui.adapters

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.models.Transaction
import com.ivanasen.smarttickets.util.toEther
import org.jetbrains.anko.find
import java.text.DateFormat

class TransactionAdapter(activity: Context, private val txHistory: LiveData<List<Transaction>>)
    : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    private val LOG_TAG = TransactionAdapter::class.simpleName

    init {
        txHistory.observe(activity as LifecycleOwner, Observer { notifyDataSetChanged() })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_transaction,
                parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = txHistory.value?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        txHistory.value?.let {
            val tx = it[position]

            val type = tx.type
            val valueWei = tx.value
            val value = valueWei.toEther
            val valueUsd = tx.valueUsd
            val timestamp = tx.timeStamp
            val usdFormat = holder.view.context.getString(R.string.usd_format_sign)
            val ethFormat = holder.view.context.getString(R.string.eth_format_sign)
            val formattedDate = DateFormat
                    .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(timestamp)

            holder.txTypeTextView.text = type
            holder.txAmountEtherTextView.text = String.format(ethFormat, value)
            holder.txAmountUsdTextView.text = String.format(usdFormat, valueUsd)
            holder.txDateTextView.text = formattedDate
        }
    }

    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        val txTypeTextView = view.find<TextView>(R.id.txTypeTextView)
        val txDateTextView = view.find<TextView>(R.id.txDateTextView)
        val txAmountUsdTextView = view.find<TextView>(R.id.txAmountUsdTextView)
        val txAmountEtherTextView = view.find<TextView>(R.id.txAmountEtherTextView)
    }
}