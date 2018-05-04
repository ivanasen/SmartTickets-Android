package com.ivanasen.smarttickets.models

import java.math.BigInteger

data class Ticket(
        val ticketId: BigInteger,
        val ticketType: TicketType,
        val event: Event)