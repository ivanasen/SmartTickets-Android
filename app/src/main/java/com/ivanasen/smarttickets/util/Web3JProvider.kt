package com.ivanasen.smarttickets.util

import com.ivanasen.smarttickets.BuildConfig
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.http.HttpService


object Web3JProvider {
    public val instance: Web3j = Web3jFactory.build(HttpService(BuildConfig.ETHEREUM_NODE_URL))
}
