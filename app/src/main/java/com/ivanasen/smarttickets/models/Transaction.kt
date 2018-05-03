package com.ivanasen.smarttickets.models

import java.math.BigInteger

data class Transaction(val blockNumber: BigInteger,
                       val timeStamp: Long,
                       val hash: String,
                       val nonce: BigInteger,
                       val blockHash: String,
                       val transactionIndex: Long,
                       val from: String,
                       val to: String,
                       val value: BigInteger,
                       val gas: BigInteger,
                       val gasPrice: BigInteger,
                       val isError: Boolean,
                       val txreceipt_status: Int,
                       val input: String,
                       val contractAddress: String,
                       val cumulativeGasUsed: BigInteger,
                       val gasUsed: BigInteger,
                       val confirmations: Long,
                       var valueUsd: Double? = null,
                       var type: String? = null)