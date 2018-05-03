package com.ivanasen.smarttickets.models

data class TransactionResponse(val status: String,
                               val message: String,
                               val result: List<Transaction>)