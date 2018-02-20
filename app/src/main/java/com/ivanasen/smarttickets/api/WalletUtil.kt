package com.ivanasen.smarttickets.api

import com.ivanasen.smarttickets.api.Web3JProvider.Companion.web3
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.crypto.WalletUtils
import java.io.File
import java.math.BigInteger
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.Transfer
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal


fun generateWallet(password: String, destinationDirectory: File): File {
    val walletName = WalletUtils.generateNewWalletFile(
            password,
            destinationDirectory,
            true)
    return File(destinationDirectory, walletName)
}

fun sendEther(wallet: File, password: String, amountEther: Double, to: String): TransactionReceipt {
    val credentials = WalletUtils.loadCredentials(password, wallet)
    val txReceipt = Transfer.sendFunds(web3, credentials, to,
            BigDecimal.valueOf(amountEther), Convert.Unit.ETHER).sendAsync().get()
    return txReceipt
}

fun deploySmartTickets(wallet: File, password: String): SmartTicketsCore {
    val credentials = WalletUtils.loadCredentials(password, wallet)
    val gasPrice = web3.ethGasPrice().sendAsync().get().gasPrice

    val contract = SmartTicketsCore.deploy(web3, credentials, gasPrice, SmartTicketsCore.GAS_LIMIT)
            .sendAsync()
            .get()

    return contract
}


fun getNonce(address: String): BigInteger {
    val ethGetTransactionCount = web3.ethGetTransactionCount(
            address, DefaultBlockParameterName.LATEST).sendAsync().get()

    return ethGetTransactionCount.transactionCount
}
