package com.ivanasen.smarttickets.ui.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.LatLng
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.models.Event
import com.ivanasen.smarttickets.ui.activities.DiscoverEventDetailActivity.Companion.EXTRA_EVENT_ID
import com.ivanasen.smarttickets.ui.adapters.TicketTypeManageAdapter
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.CONTRACT_TRUE
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.activity_manage_event_detail.*

import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.DateFormat.*
import java.text.DecimalFormat
import java.util.*

class ManageEventDetailActivity : AppCompatActivity() {

    private val LOG_TAG = ManageEventDetailActivity::class.simpleName

    private val mViewModel: AppViewModel by lazy {
        ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    private val mEvent: Event by lazy {
        val eventId = intent.extras.getLong(EXTRA_EVENT_ID)
        mViewModel.myEvents.value?.filter { it.eventId == eventId }!![0]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_event_detail)
        setSupportActionBar(toolbar)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manage_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.cancel_event_action -> {
                showCancelDialog()
                true
            }
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showCancelDialog() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.cancel_event_title))
                .content(getString(R.string.cancel_event_warning))
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no)
                .onPositive({ _, _ ->
                    attemptCancelEvent()
                })
                .show()
    }

    private fun attemptCancelEvent() {
        mViewModel.cancelEvent(mEvent.eventId).observe(this, Observer {
            when (it) {
                Utility.Companion.TransactionStatus.PENDING -> {
                    Toast.makeText(this,
                            getString(R.string.cancelling_event_pending_msg),
                            Toast.LENGTH_LONG)
                            .show()
                }

                Utility.Companion.TransactionStatus.SUCCESS -> {
                    Toast.makeText(this,
                            getString(R.string.cancelling_event_success_msg),
                            Toast.LENGTH_LONG)
                            .show()
                }

                Utility.Companion.TransactionStatus.FAILURE,
                Utility.Companion.TransactionStatus.ERROR -> {
                    Toast.makeText(this,
                            getString(R.string.cancelling_event_error_msg),
                            Toast.LENGTH_LONG)
                            .show()
                }
            }
        })
    }

    private fun setupViews() {
        title = mEvent.name

        if (mEvent.thumbnailHash.isNotEmpty()) {
            val imageUrl = Utility.getIpfsImageUrl(mEvent.thumbnailHash)
            Glide.with(this)
                    .load(imageUrl)
                    .apply(RequestOptions()
                            .centerCrop())
                    .into(eventImage)
        }

        val eventTimestamp = mEvent.timestamp
        val formatDate = getDateTimeInstance(MEDIUM, SHORT).format(eventTimestamp)
        eventTimeView.text = formatDate

        eventLocationView.text = mEvent.locationAddress
        eventLocationView.onClick { startMapsActivity(mEvent.latLong) }

        eventDescriptionView.text = mEvent.description

        val ticketTypes = mEvent.tickets
        val adapter = TicketTypeManageAdapter(this, ticketTypes)
        ticketTypesRecyclerView.adapter = adapter
        ticketTypesRecyclerView.layoutManager = LinearLayoutManager(this)

        if (mEvent.cancelled.toInt() == CONTRACT_TRUE) {
            eventCancelledView.visibility = View.VISIBLE
            eventEarningsContainer.visibility = View.GONE
            withdrawalBtn.visibility = View.GONE
            return
        } else {
            eventCancelledView.visibility = View.GONE
            eventEarningsContainer.visibility = View.VISIBLE
            withdrawalBtn.visibility = View.VISIBLE
        }

        val earningsInWei = mEvent.earnings
        val earningsInEther = earningsInWei.toDouble() / Utility.ONE_ETHER_IN_WEI
        mViewModel.convertEtherToUsd(earningsInWei).observe(this, Observer {
            it?.let {
                val usdDollars = it / 100
                val dfUsd = DecimalFormat(getString(R.string.usd_format))
                val formattedUsd = String.format(getString(R.string.earnings_usd_format,
                        dfUsd.format(usdDollars)))
                eventEarningsUsd.text = formattedUsd
            }
        })

        val dfEth = DecimalFormat(getString(R.string.eth_format))
        val formattedEther = String.format(getString(R.string.earnings_eth_format,
                dfEth.format(earningsInEther)))

        eventEarningsEther.text = formattedEther

        if (mEvent.timestamp > Calendar.getInstance().timeInMillis) {
            withdrawalBtn.visibility = View.GONE
            eventNotPassedView.visibility = View.VISIBLE
        } else {
            withdrawalBtn.visibility = View.VISIBLE
            eventNotPassedView.visibility = View.GONE

            withdrawalBtn.onClick {
                attemptWithdrawalFunds()
            }
        }
    }

    private fun attemptWithdrawalFunds() {
        if (mEvent.timestamp > Calendar.getInstance().timeInMillis) {
            Toast.makeText(this@ManageEventDetailActivity,
                    getString(R.string.event_should_have_passed_msg),
                    Toast.LENGTH_LONG)
                    .show()
            return
        }

        mViewModel.attemptWithdrawalFunds(mEvent.eventId)
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
                        Utility.Companion.TransactionStatus.FAILURE,
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
