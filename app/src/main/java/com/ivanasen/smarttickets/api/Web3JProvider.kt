package com.ivanasen.smarttickets.api

import com.ivanasen.smarttickets.BuildConfig
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.http.HttpService


class Web3JProvider {
    companion object {
        val web3: Web3j = Web3jFactory.build(HttpService(BuildConfig.ETHEREUM_NODE_URL))
    }
}
