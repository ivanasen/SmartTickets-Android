package com.ivanasen.smarttickets.util

import com.ivanasen.smarttickets.util.Web3JProvider.instance
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import java.io.File
import java.math.BigInteger
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Transfer
import org.web3j.utils.Convert
import java.math.BigDecimal

class WalletUtil {
    companion object {
        fun generateWallet(password: String, destinationDirectory: File): File {
            val walletName = WalletUtils.generateNewWalletFile(
                    password,
                    destinationDirectory,
                    true)
            return File(destinationDirectory, walletName)
        }

        fun sendEther(credentials: Credentials, amountEther: Double, to: String): TransactionReceipt {
            val txReceipt = Transfer.sendFunds(instance, credentials, to,
                    BigDecimal.valueOf(amountEther), Convert.Unit.ETHER).sendAsync().get()
            return txReceipt
        }

    }
}