package com.ivanasen.smarttickets.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.ivanasen.smarttickets.api.ApplicationApi
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

    val currentEventsSortIndex: MutableLiveData<Int> = MutableLiveData()

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

    fun fetchEvents(order: String = ApplicationApi.EVENT_ORDER_POPULARITY,
                    page: Int = ApplicationApi.EVENT_PAGE_DEFAULT,
                    limit: Int = ApplicationApi.EVENT_LIMIT_DEFAULT) {
        mRepository.fetchEvents(order, page, limit)
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

    fun fetchMyEvents(): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.fetchMyEvents()
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

    fun createTicketValidationCode(ticket: Ticket): LiveData<String> {
        return mRepository.createTicketValidationCode(ticket)
    }

    fun verifyTicket(qrCodeString: String): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.validateTicket(qrCodeString)
    }

    fun fetchTxHistory(): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.fetchTxHistory()
    }

    fun convertUsdCentsToEther(usdCents: BigInteger): LiveData<BigDecimal> {
        return mRepository.convertUsdCentsToEther(usdCents)
    }

    fun cancelEvent(eventId: Long): LiveData<Utility.Companion.TransactionStatus> {
        return mRepository.cancelEvent(eventId.toBigInteger())
    }
}