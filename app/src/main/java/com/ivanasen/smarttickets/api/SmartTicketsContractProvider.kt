package com.ivanasen.smarttickets.api


import com.ivanasen.smarttickets.api.contractwrappers.SmartTicketsCore
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthBlock
import rx.Observable


object SmartTicketsContractProvider {
    private const val address: String = "0x345ca3e014aaf5dca488057592ee47305d9b3e10"

    private lateinit var blockObserver: Observable<EthBlock>

    private var gasLimit = SmartTicketsCore.GAS_LIMIT

    fun provide(web3: Web3j, credentials: Credentials): SmartTicketsCore {
        val gasPrice = web3.ethGasPrice().sendAsync().get().gasPrice

        return SmartTicketsCore.load(
                address,
                web3,
                credentials,
                gasPrice,
                gasLimit)

    }
}