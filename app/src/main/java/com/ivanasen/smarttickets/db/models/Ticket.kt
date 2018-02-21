package com.ivanasen.smarttickets.db.models

import java.math.BigInteger
import java.util.*


data class Ticket(val eventId: BigInteger,
                  val price: BigInteger,
                  val initialSupply: BigInteger,
                  val currentSupply: BigInteger,
                  val startVendingTime: Date,
                  val endVendingTime: Date,
                  val refundable: Boolean)