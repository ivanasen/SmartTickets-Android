package com.ivanasen.smarttickets.api


import com.ivanasen.smarttickets.api.contractwrappers.SmartTicketsCore
import org.jetbrains.anko.coroutines.experimental.bg
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j


class SmartTicketsContractProvider {

    companion object {
        private const val address: String = "0xf25186b5081ff5ce73482ad761db0eb0d25abfbf"

        fun provide(web3: Web3j, credentials: Credentials): SmartTicketsCore {
            val gasPrice = web3.ethGasPrice().sendAsync().get().gasPrice
            return SmartTicketsCore.load(
                    address,
                    web3,
                    credentials,
                    gasPrice,
                    SmartTicketsCore.GAS_LIMIT)
        }
    }
}