package com.ivanasen.smarttickets.repositories

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.ivanasen.smarttickets.contractwrappers.SmartTicketsContractProvider

import com.ivanasen.smarttickets.api.SmartTicketsIPFSApi
import com.ivanasen.smarttickets.contractwrappers.SmartTickets
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.db.models.IPFSEvent
import com.ivanasen.smarttickets.db.models.TicketType
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.INFURA_ETHER_PRICE_IN_USD_URL
import com.ivanasen.smarttickets.util.Utility.Companion.IPFS_HASH_HEADER
import com.ivanasen.smarttickets.util.Utility.Companion.ONE_ETHER_IN_WEI
import com.ivanasen.smarttickets.util.WalletUtil
import com.ivanasen.smarttickets.util.Web3JProvider
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.anko.coroutines.experimental.bg
import org.json.JSONObject
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.nio.charset.Charset
import java.util.*


object SmartTicketsRepository {
    private val LOG_TAG = SmartTicketsRepository::class.simpleName
    private const val DATA_FETCH_PERIOD_MILLIS: Long = 60000
    private val mWeb3: Web3j = Web3JProvider.instance
    private val mIpfsApi: SmartTicketsIPFSApi = SmartTicketsIPFSApi.instance


    private lateinit var mContract: SmartTickets

    var credentials: MutableLiveData<Credentials> = MutableLiveData()
    var unlockedWallet: MutableLiveData<Boolean> = MutableLiveData()

    var contractDeployed: MutableLiveData<Boolean> = MutableLiveData()

    val etherBalance: MutableLiveData<Double> = MutableLiveData()
    val usdBalance: MutableLiveData<Double> = MutableLiveData()

    val events: MutableLiveData<MutableList<Event>> = MutableLiveData()

    fun createEvent(name: String,
                    description: String,
                    timestamp: Long,
                    latLong: LatLng,
                    locationName: String,
                    locationAddress: String,
                    imagePaths: List<String>,
                    tickets: List<TicketType>) {
        launch(UI) {
            bg {
                try {
                    val imageHashes = uploadImages(imagePaths)

                    val event = IPFSEvent(name, description, timestamp, latLong, locationName,
                            locationAddress, imageHashes, tickets)
                    val eventMetadataHash = postEventToIpfs(event)

                    Log.d(LOG_TAG, eventMetadataHash.toString(Charset.forName("UTF-8")))

                    val ticketPrices = tickets.map { it.priceInUSDCents }
                    val ticketSupplies = tickets.map { it.initialSupply }
                    val ticketRefundables = tickets.map {
                        BigInteger.valueOf(if (it.refundable) 1 else 0)
                    }

                    val eventTxReceipt = mContract.createEvent(BigInteger.valueOf(timestamp),
                            eventMetadataHash, ticketPrices, ticketSupplies, ticketRefundables).send()
                    Log.d(LOG_TAG, eventTxReceipt.transactionHash)

                } catch (e: Exception) {
                    Log.e(LOG_TAG, e.message)
                }
            }
        }
    }

    private fun addTicketTypeForEvent(eventId: BigInteger, ticketType: TicketType) {
        launch(UI) {
            val txReceipt = bg {
                mContract.addTicketForEvent(
                        eventId,
                        ticketType.priceInUSDCents,
                        ticketType.initialSupply,
                        BigInteger.valueOf(if (ticketType.refundable) 1 else 0)
                ).send()
            }

        }
    }

    private fun postEventToIpfs(event: IPFSEvent): ByteArray {
        val response = mIpfsApi.postEvent(event).execute()
        return response.headers().get(Utility.IPFS_HASH_HEADER)
                .toString()
                .toByteArray()
    }

    private fun uploadImage(path: String): String? {
        val file = File(path)
        val requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)

        val imageResponse = mIpfsApi.postImage(requestBody).execute()
        return imageResponse.headers().get(IPFS_HASH_HEADER)
    }

    private fun uploadImages(imagePaths: List<String>): List<String> {
        val imageHashes = mutableListOf<String>()
        imagePaths.forEach {
            val hash = uploadImage(it)
            hash?.let { imageHashes.add(it) }
        }
        return imageHashes
    }

//
//    fun addTicketForEvent(): LiveData<IPFSEvent> {
//
//    }
//
//    fun modifyEvent(): LiveData<IPFSEvent> {
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

//    fun getEvent(id: Int): IPFSEvent {
//        val event = mContract.getEvent(id.toBigInteger()).sendAsync().get()
//        Log.d(LOG_TAG, "IPFSEvent: ${event.value1} ${event.value1}")
//        return IPFSEvent(id.toBigInteger(),
//                Hex.toHexString(event.value2),
//                Time(event.value1.toLong()),
//                emptyList(),
//                emptyList(),
//                true)
//    }


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


//    fun getTicket(): LiveData<TicketType> {
//
//    }
//


    fun fetchEvents() {
        bg {
            val newEvents = mutableListOf<Event>()
            val eventCount = mContract.eventCount.send().toLong()

            // We start from index 1 because at index 0 is the Genesis Event of the contract
            for (i in 1 until eventCount + 1) {
                val event = mContract.getEvent(BigInteger.valueOf(i)).send()

                val timestamp = event.value1.toLong()
                val ipfsHash = event.value2.toString(Charset.forName("UTF-8"))
                val cancelled = event.value3

                val eventData: IPFSEvent? = getEventFromIPFS(ipfsHash)

                if (cancelled.toInt() == 0 && eventData != null &&
                        eventData.name != null &&
                        eventData.latLong != null &&
                        eventData.locationAddress != null &&
                        eventData.locationName != null &&
                        eventData.tickets != null) {
                    val fullEvent = Event(
                            i,
                            ipfsHash,
                            eventData.name,
                            eventData.description!!,
                            timestamp,
                            eventData.latLong,
                            eventData.locationName,
                            eventData.locationAddress,
                            eventData.images!!,
                            eventData.tickets)

                    newEvents.add(fullEvent)
                }
            }
            events.postValue(newEvents)
        }
    }

    private fun getEventFromIPFS(ipfsHash: String): IPFSEvent? {
        val eventResponse = mIpfsApi.getEvent(ipfsHash).execute()
        return eventResponse.body()
    }

    private fun getImageFromIPFS(ipfsHash: String): Deferred<Bitmap?> {
        return bg {
            val imageResponse = mIpfsApi.getImage(ipfsHash).execute()
            imageResponse.body()
        }
    }

//
//    fun getTicketsForAddress(): LiveData<List<TicketType>> {
//
//    }
//
//    fun getEventsForAddress(): LiveData<List<IPFSEvent>> {
//
//    }
//
//    fun getEventsForArea(lat: Double, long: Double): LiveData<List<IPFSEvent>> {
//
//    }
//

//
//    fun loadWallet() {}
}