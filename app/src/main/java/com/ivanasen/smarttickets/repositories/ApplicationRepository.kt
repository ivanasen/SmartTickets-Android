package com.ivanasen.smarttickets.repositories

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.api.ApplicationApi
import com.ivanasen.smarttickets.contractwrappers.SmartTicketsContractProvider

import com.ivanasen.smarttickets.api.IPFSApi
import com.ivanasen.smarttickets.contractwrappers.SmartTickets
import com.ivanasen.smarttickets.models.*
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.IPFS_HASH_HEADER
import com.ivanasen.smarttickets.util.Utility.Companion.ONE_ETHER_IN_WEI
import com.ivanasen.smarttickets.util.Web3JProvider
import okhttp3.MediaType
import okhttp3.RequestBody
import org.jetbrains.anko.coroutines.experimental.bg
import org.web3j.crypto.*
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tuples.generated.Tuple6
import org.web3j.tx.Transfer
import org.web3j.utils.Convert
import java.io.File
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.Charset
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


object ApplicationRepository {
    private val LOG_TAG = ApplicationRepository::class.simpleName
    private const val ETHEREUM_VALIDATION_PREFIX = "\\x19Ethereum Signed Message:\\n30"

    private val mWeb3 by lazy { Web3JProvider.instance }
    private val mIpfsApi by lazy { IPFSApi.instance }
    private val mApi by lazy { ApplicationApi.instance }

    private lateinit var mContract: SmartTickets

    var credentials: MutableLiveData<Credentials> = MutableLiveData()
    var unlockedWallet: MutableLiveData<Boolean> = MutableLiveData()

    var contractDeployed: MutableLiveData<Boolean> = MutableLiveData()

    val etherBalance: MutableLiveData<BigDecimal> = MutableLiveData()
    val usdBalance: MutableLiveData<Double> = MutableLiveData()

    val createdEvents: MutableLiveData<MutableList<Event>> = MutableLiveData()
    val events: MutableLiveData<MutableList<Event>> = MutableLiveData()
    val tickets: MutableLiveData<MutableList<Ticket>> = MutableLiveData()
    val txHistory: MutableLiveData<List<Transaction>> = MutableLiveData()

    val eventsFetchStatus by lazy { MutableLiveData<Utility.Companion.TransactionStatus>() }
    val ticketsFetchStatus by lazy { MutableLiveData<Utility.Companion.TransactionStatus>() }
//    val myEventsFetchStatus by lazy { MutableLiveData<Utility.Companion.TransactionStatus>() }

    fun createEvent(name: String,
                    description: String,
                    timestamp: Long,
                    latLong: LatLng,
                    locationName: String,
                    locationAddress: String,
                    imagePaths: List<String>,
                    tickets: List<TicketType>): LiveData<Utility.Companion.TransactionStatus> {
        val txStatusLiveData: MutableLiveData<Utility.Companion.TransactionStatus> =
                MutableLiveData()
        bg {
            try {
                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.PENDING)

                val imageHashes = uploadImages(imagePaths)

                val event = IPFSEvent(name, description, latLong, locationName,
                        locationAddress, imageHashes)
                val eventMetadataHash = postEventToIpfs(event)

                Log.d(LOG_TAG, eventMetadataHash.toString(Charset.defaultCharset()))

                val ticketPrices = tickets.map { it.priceInUSDCents }
                val ticketSupplies = tickets.map { it.initialSupply }
                val ticketRefundables = tickets.map { it.refundable }

                val eventTxReceipt = mContract.createEvent(
                        timestamp.toBigInteger(),
                        eventMetadataHash,
                        ticketPrices,
                        ticketSupplies,
                        ticketRefundables)
                        .send()
                Log.d(LOG_TAG, eventTxReceipt.transactionHash)


                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.SUCCESS)
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.message)
                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.ERROR)
            }

        }
        return txStatusLiveData
    }

//    private fun addTicketTypeForEvent(eventId: BigInteger, ticketType: TicketType) {
//        bg {
//            val txReceipt = mContract.addTicketForEvent(
//                    eventId,
//                    ticketType.priceInUSDCents,
//                    ticketType.initialSupply,
//                    BigInteger.valueOf(if (ticketType.refundable) 1 else 0)
//            ).send()
//        }
//    }

    private fun postEventToIpfs(event: IPFSEvent): ByteArray {
        val response = mIpfsApi.postEvent(event)
                .execute()
        return response.headers()
                .get(Utility.IPFS_HASH_HEADER)
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

    private fun fetchContractData() {
        fetchEvents()
        fetchTickets()
        fetchWalletData()
    }

    fun unlockWallet(password: String, wallet: File): Boolean {
        return try {
            credentials.postValue(getCredentials(password, wallet))
            unlockedWallet.postValue(true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            unlockedWallet.postValue(false)
            false
        }
    }


    private fun getCredentials(password: String, wallet: File): Credentials =
            WalletUtils.loadCredentials(password, wallet)

    fun checkPassword(password: String, wallet: File): Boolean {
        return try {
            getCredentials(password, wallet)
            true
        } catch (e: Exception) {
            e.printStackTrace()
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
            usdBalance.postValue((getWeiBalance() / oneUsdCentInWei).toDouble())
        }
    }

    fun convertEtherToUsd(weiValue: BigInteger): LiveData<Double> {
        val convertLiveData: MutableLiveData<Double> = MutableLiveData()
        bg {
            val usd = convertWeiToUsdSynchronously(weiValue)
            convertLiveData.postValue(usd)
        }
        return convertLiveData
    }

    private fun convertWeiToUsdSynchronously(weiValue: BigInteger): Double {
        val oneUsdCentInWei = mContract.oneUSDCentInWei.send()
        return (weiValue / oneUsdCentInWei).toDouble()
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
            val txReceipt = Transfer.sendFunds(mWeb3, credentials.value, address,
                    BigDecimal.valueOf(etherAmount), Convert.Unit.ETHER).sendAsync().get()
            Log.d(LOG_TAG, "Tx: $txReceipt")
        }
    }

    private fun getEvent(id: Long): Event {
        val event = mContract.getEvent(BigInteger.valueOf(id)).send()

        val timestamp = event.value1.toLong() * 1000 // Convert to milliseconds
        val ipfsHash = event.value2.toString(Charset.defaultCharset())
        val cancelled = event.value3
        val earnings = event.value5

        val eventData: IPFSEvent? = getEventFromIPFS(ipfsHash)

        val ticketTypes = getTicketTypesForEvent(id)

        if (cancelled.toInt() == 0
                && eventData != null &&
                eventData.name != null &&
                eventData.latLong != null &&
                eventData.locationAddress != null &&
                eventData.locationName != null) {

            return Event(
                    id,
                    ipfsHash,
                    eventData.name,
                    eventData.description ?: "",
                    timestamp,
                    eventData.latLong,
                    eventData.locationName,
                    eventData.locationAddress,
                    eventData.images ?: emptyList(),
                    ticketTypes,
                    earnings)
        }

        throw IllegalArgumentException("Event not found")
    }

    fun fetchTxHistory(): MutableLiveData<Utility.Companion.TransactionStatus> {
        val txHistoryFetchStatus = MutableLiveData<Utility.Companion.TransactionStatus>()
        bg {
            txHistoryFetchStatus.postValue(Utility.Companion.TransactionStatus.PENDING)
            try {
                val address = credentials.value?.address
                if (address != null) {
                    val response = mApi.getTxHistory(address,
                            ApplicationApi.TX_HISTORY_PAGE_DEFAULT,
                            ApplicationApi.TX_HISTORY_LIMIT_DEFAULT,
                            ApplicationApi.TX_HISTORY_SORT_DSC)
                            .execute()

                    if (response.isSuccessful) {
                        val txResponse = response.body() as MutableList<Transaction>
                        if (txResponse.isNotEmpty()) {
                            txResponse.forEach {
                                if (it.from == credentials.value?.address && it.to == mContract.contractAddress) {
                                    it.type = "Called Contract"
                                } else if (it.from == credentials.value?.address) {
                                    it.type = "Sent Ether"
                                } else if (it.to == credentials.value?.address) {
                                    it.type = "Received Ether"
                                } else {
                                    it.type = "Unknown Transaction"
                                }
                            }

                            txHistory.postValue(txResponse)
                            txHistoryFetchStatus.postValue(Utility.Companion.TransactionStatus.SUCCESS)
                        } else {
                            txHistoryFetchStatus.postValue(Utility.Companion.TransactionStatus.FAILURE)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                txHistoryFetchStatus.postValue(Utility.Companion.TransactionStatus.ERROR)
            }
        }
        return txHistoryFetchStatus
    }

    fun fetchEvents(order: String = ApplicationApi.EVENT_ORDER_RECENT,
                    page: Int = ApplicationApi.EVENT_PAGE_DEFAULT,
                    limit: Int = ApplicationApi.EVENT_LIMIT_DEFAULT) {
        bg {
            eventsFetchStatus.postValue(Utility.Companion.TransactionStatus.PENDING)
            try {
                val eventResponse = mApi.getEvents(order, page, limit).execute()

                if (eventResponse.isSuccessful) {
                    val resBody = eventResponse.body()
                    if (resBody != null && resBody.isNotEmpty()) {
                        events.postValue(resBody.toMutableList())
                        eventsFetchStatus.postValue(Utility.Companion.TransactionStatus.SUCCESS)
                    } else {
                        eventsFetchStatus.postValue(Utility.Companion.TransactionStatus.FAILURE)
                    }
                } else {
                    eventsFetchStatus.postValue(Utility.Companion.TransactionStatus.ERROR)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                eventsFetchStatus.postValue(Utility.Companion.TransactionStatus.ERROR)
            }
        }
    }

    private fun getEventFromIPFS(ipfsHash: String): IPFSEvent? {
        val eventResponse = mIpfsApi.getEvent(ipfsHash).execute()
        return eventResponse.body()
    }

    private fun getTicketTypesForEvent(eventId: Long): MutableList<TicketType> {
        val count = mContract.getTicketTypesCountForEvent(BigInteger.valueOf(eventId))
                .send().toLong()

        val ticketTypes = (0 until count).map {
            val ticketTuple = mContract.getTicketTypeForEvent(BigInteger.valueOf(eventId),
                    it.toBigInteger()).send()
            convertTupleToTicketType(ticketTuple)
        }

        return ticketTypes.toMutableList()
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
                txStatusLiveData.postValue(Utility.Companion.TransactionStatus.SUCCESS)
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

    fun fetchWalletData() {
        fetchEtherBalance()
        fetchUsdBalance()
    }

    fun fetchMyEvents(): LiveData<Utility.Companion.TransactionStatus> {
        val myEventsFetchStatus = MutableLiveData<Utility.Companion.TransactionStatus>()
        bg {
            myEventsFetchStatus.postValue(Utility.Companion.TransactionStatus.PENDING)
            try {
                val creatorAddress = credentials.value?.address
                creatorAddress?.let {
                    val response = mApi.getEventsForCreator(creatorAddress).execute()
                    if (response.isSuccessful) {
                        val eventsRes = response.body()
                        if (eventsRes != null && eventsRes.isNotEmpty()) {
                            createdEvents.postValue(eventsRes.toMutableList())
                            myEventsFetchStatus.postValue(Utility.Companion.TransactionStatus.SUCCESS)
                        } else {
                            myEventsFetchStatus.postValue(Utility.Companion.TransactionStatus.FAILURE)
                        }
                    } else {
                        myEventsFetchStatus.postValue(Utility.Companion.TransactionStatus.ERROR)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                myEventsFetchStatus.postValue(Utility.Companion.TransactionStatus.ERROR)
            }
        }
        return myEventsFetchStatus
    }

    fun fetchTickets() {
        bg {
            ticketsFetchStatus.postValue(Utility.Companion.TransactionStatus.PENDING)
            try {
                val ownerAddress = credentials.value?.address

                ownerAddress?.let {
                    val response = mApi.getTickets(ownerAddress).execute()
                    if (response.isSuccessful) {
                        val ticketsRes = response.body()
                        if (ticketsRes != null && ticketsRes.isNotEmpty()) {
                            tickets.postValue(ticketsRes.toMutableList())
                            ticketsFetchStatus.postValue(Utility.Companion.TransactionStatus.SUCCESS)
                        } else {
                            ticketsFetchStatus.postValue(Utility.Companion.TransactionStatus.FAILURE)
                        }
                    } else {
                        ticketsFetchStatus.postValue(Utility.Companion.TransactionStatus.ERROR)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ticketsFetchStatus.postValue(Utility.Companion.TransactionStatus.ERROR)
            }
        }
    }

    private fun convertTupleToTicketType(ticketTypeTuple: Tuple6<BigInteger,
            BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>?): TicketType {
        val ticketTypeId = ticketTypeTuple?.value1 ?: (-1).toBigInteger()
        val eventId = ticketTypeTuple?.value2 ?: (-1).toBigInteger()
        val price = ticketTypeTuple?.value3 ?: (-1).toBigInteger()
        val initialSupply = ticketTypeTuple?.value4 ?: (-1).toBigInteger()
        val currentSupply = ticketTypeTuple?.value5 ?: (-1).toBigInteger()
        val refundable = ticketTypeTuple?.value6 ?: (-1).toBigInteger()

        return TicketType(
                ticketTypeId,
                eventId,
                price,
                initialSupply,
                currentSupply,
                refundable)
    }


    fun sellTicket(ticket: Ticket): LiveData<Utility.Companion.TransactionStatus> {
        val ticketLiveData: MutableLiveData<Utility.Companion.TransactionStatus> =
                MutableLiveData()
        bg {
            ticketLiveData.postValue(Utility.Companion.TransactionStatus.PENDING)
            try {
                val ticketId = ticket.ticketId
                val txReceipt = mContract.refundTicket(ticketId).send()
                ticketLiveData.postValue(Utility.Companion.TransactionStatus.SUCCESS)
                Log.d(LOG_TAG, txReceipt.transactionHash)
            } catch (e: Exception) {
                e.printStackTrace()
                ticketLiveData.postValue(Utility.Companion.TransactionStatus.ERROR)
            }
        }

        return ticketLiveData
    }

    fun withdrawalFunds(eventId: Long): LiveData<Utility.Companion.TransactionStatus> {
        val fundsLiveData: MutableLiveData<Utility.Companion.TransactionStatus> =
                MutableLiveData()
        bg {
            fundsLiveData.postValue(Utility.Companion.TransactionStatus.PENDING)
            try {
                val txReceipt = mContract.withdrawalEarningsForEvent(eventId.toBigInteger()).send()
                fundsLiveData.postValue(Utility.Companion.TransactionStatus.SUCCESS)
                Log.d(LOG_TAG, txReceipt.transactionHash)
            } catch (e: Exception) {
                e.printStackTrace()
                fundsLiveData.postValue(Utility.Companion.TransactionStatus.ERROR)
            }
        }

        return fundsLiveData
    }

    fun backupWallet(context: Context, walletFile: File) {
        Utility.backupWallet(context, walletFile)
    }

    fun importWallet(context: Context, walletUri: Uri, destinationDirectory: File): File {
        val walletString = readTextFromUri(context, walletUri)
        val walletFile = File(destinationDirectory,
                context.getString(R.string.default_wallet_name))
        walletFile.writeText(walletString)
        return walletFile
    }

    @Throws(IOException::class)
    private fun readTextFromUri(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))

        val stringBuilder = StringBuilder()
        var line = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
        }

        inputStream.close()
        return stringBuilder.toString()
    }

    fun createTicketValidationCode(ticket: Ticket): LiveData<String> {
        val codeLiveData: MutableLiveData<String> = MutableLiveData()
        bg {
            val signature = signMessage(ticket.ticketId.toString())

            val validation = TicketValidationCode(
                    ticket.ticketId.toString(),
                    signature,
                    credentials.value?.address!!)
            codeLiveData.postValue(Gson().toJson(validation))
        }
        return codeLiveData
    }

    private fun signMessage(message: String): Sign.SignatureData? =
            Sign.signMessage(message.toByteArray(), credentials.value?.ecKeyPair)

    // TODO: Ticket validation stopped working for some reason
    fun validateTicket(qrCodeString: String): LiveData<Utility.Companion.TransactionStatus> {
        val validationLiveData: MutableLiveData<Utility.Companion.TransactionStatus> =
                MutableLiveData()

        bg {
            validationLiveData.postValue(Utility.Companion.TransactionStatus.PENDING)
            try {
                val ticketValidationCode = Gson().fromJson(qrCodeString,
                        TicketValidationCode::class.java)

                val ticketId = ticketValidationCode.ticket.toBigInteger()
                val ticketHash = Hash.sha3(ticketId.toString().toByteArray())

                val address = ticketValidationCode.address
                val signature = ticketValidationCode.ticketSignature

                val s = signature?.s
                val r = signature?.r
                val v = signature?.v?.toInt()?.toBigInteger()

                val isOwned = mContract.verifyTicket(ticketId,
                        ticketHash, address, v, r, s).send()
                if (isOwned) {
                    validationLiveData.postValue(Utility.Companion.TransactionStatus.SUCCESS)
                } else {
                    validationLiveData.postValue(Utility.Companion.TransactionStatus.ERROR)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                validationLiveData.postValue(Utility.Companion.TransactionStatus.ERROR)
            }
        }

        return validationLiveData
    }

//    fun signTicketMessage(ticket: Ticket): String {
//        val ticketJson = Gson().toJson(ticket)
//
//    }


//    fun getEventsForArea(lat: Double, long: Double): LiveData<List<IPFSEvent>> {
//
//    }
//
}