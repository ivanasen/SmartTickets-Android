package com.ivanasen.smarttickets.contractwrappers


import com.ivanasen.smarttickets.BuildConfig
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName


object SmartTicketsContractProvider {
//    private const val debugAddress: String = "0x345ca3e014aaf5dca488057592ee47305d9b3e10"
    private const val ropstenAddress: String = "0x073be062A001f0A97982E531f83EFDc0482779C6"
    private const val debugAddress: String = "0xCBc2f1D573bcA98E96778C384A4772b8d6e6f450"

    fun provide(web3: Web3j, credentials: Credentials): SmartTickets {
        val gasPrice = web3.ethGasPrice().sendAsync().get().gasPrice
        val gasLimit = web3.ethGetBlockByNumber(
                DefaultBlockParameterName.LATEST,
                false).send().block.gasLimit

        val address = if (BuildConfig.DEBUG) debugAddress else ropstenAddress

        return SmartTickets.load(
                address,
                web3,
                credentials,
                gasPrice,
                gasLimit)
    }
}