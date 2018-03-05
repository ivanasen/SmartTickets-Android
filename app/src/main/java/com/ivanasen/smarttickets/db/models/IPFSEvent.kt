package com.ivanasen.smarttickets.db.models

import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng

data class IPFSEvent(val name: String?,
                     val description: String?,
                     val timestamp: Long,
                     val latLong: LatLng?,
                     val locationName: String?,
                     val locationAddress: String?,
                     val images: List<String>?,
                     val tickets: List<TicketType>?)