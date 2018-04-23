package com.ivanasen.smarttickets.ui.activities

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.db.models.TicketType
import com.ivanasen.smarttickets.ui.activities.DiscoverEventDetailActivity.Companion.EXTRA_EVENT_ID
import com.ivanasen.smarttickets.ui.adapters.TicketTypeAdapter
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.activity_manage_event_detail.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.DateFormat.MEDIUM
import java.text.DateFormat.getDateTimeInstance
import java.text.DecimalFormat
import java.util.*
import java.util.Calendar.SHORT

class ManageEventDetailActivity : AppCompatActivity() {

    private val mViewModel: AppViewModel by lazy {
        ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_event_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViews()
    }

    private fun startMapsActivity(latLong: LatLng) {
        val mapsIntent = Intent(this,
                MapsActivity::class.java)
        val extras = Bundle()
        extras.putParcelable(MapsActivity.LAT_LONG_EXTRA_KEY, latLong)
        mapsIntent.putExtras(extras)
        startActivity(mapsIntent)
    }

    @SuppressLint("SetTextI18n")
    private fun setupViews() {
        val eventId = intent.extras.getLong(EXTRA_EVENT_ID)
        val event = mViewModel.events.value?.filter { it.eventId == eventId }!![0]

        title = event.name

        val imageUrl = Utility.getIpfsImageUrl(event.images[0])
        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(eventImage)

        val attemptBuyTicket: (ticketType: TicketType) -> Unit = {
            mViewModel.attemptToBuyTicket(it)
                    .observe(this, Observer {
                        when (it) {
                            Utility.Companion.TransactionStatus.PENDING -> {
                                Toast.makeText(applicationContext,
                                        getString(R.string.buying_ticket_text),
                                        Toast.LENGTH_LONG)
                                        .show()
                            }
                            Utility.Companion.TransactionStatus.SUCCESS -> {
                                MaterialDialog.Builder(this)
                                        .title(getString(R.string.ticket_success_title))
                                        .content(getString(R.string.ticket_success))
                                        .positiveText(getString(R.string.OK))
                                        .show()
                            }
                            Utility.Companion.TransactionStatus.FAILURE -> {

                            }
                            Utility.Companion.TransactionStatus.ERROR -> {
                                Toast.makeText(applicationContext,
                                        getString(R.string.tx_error_text),
                                        Toast.LENGTH_LONG)
                                        .show()
                            }
                        }
                    })
        }

        val eventTimeInMillis = event.timestamp * 1000
        val formatDate = getDateTimeInstance(MEDIUM, SHORT).format(eventTimeInMillis)
        eventTimeView.text = formatDate

        eventLocationView.text = event.locationAddress
        eventLocationView.onClick { startMapsActivity(event.latLong) }

        eventDescriptionView.text = event.description

        val ticketTypes = event.tickets
        val adapter = TicketTypeAdapter(this, ticketTypes, attemptBuyTicket)
        ticketTypesRecyclerView.adapter = adapter
        ticketTypesRecyclerView.layoutManager = LinearLayoutManager(this)

        val earningsInWei = event.earnings
        val earningsInEther = earningsInWei.toDouble() / Utility.ONE_ETHER_IN_WEI
        mViewModel.convertEtherToUsd(earningsInWei).observe(this, Observer {
            val dfUsd = DecimalFormat(getString(R.string.usd_format))
            val formattedUsd = String.format(getString(R.string.earnings_usd_format,
                    dfUsd.format(it)))
            eventEarningsUsd.text = formattedUsd
        })

        val dfEth = DecimalFormat(getString(R.string.eth_format))
        val formattedEther = String.format(getString(R.string.earnings_eth_format,
                dfEth.format(earningsInEther)))

        eventEarningsEther.text = formattedEther

        withdrawalBtn.onClick {
            if (eventTimeInMillis > Calendar.getInstance().timeInMillis) {
                Toast.makeText(this@ManageEventDetailActivity,
                        getString(R.string.event_should_have_passed_msg),
                        Toast.LENGTH_LONG)
                        .show()
                return@onClick
            }

            mViewModel.attemptWithdrawalFunds(eventId)
                    .observe(this@ManageEventDetailActivity, Observer {
                        when (it) {
                            Utility.Companion.TransactionStatus.PENDING -> {
                                Toast.makeText(
                                        this@ManageEventDetailActivity,
                                        getString(R.string.withdrawal_funds_pending),
                                        Toast.LENGTH_LONG)
                                        .show()
                            }
                            Utility.Companion.TransactionStatus.SUCCESS -> {
                                MaterialDialog.Builder(this@ManageEventDetailActivity)
                                        .content(getString(R.string.withdrawal_funds_success))
                                        .positiveText(getString(R.string.OK))
                                        .show()
                            }
                            Utility.Companion.TransactionStatus.ERROR -> {
                                Toast.makeText(
                                        this@ManageEventDetailActivity,
                                        getString(R.string.withdrawal_funds_error),
                                        Toast.LENGTH_LONG)
                                        .show()
                            }
                        }
                    })
        }
    }
}
