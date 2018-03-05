package com.ivanasen.smarttickets.db.models

import java.math.BigInteger
import java.util.*


data class TicketType(var eventId: BigInteger,
                      val priceInUSDCents: BigInteger,
                      val initialSupply: BigInteger,
                      val currentSupply: BigInteger,
                      val refundable: Boolean)