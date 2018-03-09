package com.ivanasen.smarttickets.contractwrappers


import com.ivanasen.smarttickets.BuildConfig
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import kotlin.math.max


object SmartTicketsContractProvider {
    // Uses Ganache
    //    private const val debugAddress: String = "0x345ca3e014aaf5dca488057592ee47305d9b3e10"
    private const val ropstenAddress: String = "0x5Cf40Ca267A29b19Ec54D4A36A259396b81282Cb"
    private const val debugAddress: String = "0x5Cf40Ca267A29b19Ec54D4A36A259396b81282Cb"

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