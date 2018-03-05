package com.ivanasen.smarttickets.contractwrappers


import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthBlock
import rx.Observable


object SmartTicketsContractProvider {
    private const val address: String = "0x345ca3e014aaf5dca488057592ee47305d9b3e10"

    fun provide(web3: Web3j, credentials: Credentials): SmartTickets {
        val gasPrice = web3.ethGasPrice().sendAsync().get().gasPrice
        val gasLimit = web3.ethGetBlockByNumber(
                DefaultBlockParameterName.LATEST,
                false).send().block.gasLimit

        return SmartTickets.load(
                address,
                web3,
                credentials,
                gasPrice,
                gasLimit)
    }
}