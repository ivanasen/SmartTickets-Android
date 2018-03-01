package com.ivanasen.smarttickets.repository

import android.arch.lifecycle.MutableLiveData
import android.util.Log

import com.ivanasen.smarttickets.api.SmartTicketsIPFSApi
import com.ivanasen.smarttickets.api.SmartTicketsContractProvider
import com.ivanasen.smarttickets.api.contractwrappers.SmartTicketsCore
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.util.Utility.Companion.INFURA_ETHER_PRICE_IN_USD_URL
import com.ivanasen.smarttickets.util.Utility.Companion.ONE_ETHER_IN_WEI
import com.ivanasen.smarttickets.util.WalletUtil
import com.ivanasen.smarttickets.util.Web3JProvider
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg
import org.json.JSONObject
import org.spongycastle.util.encoders.Hex
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import java.io.File
import java.math.BigDecimal
import java.net.URL
import java.sql.Time


object SmartTicketsRepository {

    private val LOG_TAG = SmartTicketsRepository::class.simpleName
    private val mWeb3: Web3j = Web3JProvider.instance
    private val mIpfsApi: SmartTicketsIPFSApi = SmartTicketsIPFSApi.instance


    private lateinit var mContract: SmartTicketsCore

    var credentials: MutableLiveData<Credentials> = MutableLiveData()
    var unlockedWallet: MutableLiveData<Boolean> = MutableLiveData()

    var contractDeployed: MutableLiveData<Boolean> = MutableLiveData()

    val etherBalance: MutableLiveData<Double> = MutableLiveData()
    val usdBalance: MutableLiveData<Double> = MutableLiveData()

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

    fun loadInitialAppData() {
        createContractInstance()
        getEtherBalance()
    }

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


    fun createContractInstance() {
        launch(UI) {
            val contract = bg { SmartTicketsContractProvider.provide(mWeb3, credentials.value!!) }
            mContract = contract.await()
            try {
                val isValid = bg { mContract.isValid }
                contractDeployed.postValue(isValid.await())
            } catch (e: Exception) {
                e.printStackTrace()
                contractDeployed.postValue(false)
            }
        }
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

    private fun getEtherBalance() {
        launch(UI) {
            val balanceInWei = bg {
                val request = mWeb3.ethGetBalance(credentials.value?.address,
                        DefaultBlockParameterName.LATEST).send()
                request.balance
            }
            val balanceInEther = balanceInWei.await().toBigDecimal()
                    .divide(BigDecimal.valueOf(ONE_ETHER_IN_WEI))
            etherBalance.postValue(balanceInEther.toDouble())
            getUsdValueOfEther(balanceInEther.toDouble())
        }
    }

    private fun getUsdValueOfEther(ether: Double) {
        launch(UI) {
            val result = bg { URL(INFURA_ETHER_PRICE_IN_USD_URL).readText() }.await()
            val resultObj = JSONObject(result)
            val askPrice = resultObj.getDouble("ask")
            usdBalance.postValue(askPrice * ether)
        }
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