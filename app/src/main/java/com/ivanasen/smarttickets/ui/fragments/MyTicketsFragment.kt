package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.Observer


import android.arch.lifecycle.ViewModelProviders
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.transition.Fade
import android.transition.TransitionManager
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.models.Ticket
import com.ivanasen.smarttickets.ui.adapters.TicketsAdapter
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.fragment_my_tickets.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import java.text.DateFormat
import android.nfc.NdefRecord
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.transition.Slide
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.ivanasen.smarttickets.ui.activities.TicketValidatorActivity
import com.ivanasen.smarttickets.util.Utility.Companion.launchActivity
import com.ivanasen.smarttickets.util.toPx
import org.jetbrains.anko.find


class MyTicketsFragment : Fragment(),
        NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback {

    private val LOG_TAG: String = MyTicketsFragment::class.java.simpleName
    private val QR_CODE_SIZE: Int = 160.toPx

    private val mViewModel: AppViewModel by lazy {
        if (activity != null)
            ViewModelProviders.of(activity as FragmentActivity).get(AppViewModel::class.java)
        else
            ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    private val ticketDetailView: View by lazy { activity!!.find<View>(R.id.ticketDetailView) }

    private val mNfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(context)
    }
    private lateinit var mCurrentTicket: Ticket

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = getString(R.string.title_my_tickets)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_my_tickets, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.my_tickets, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.navigation_validate_tickets -> {
                context?.let { launchActivity(it, TicketValidatorActivity::class.java) }
            }
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observeLiveData()
        setupViews()
        setupNfcAdapter()
    }

    private fun setupNfcAdapter() {
//        if (mNfcAdapter == null) {
//            Toast.makeText(context,
//                    "nfcAdapter==null, no NFC adapter exists",
//                    Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(context,
//                    "Set Callback(s)",
//                    Toast.LENGTH_LONG).show();
//            mNfcAdapter!!.setNdefPushMessageCallback(this, activity)
//            mNfcAdapter!!.setOnNdefPushCompleteCallback(this, activity)
//        }
    }




    private fun setupViews() {
        ticketsRefreshLayout.setColorSchemeColors(resources.getColor(R.color.pink),
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.pink))
        ticketsRefreshLayout.onRefresh {
            mViewModel.refreshTickets()
        }

        val adapter = TicketsAdapter(context, mViewModel.tickets,
                { ticket -> showTicketDetailView(ticket) })
        ticketsRecyclerView.layoutManager = LinearLayoutManager(context)
        ticketsRecyclerView.adapter = adapter


        ticketDetailView.onClick {
            TransitionManager.beginDelayedTransition(view as ViewGroup, Fade())
            ticketDetailView.visibility = View.GONE
        }
        val ticketCardView = ticketDetailView.find<View>(R.id.ticketCardView)
        ticketCardView.onClick {
            // Don't hide the ticket view view
        }
    }


    override fun createNdefMessage(event: NfcEvent?): NdefMessage {
        val signedMessage = mViewModel.signTicketNfcMessage(mCurrentTicket)
        val bytesOut = signedMessage.toByteArray()

        val ndefRecordOut = NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".toByteArray(),
                byteArrayOf(),
                bytesOut)

        return NdefMessage(ndefRecordOut)
    }

    override fun onNdefPushComplete(p0: NfcEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun showTicketDetailView(ticket: Ticket) {
        val ticketEventImageViewDetail =
                ticketDetailView.find<ImageView>(R.id.ticketEventImageViewDetail)
        val ticketQrCodeDetail = ticketDetailView.find<ImageView>(R.id.ticketQrCodeDetail)
        val ticketEventNameDetail =
                ticketDetailView.find<TextView>(R.id.ticketEventNameDetail)
        val ticketEventLocationDetail =
                ticketDetailView.find<TextView>(R.id.ticketEventLocationDetail)
        val eventDateViewDetail = ticketDetailView.find<TextView>(R.id.eventDateViewDetail)
        val eventTimeViewDetail = ticketDetailView.find<TextView>(R.id.eventTimeViewDetail)
        val ticketPriceInEtherViewDetail =
                ticketDetailView.find<TextView>(R.id.ticketPriceInEtherViewDetail)
        val ticketRefundDetailContainer =
                ticketDetailView.find<View>(R.id.ticketRefundDetailContainer)
        val ticketNotRefundableDetailTextView =
                ticketDetailView.find<TextView>(R.id.ticketNotRefundableDetailTextView)
        val ticketPriceInUsdViewDetail =
                ticketDetailView.find<TextView>(R.id.ticketPriceInUsdViewDetail)
        val refundTicketBtnDetail =
                ticketDetailView.find<Button>(R.id.refundTicketBtnDetail)


        mCurrentTicket = ticket
        val event = ticket.event

        TransitionManager.beginDelayedTransition(ticketDetailView.parent as ViewGroup, Slide())
        ticketDetailView.visibility = View.VISIBLE

        val imageUrl = Utility.getIpfsImageUrl(event.images[0])
        Glide.with(this)
                .load(imageUrl)
                .apply(RequestOptions()
                        .centerCrop())
                .into(ticketEventImageViewDetail)

        mViewModel.createTicketValidationCode(ticket).observe(this, Observer {
            it?.let {
                val ticketBitmap = QRCode.from(it)
                        .withSize(QR_CODE_SIZE, QR_CODE_SIZE)
                        .withColor(ContextCompat.getColor(context!!, R.color.pinkDark), 1)
                        .bitmap()
                ticketQrCodeDetail.imageBitmap = ticketBitmap
            }
        })

        ticketEventNameDetail.text = event.name
        ticketEventLocationDetail.text = event.locationName
        eventDateViewDetail.text = DateFormat.getDateInstance(DateFormat.MEDIUM)
                .format(event.timestamp)
        eventTimeViewDetail.text = DateFormat.getTimeInstance(DateFormat.MEDIUM)
                .format(event.timestamp)

        ticketPriceInEtherViewDetail.text =
                (ticket.ticketType.priceInUSDCents.toDouble() / 100).toString()

        if (ticket.ticketType.refundable == 1.toBigInteger()) {
            ticketRefundDetailContainer.visibility = View.VISIBLE
            ticketNotRefundableDetailTextView.visibility = View.GONE

            ticketPriceInUsdViewDetail.text =
                    (ticket.ticketType.priceInUSDCents.toDouble() / 100).toString()

            refundTicketBtnDetail.onClick {
                mViewModel.attemptSellTicket(ticket)
                        .observe(this@MyTicketsFragment, Observer {
                            when (it) {
                                Utility.Companion.TransactionStatus.PENDING -> {
                                    Toast.makeText(this@MyTicketsFragment.context,
                                            getString(R.string.selling_ticket_text),
                                            Toast.LENGTH_LONG)
                                            .show()
                                }
                                Utility.Companion.TransactionStatus.SUCCESS -> {
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
                                Utility.Companion.TransactionStatus.FAILURE -> TODO()
                            }
                        })
            }
        } else {
            ticketRefundDetailContainer.visibility = View.GONE
            ticketNotRefundableDetailTextView.visibility = View.VISIBLE
        }

    }

    private fun observeLiveData() {
        mViewModel.ticketsFetchStatus.observe(this, Observer {
            when (it) {
                Utility.Companion.TransactionStatus.PENDING -> {
                    ticketsRefreshLayout.isRefreshing = true
                }

                Utility.Companion.TransactionStatus.SUCCESS -> {
                    emptyViewLayout.visibility = View.GONE
                    ticketsRecyclerView.visibility = View.VISIBLE
                    ticketsRefreshLayout.isRefreshing = false
                }

                Utility.Companion.TransactionStatus.FAILURE,
                Utility.Companion.TransactionStatus.ERROR -> {
                    emptyViewLayout.visibility = View.VISIBLE
                    ticketsRecyclerView.visibility = View.GONE
                    ticketsRefreshLayout.isRefreshing = false
                }
            }
        })
    }
}
