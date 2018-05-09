package com.ivanasen.smarttickets.models


import com.google.android.gms.maps.model.LatLng
import java.math.BigInteger

data class Event(val eventId: Long,
                 val metaDescriptionHash: String,
                 val name: String,
                 val description: String,
                 val timestamp: Long,
                 val latLong: LatLng,
                 val locationName: String,
                 val locationAddress: String,
                 val thumbnailHash: String,
                 val tickets: List<TicketType>,
                 val earnings: BigInteger)