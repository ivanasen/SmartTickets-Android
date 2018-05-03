package com.ivanasen.smarttickets.contractwrappers


import com.ivanasen.smarttickets.BuildConfig
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j


object SmartTicketsContractProvider {
    private lateinit var mContract: SmartTickets

    fun provide(web3: Web3j, credentials: Credentials): SmartTickets {
        if (::mContract.isInitialized) {
            return mContract
        }

        val network =  web3.netVersion().send().netVersion
        mContract = SmartTickets.load(
                SmartTickets._addresses[network],
                web3,
                credentials,
                SmartTickets.GAS_PRICE,
                SmartTickets.GAS_LIMIT)
        return mContract
    }
}