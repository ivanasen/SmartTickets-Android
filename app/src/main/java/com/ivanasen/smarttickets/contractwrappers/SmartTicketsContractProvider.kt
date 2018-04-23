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

        mContract = SmartTickets.load(
                BuildConfig.CONTRACT_ADDRESS,
                web3,
                credentials,
                SmartTickets.GAS_PRICE,
                SmartTickets.GAS_LIMIT)
        return mContract
    }
}