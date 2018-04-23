package com.ivanasen.smarttickets.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.content.Context
import com.google.android.gms.location.places.Place
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.db.models.Ticket
import com.ivanasen.smarttickets.db.models.TicketType
import com.ivanasen.smarttickets.repositories.SmartTicketsRepository
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.WALLET_FILE_NAME_KEY
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger


class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository = SmartTicketsRepository

    val credentials = mRepository.credentials


    var contractExists = mRepository.contractDeployed

    val etherBalance: LiveData<BigDecimal> = mRepository.etherBalance
    val usdBalance: LiveData<Double> = mRepository.usdBalance

    val myEvents: LiveData<MutableList<Event>> = mRepository.createdEvents
    val events: LiveData<MutableList<Event>> = mRepository.events
    val tickets: LiveData<MutableList<Ticket>> = mRepository.tickets

    val areEventsFetched: LiveData<Utility.Companion.TransactionStatus> =
            mRepository.eventsFetchStatus
    val areTicketsFetched: LiveData<Utility.Companion.TransactionStatus> =
            mRepository.areTicketsFetched

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

    fun onPlacePicked(place: Place?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun refreshEvents() {
        mRepository.fetchEvents()
    }

    fun fetchEvent(eventId: Long): LiveData<Event> {
        return mRepository.fetchEvent(eventId)
    }

    fun fetchTicketTypesForEvent(eventId: Long): LiveData<MutableList<TicketType>> {
        return mRepository.fetchTicketTypesForEvent(eventId)
    }

    fun attemptToBuyTicket(ticketType: TicketType): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.buyTicket(ticketType)
    }

    fun fetchBalance() {
        mRepository.fetchBalance()
    }

    fun fetchMyEvents() {
        mRepository.fetchMyEvents()
    }

    fun refreshTickets() {
        return mRepository.fetchTickets()
    }

    fun loadInitialAppData() {
        mRepository.loadInitialAppData()
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
//        return mRepository.signTicketMessage(ticket)
        return ""
    }

    fun createTicketValidationCode(ticket: Ticket): LiveData<String> {
        return mRepository.createTicketValidationCode(ticket)
    }

    fun verifyTicket(qrCodeString: String): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.validateTicket(qrCodeString)
    }
}