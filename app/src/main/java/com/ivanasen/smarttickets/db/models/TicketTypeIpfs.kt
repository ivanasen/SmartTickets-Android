package com.ivanasen.smarttickets.db.models

import java.math.BigInteger


data class TicketTypeIpfs(val priceInUSDCents: BigInteger,
                      val initialSupply: BigInteger,
                      val currentSupply: BigInteger,
                      val refundable: Boolean)