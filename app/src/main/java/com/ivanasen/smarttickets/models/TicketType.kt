package com.ivanasen.smarttickets.models

import java.math.BigInteger
import java.util.*


data class TicketType(val ticketTypeId: BigInteger,
                      val eventId: BigInteger,
                      val priceInUSDCents: BigInteger,
                      val initialSupply: BigInteger,
                      val currentSupply: BigInteger,
                      val refundable: BigInteger)