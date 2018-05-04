package com.ivanasen.smarttickets.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.content.Context
import com.ivanasen.smarttickets.models.Event
import com.ivanasen.smarttickets.models.Ticket
import com.ivanasen.smarttickets.models.TicketType
import com.ivanasen.smarttickets.models.Transaction
import com.ivanasen.smarttickets.repositories.ApplicationRepository
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.WALLET_FILE_NAME_KEY
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger


class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository = ApplicationRepository

    val credentials = mRepository.credentials


    val etherBalance: LiveData<BigDecimal> = mRepository.etherBalance
    val usdBalance: LiveData<Double> = mRepository.usdBalance

    val myEvents: LiveData<MutableList<Event>> = mRepository.createdEvents
    val events: LiveData<MutableList<Event>> = mRepository.events
    val tickets: LiveData<MutableList<Ticket>> = mRepository.tickets
    val txHistory: LiveData<List<Transaction>> = mRepository.txHistory

    val eventsFetchStatus: LiveData<Utility.Companion.TransactionStatus> =
            mRepository.eventsFetchStatus
    val ticketsFetchStatus: LiveData<Utility.Companion.TransactionStatus> =
            mRepository.ticketsFetchStatus
    val myEventsFetchStatus: LiveData<Utility.Companion.TransactionStatus> =
            mRepository.myEventsFetchStatus

//    public fun refreshEvents(): LiveData<List<IPFSEvent>> {
//
//    }
//
//    public fun getTicketsForUser(): LiveData<List<TicketType>> {
//
//    }

    fun checkPassword(context: Context, password: String): Boolean {
        val walletName = context.applicationContext.defaultSharedPreferences.getString(WALLET_FILE_NAME_KEY, "")
        require(walletName != "")

        val wallet = File(context.filesDir, walletName)
        require(wallet.exists())
        return mRepository.checkPassword(password, wallet)
    }

    fun sendEther(address: String, amount: Double) {
        mRepository.sendEtherTo(address, amount)
    }

    fun refreshEvents() {
        mRepository.fetchEvents()
    }

    fun attemptToBuyTicket(ticketType: TicketType): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.buyTicket(ticketType)
    }

    fun fetchBalance() {
        mRepository.fetchWalletData()
    }

    fun fetchMyEvents() {
        mRepository.fetchMyEvents()
    }

    fun refreshTickets() {
        return mRepository.fetchTickets()
    }

    fun attemptSellTicket(ticket: Ticket): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.sellTicket(ticket)
    }

    fun attemptWithdrawalFunds(eventId: Long): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.withdrawalFunds(eventId)
    }

    fun convertEtherToUsd(weiValue: BigInteger): LiveData<Double> {
        return mRepository.convertEtherToUsd(weiValue)
    }

    fun signTicketNfcMessage(ticket: Ticket): String {
        // TODO: Not implemented
//        return mRepository.signTicketMessage(ticket)
        return ""
    }

    fun createTicketValidationCode(ticket: Ticket): LiveData<String> {
        return mRepository.createTicketValidationCode(ticket)
    }

    fun verifyTicket(qrCodeString: String): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.validateTicket(qrCodeString)
    }

    fun fetchTxHistory(): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.fetchTxHistory()
    }
}