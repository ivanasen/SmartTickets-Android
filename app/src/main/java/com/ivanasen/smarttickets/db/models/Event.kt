package com.ivanasen.smarttickets.db.models


data class Event(val timestamp: Long,
                 val images: List<String>,
                 val tickets: List<TicketType>)