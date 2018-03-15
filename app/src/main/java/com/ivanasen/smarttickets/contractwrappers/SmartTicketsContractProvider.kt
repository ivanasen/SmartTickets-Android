package com.ivanasen.smarttickets.contractwrappers


import com.ivanasen.smarttickets.BuildConfig
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j


object SmartTicketsContractProvider {
    // Uses Ganache
    //    private const val debugAddress: String = "0x345ca3e014aaf5dca488057592ee47305d9b3e10"
    private const val ropstenAddress: String = "0x7328fc22226ac65c9a93b9d873fba0683d7d1b3e"
    private const val debugAddress: String = "0x7328fc22226ac65c9a93b9d873fba0683d7d1b3e"

    fun provide(web3: Web3j, credentials: Credentials): SmartTickets {
        val address = if (BuildConfig.DEBUG) debugAddress else ropstenAddress

        return SmartTickets.load(
                address,
                web3,
                credentials,
                SmartTickets.GAS_PRICE,
                SmartTickets.GAS_LIMIT)
    }
}