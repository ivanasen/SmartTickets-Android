package com.ivanasen.smarttickets.repository

import android.net.Credentials
import java.math.BigInteger


class SmartTicketsRepository {

    companion object {
        val instance = SmartTicketsRepository()
    }

    fun getAddressBalance(address: String) {}

    fun sendEther(from: Credentials, to: String, amountEther: BigInteger) {}

    fun deploySmartTickets() {}

    fun buyTicket() {}

    fun createEvent() {}

    fun addTicketForEvent() {}

    fun modifyEvent() {}

    fun getEventMetaData() {}

    fun getEvent() {}

    fun getTicket() {}

    fun getEvents() {}

    fun getTicketsForAddress() {}

    fun getEventsForAddress() {}

    fun getEventsForArea() {}

    fun createWallet() {}

    fun loadWallet() {}
}