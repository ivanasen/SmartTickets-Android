package com.ivanasen.smarttickets.db.models

import java.math.BigInteger
import java.sql.Time


data class Event(val id: BigInteger,
                 val metaDescriptionHash: String,
                 val date: Time,
                 val images: List<String>,
                 val tickets: List<Ticket>,
                 val active: Boolean)