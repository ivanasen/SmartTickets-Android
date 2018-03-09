package com.ivanasen.smarttickets.repositories

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.ivanasen.smarttickets.contractwrappers.SmartTicketsContractProvider

import com.ivanasen.smarttickets.api.SmartTicketsIPFSApi
import com.ivanasen.smarttickets.contractwrappers.SmartTickets
import com.ivanasen.smarttickets.db.models.*
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.IPFS_HASH_HEADER
import com.ivanasen.smarttickets.util.Utility.Companion.ONE_ETHER_IN_WEI
import com.ivanasen.smarttickets.util.WalletUtil
import com.ivanasen.smarttickets.util.Web3JProvider
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.anko.coroutines.experimental.bg
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tuples.generated.Tuple6
import java.io.File
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.Charset


object SmartTicketsRepository {
    private val LOG_TAG = SmartTicketsRepository::class.simpleName
    private const val DATA_FETCH_PERIOD_MILLIS: Long = 60000
    private val mWeb3: Web3j = Web3JProvider.instance
    private val mIpfsApi: SmartTicketsIPFSApi = SmartTicketsIPFSApi.instance


    private lateinit var mContract: SmartTickets

    var credentials: MutableLiveData<Credentials> = MutableLiveData()
    var unlockedWallet: MutableLiveData<Boolean> = MutableLiveData()

    var contractDeployed: MutableLiveData<Boolean> = MutableLiveData()

    val etherBalance: MutableLiveData<BigDecimal> = MutableLiveData()
    val usdBalance: MutableLiveData<Double> = MutableLiveData()

    val myEvents: MutableLiveData<MutableList<Event>> = MutableLiveData()
    val events: MutableLiveData<MutableList<Event>> = MutableLiveData()
    val tickets: MutableLiveData<MutableList<Ticket>> = MutableLiveData()

    fun createEvent(name: String,
                    description: String,
                    timestamp: Long,
                    latLong: LatLng,
                    locationName: String,
                    locationAddress: String,
                    imagePaths: List<String>,
                    tickets: List<TicketTypeIpfs>): LiveData<Utility.Companion.TransactionStatus> {
        val txStatusLiveData: MutableLiveData<Utility.Companion.TransactionStatus> =
                MutableLiveData()
        bg {
            try {
                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.PENDING)
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
                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.COMPLETE)
                Log.d(LOG_TAG, eventTxReceipt.transactionHash)

            } catch (e: Exception) {
                Log.e(LOG_TAG, e.message)
                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.ERROR)
            }

        }
        return txStatusLiveData
    }

    private fun addTicketTypeForEvent(eventId: BigInteger, ticketType: TicketType) {
        bg {
            val txReceipt = mContract.addTicketForEvent(
                    eventId,
                    ticketType.priceInUSDCents,
                    ticketType.initialSupply,
                    BigInteger.valueOf(if (ticketType.refundable) 1 else 0)
            ).send()
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
        initialiseContract()
    }

    private fun fetchContractData() {
        fetchTickets()
        fetchEvents()
        fetchBalance()
        fetchMyEvents()
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


    private fun initialiseContract() {
        bg {
            val contract = SmartTicketsContractProvider.provide(mWeb3, credentials.value!!)
            mContract = contract
            try {
                val isValid = mContract.isValid
                contractDeployed.postValue(isValid)

                fetchContractData()
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
        bg {
            val balanceInWei = getWeiBalance()

            val balanceInEther = balanceInWei.toBigDecimal()
                    .divide(BigDecimal.valueOf(ONE_ETHER_IN_WEI))
            etherBalance.postValue(balanceInEther)

            Log.d(LOG_TAG, "Fetch ether called!")
        }
    }

    private fun getWeiBalance(): BigInteger {
        val request = mWeb3.ethGetBalance(credentials.value?.address,
                DefaultBlockParameterName.LATEST).send()
        return request.balance
    }

    private fun fetchUsdBalance() {
        bg {
            val oneUsdCentInWei = mContract.oneUSDCentInWei.send()
            usdBalance.postValue((getWeiBalance() / oneUsdCentInWei).toDouble() / 100)
            Log.d(LOG_TAG, "Fetch usd called!")
        }
    }

    fun fetchEtherValueOfUsd(usdCents: BigDecimal): LiveData<BigDecimal> {
        val data = MutableLiveData<BigDecimal>()
        bg {
            val oneUsdCentInWei = mContract.oneUSDCentInWei.send().toBigDecimal()
            val oneEtherInWei = BigInteger.valueOf(ONE_ETHER_IN_WEI).toBigDecimal()
            data.postValue((oneUsdCentInWei * usdCents).divide(oneEtherInWei))
        }
        return data
    }

    fun sendEtherTo(address: String, etherAmount: Double) {
        bg {
            require(credentials.value != null)
            val txReceipt = WalletUtil.sendEther(credentials.value!!, etherAmount, address)
            Log.d(LOG_TAG, "Tx: $txReceipt")

            fetchBalance()
        }
    }

    fun fetchEvent(id: Long): LiveData<Event> {
        val eventLiveData: MutableLiveData<Event> = MutableLiveData()

        bg {
            val event = getEvent(id)
            eventLiveData.postValue(event)
        }

        return eventLiveData
    }


    private fun getEvent(id: Long): Event {
        val event = mContract.getEvent(BigInteger.valueOf(id)).send()

        val timestamp = event.value1.toLong()
        val ipfsHash = event.value2.toString(Charset.forName("UTF-8"))
        val cancelled = event.value3

        val eventData: IPFSEvent? = getEventFromIPFS(ipfsHash)

        val ticketTypes = getTicketTypesForEvent(id)

        if (cancelled.toInt() == 0 && eventData != null &&
                eventData.name != null &&
                eventData.latLong != null &&
                eventData.locationAddress != null &&
                eventData.locationName != null &&
                eventData.tickets != null) {

            return Event(
                    id,
                    ipfsHash,
                    eventData.name,
                    eventData.description!!,
                    timestamp,
                    eventData.latLong,
                    eventData.locationName,
                    eventData.locationAddress,
                    eventData.images!!,
                    ticketTypes)
        }

        throw IllegalArgumentException("Event not found")
    }

    fun fetchEvents() {
        bg {
            val newEvents = mutableListOf<Event>()
            val eventCount = mContract.eventCount.send().toLong()

            // We start from index 1 because at index 0 is the Genesis Event of the contract
            for (i in 1 until eventCount + 1) {
                val event = getEvent(i)
                newEvents.add(event)
                events.postValue(newEvents)
            }
        }
    }

    private fun getEventFromIPFS(ipfsHash: String): IPFSEvent? {
        val eventResponse = mIpfsApi.getEvent(ipfsHash).execute()
        return eventResponse.body()
    }


    fun fetchTicketTypesForEvent(eventId: Long): LiveData<MutableList<TicketType>> {
        val ticketTypes: MutableLiveData<MutableList<TicketType>> = MutableLiveData()
        bg {
            val ticketTypesForEvent = getTicketTypesForEvent(eventId)
            if (ticketTypesForEvent.isNotEmpty()) {
                ticketTypes.postValue(ticketTypesForEvent)
            }
        }
        return ticketTypes
    }

    private fun getTicketTypesForEvent(eventId: Long): MutableList<TicketType> {
        val count = mContract.getTicketTypesCountForEvent(BigInteger.valueOf(eventId))
                .send().toLong()
        val ticketTypesList = mutableListOf<TicketType>()
        for (ticketTypeIndex in 0 until count) {

            val ticketTypeTuple = mContract.getTicketTypeForEvent(BigInteger.valueOf(eventId),
                    BigInteger.valueOf(ticketTypeIndex)).send()

            ticketTypeTuple?.let {
                ticketTypesList.add(convertTupleToTicketType(it))
            }
        }
        return ticketTypesList
    }

    fun buyTicket(ticketType: TicketType): LiveData<Utility.Companion.TransactionStatus> {
        val txStatusLiveData: MutableLiveData<Utility.Companion.TransactionStatus> =
                MutableLiveData()

        bg {
            txStatusLiveData.postValue(Utility.Companion.TransactionStatus.PENDING)
            val oneUsdCentInWei = mContract.oneUSDCentInWei.send()
            val totalPrice = ticketType.priceInUSDCents * oneUsdCentInWei

            val balanceInWei = getWeiBalance()
            require(totalPrice < balanceInWei)

            try {
                val txReceipt = mContract.buyTicket(ticketType.ticketTypeId,
                        totalPrice).send()
                Log.d(LOG_TAG, txReceipt.transactionHash.toString())
                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.COMPLETE)
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.message)
                Log.d(LOG_TAG, "Balance: " + balanceInWei.toBigDecimal()
                        .divide(Math.pow(10.0, 18.0).toBigDecimal()).toString())
                Log.d(LOG_TAG, "Total Price: " + totalPrice.toBigDecimal()
                        .divide(Math.pow(10.0, 18.0).toBigDecimal()).toString())
                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.ERROR)
            }
        }

        return txStatusLiveData
    }

    fun fetchBalance() {
        fetchEtherBalance()
        fetchUsdBalance()
    }

    fun fetchTickets() {
        bg {
            val newTickets = mutableListOf<Ticket>()
            val ownerAddress = credentials.value?.address
            val ticketIds = mContract.getTicketsForOwner(ownerAddress).send()

            ticketIds.forEach {
                val id = (it as Uint256).value
                val ticketTypeTuple = mContract.getTicketTypeForTicket(id).send()
                val ticketType = convertTupleToTicketType(ticketTypeTuple)

                newTickets.add(Ticket(id, ticketType))
                tickets.postValue(newTickets)
            }
        }
    }

    private fun convertTupleToTicketType(ticketTypeTuple: Tuple6<BigInteger,
            BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>?): TicketType {
        val ticketTypeId = ticketTypeTuple?.value1
        val eventId = ticketTypeTuple?.value2
        val price = ticketTypeTuple?.value3
        val initialSupply = ticketTypeTuple?.value4
        val currentSupply = ticketTypeTuple?.value5
        val refundable = ticketTypeTuple?.value6?.toInt() == 1

        return TicketType(
                ticketTypeId!!,
                eventId!!,
                price!!,
                initialSupply!!,
                currentSupply!!,
                refundable)
    }

    fun fetchMyEvents() {
        bg {
            val events = mutableListOf<Event>()
            val ownerAddress = credentials.value?.address
            val eventIds = mContract.getEventIdsForCreator(ownerAddress).send()

            eventIds.forEach {
                val id = (it as Uint256).value
                val event = getEvent(id.toLong())
                events.add(event)
            }
            myEvents.postValue(events)
        }
    }

//    fun getEventsForArea(lat: Double, long: Double): LiveData<List<IPFSEvent>> {
//
//    }
//
}