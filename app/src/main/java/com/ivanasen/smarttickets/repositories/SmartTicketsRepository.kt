package com.ivanasen.smarttickets.repositories

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.ivanasen.smarttickets.R
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
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tuples.generated.Tuple6
import java.io.File
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.Charset
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


object SmartTicketsRepository {
    private val LOG_TAG = SmartTicketsRepository::class.simpleName
    private val mWeb3: Web3j = Web3JProvider.instance
    private val mIpfsApi: SmartTicketsIPFSApi = SmartTicketsIPFSApi.instance

    private lateinit var mContract: SmartTickets

    var credentials: MutableLiveData<Credentials> = MutableLiveData()
    var unlockedWallet: MutableLiveData<Boolean> = MutableLiveData()

    var contractDeployed: MutableLiveData<Boolean> = MutableLiveData()

    val etherBalance: MutableLiveData<BigDecimal> = MutableLiveData()
    val usdBalance: MutableLiveData<Double> = MutableLiveData()

    val createdEvents: MutableLiveData<MutableList<Event>> = MutableLiveData()
    val events: MutableLiveData<MutableList<Event>> = MutableLiveData()
    val tickets: MutableLiveData<MutableList<Ticket>> = MutableLiveData()

    val eventsFetchStatus: MutableLiveData<Utility.Companion.TransactionStatus> = MutableLiveData()
    val areTicketsFetched: MutableLiveData<Utility.Companion.TransactionStatus> = MutableLiveData()

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

                Log.d(LOG_TAG, eventMetadataHash.toString(Charset.defaultCharset()))

                val ticketPrices = tickets.map { it.priceInUSDCents }
                val ticketSupplies = tickets.map { it.initialSupply }
                val ticketRefundables = tickets.map {
                    (if (it.refundable) 1 else 0).toBigInteger()
                }

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
            usdBalance.postValue((getWeiBalance() / oneUsdCentInWei).toDouble() / 100)
        }
    }

    fun convertEtherToUsd(weiValue: BigInteger): LiveData<Double> {
        val convertLiveData: MutableLiveData<Double> = MutableLiveData()
        bg {
            val oneUsdCentInWei = mContract.oneUSDCentInWei.send()
            convertLiveData.postValue((weiValue / oneUsdCentInWei).toDouble() / 100)
        }
        return convertLiveData
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

        val timestamp = event.value1.toLong() * 1000 // Convert to milliseconds
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
                    ticketTypes,
                    event.value5)
        }

        throw IllegalArgumentException("Event not found")
    }

    fun fetchEvents() {
        bg {
            eventsFetchStatus.postValue(Utility.Companion.TransactionStatus.PENDING)
            val newEvents = mutableListOf<Event>()
            val eventCount = mContract.eventCount.send().toLong()

            // We start one index up because of genesis event in contract
            for (i in eventCount downTo 1) {
                val event = getEvent(i)
                newEvents.add(event)
                events.postValue(newEvents)
                eventsFetchStatus.postValue(Utility.Companion.TransactionStatus.SUCCESS)
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

    fun fetchBalance() {
        fetchEtherBalance()
        fetchUsdBalance()
    }

    fun fetchTickets() {
        bg {
            areTicketsFetched.postValue(Utility.Companion.TransactionStatus.PENDING)
            val newTickets = mutableListOf<Ticket>()
            val ownerAddress = credentials.value?.address
            val ticketIds = mContract.getTicketsForOwner(ownerAddress).send()

            ticketIds.forEach {
                val id = (it as Uint256).value
                val ticketTypeTuple = mContract.getTicketTypeForTicket(id).send()
                val ticketType = convertTupleToTicketType(ticketTypeTuple)

                if (ticketType.eventId.toLong() > 0) {
                    newTickets.add(Ticket(id, ticketType))
                    tickets.postValue(newTickets)
                }
            }
            areTicketsFetched.postValue(Utility.Companion.TransactionStatus.SUCCESS)
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
            createdEvents.postValue(events)
        }
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
                val ticketHash = Hash.sha3(ticketId.toByteArray())

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