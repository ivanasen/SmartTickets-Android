package com.ivanasen.smarttickets.db.models

import java.math.BigInteger

data class Ticket(
        val ticketId: BigInteger,
        val ticketType: TicketType)