package com.ivanasen.smarttickets.repository

import android.arch.lifecycle.MutableLiveData
import android.util.Log

import com.ivanasen.smarttickets.api.SmartTicketsIPFSApi
import com.ivanasen.smarttickets.api.SmartTicketsContractProvider
import com.ivanasen.smarttickets.api.contractwrappers.SmartTicketsCore
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.util.WalletUtil
import com.ivanasen.smarttickets.util.Web3JProvider
import org.jetbrains.anko.coroutines.experimental.bg
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import java.io.File
import java.sql.Time


object SmartTicketsRepository {

    private val LOG_TAG = SmartTicketsRepository::class.simpleName
    private val mWeb3: Web3j = Web3JProvider.instance
    private val mIpfsApi: SmartTicketsIPFSApi = SmartTicketsIPFSApi.instance


    private lateinit var mContract: SmartTicketsCore

    var credentials: MutableLiveData<Credentials> = MutableLiveData()
    var unlockedWallet: MutableLiveData<Boolean> = MutableLiveData()

    fun getAddressBalance(address: String) {}

    fun sendEther(wallet: File, password: String, to: String, amountEther: Double) {
        val sendEther = bg {
            WalletUtil.sendEther(wallet, password, amountEther, to)
        }
    }

//    fun deploySmartTickets(wallet: File, password: String): SmartTicketsCore {
//        WalletUtil.deploySmartTickets(wallet, password)
//    }
//
//    fun buyTicket(): LiveData<Boolean> {
//    }
//
//    fun createEvent(): LiveData<Event> {
//
//    }
//
//    fun addTicketForEvent(): LiveData<Event> {
//
//    }
//
//    fun modifyEvent(): LiveData<Event> {
//
//    }
//
//    fun getEventMetaData(id: BigInteger): LiveData<String> {
//
//    }

    fun getEvent(id: Int): Event {
        val event = mContract.getEvent(id.toBigInteger()).sendAsync().get()
        Log.d(LOG_TAG, "Event: ${event.value1} ${event.value1}")
        return Event(id.toBigInteger(),
                Hex.toHexString(event.value2),
                Time(event.value1.toLong()),
                emptyList(),
                emptyList(),
                true)
    }

    fun getCeoAddress() {
        Log.d(LOG_TAG, "CeoAddress: ${mContract.ceoAddress().sendAsync().get()}")
    }

    fun createContractInstance() {
        mContract = SmartTicketsContractProvider.provide(mWeb3, credentials.value!!)
    }

    fun unlockWallet(password: String, wallet: File): Boolean {
        return try {
            credentials.postValue(WalletUtils.loadCredentials(password, wallet))
            unlockedWallet.postValue(true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            unlockedWallet.postValue(false)
            false
        }
    }

    fun createWallet(password: String, destinationDirectory: File): String {
        return WalletUtils.generateNewWalletFile(password, destinationDirectory, false)
    }


//    fun getTicket(): LiveData<Ticket> {
//
//    }
//
//    fun getEvents(): LiveData<List<Event>> {
//
//    }
//
//    fun getTicketsForAddress(): LiveData<List<Ticket>> {
//
//    }
//
//    fun getEventsForAddress(): LiveData<List<Event>> {
//
//    }
//
//    fun getEventsForArea(lat: Double, long: Double): LiveData<List<Event>> {
//
//    }
//

//
//    fun loadWallet() {}
}