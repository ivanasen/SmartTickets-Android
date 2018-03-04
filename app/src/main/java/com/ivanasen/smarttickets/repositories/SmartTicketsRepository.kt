package com.ivanasen.smarttickets.repositories

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.ivanasen.smarttickets.api.SmartTicketsContractProvider

import com.ivanasen.smarttickets.api.SmartTicketsIPFSApi
import com.ivanasen.smarttickets.api.contractwrappers.SmartTicketsCore
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.util.Utility
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
import rx.Subscription
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.sql.Time
import java.util.*


object SmartTicketsRepository {

    private val LOG_TAG = SmartTicketsRepository::class.simpleName
    private const val DATA_FETCH_PERIOD_MILLIS: Long = 6000
    private val mWeb3: Web3j = Web3JProvider.instance
    private val mIpfsApi: SmartTicketsIPFSApi = SmartTicketsIPFSApi.instance


    private lateinit var mContract: SmartTicketsCore
    private lateinit var mTxSubscription: Subscription

    var credentials: MutableLiveData<Credentials> = MutableLiveData()
    var unlockedWallet: MutableLiveData<Boolean> = MutableLiveData()

    var contractDeployed: MutableLiveData<Boolean> = MutableLiveData()

    val etherBalance: MutableLiveData<Double> = MutableLiveData()
    val usdBalance: MutableLiveData<Double> = MutableLiveData()

    fun getAddressBalance(address: String) {}


    fun createEvent(context: Context, event: Event) {
        launch(UI) {
            bg {
                try {
                    val eventMetadataHash = postEventToIpfs(event)

                    val eventTxReceipt = mContract.createEvent(BigInteger.valueOf(event.date.time),
                            eventMetadataHash).send()
                    Log.d(LOG_TAG, eventTxReceipt.transactionHash)
                } catch (e: Exception) {
                    Log.e(LOG_TAG, e.message)
                }
            }

        }
    }

    private fun postEventToIpfs(event: Event): ByteArray {
        val response = mIpfsApi.postEvent(event).execute()
        return response.headers().get(Utility.IPFS_HASH_HEADER)
                .toString()
                .toByteArray()
    }

    private fun uploadImage(drawable: BitmapDrawable) {
        val bitmap = drawable.bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bitmapData = stream.toByteArray()

        val imageResponse = mIpfsApi.postImage(bitmapData).execute()
        val imageHash = imageResponse.headers().get("ipfs-hash")
        Log.d(LOG_TAG, "Image IPFS hash: $imageHash")
    }
//
//    fun addTicketForEvent(): LiveData<Event> {
//
//    }
//
//    fun modifyEvent(): LiveData<Event> {
//
//    }
//
//    fun getEvent(id: BigInteger): LiveData<String> {
//
//    }

    fun loadInitialAppData() {
        createContractInstance()
        fetchDataRepeatedly()
    }

    private fun fetchDataRepeatedly() {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                fetchData()
            }
        }, 0, DATA_FETCH_PERIOD_MILLIS)
    }

    private fun fetchData() {
        bg {
            fetchEtherBalance()
        }
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


    private fun createContractInstance() {
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

    private fun fetchEtherBalance() {
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
            Log.d(LOG_TAG, "Fetch ether called!")
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

    fun sendEtherTo(address: String, etherAmount: Double) {
        require(credentials.value != null)
        launch(UI) {
            val txReceipt = bg { WalletUtil.sendEther(credentials.value!!, etherAmount, address) }
            Log.d(LOG_TAG, "Tx: ${txReceipt.await()}")
            fetchEtherBalance()
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