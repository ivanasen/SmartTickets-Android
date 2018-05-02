package com.ivanasen.smarttickets.models

import com.google.android.gms.maps.model.LatLng

data class IPFSEvent(val name: String?,
                     val description: String?,
                     var timestamp: Long,
                     val latLong: LatLng?,
                     val locationName: String?,
                     val locationAddress: String?,
                     val images: List<String>?,
                     val tickets: List<TicketTypeIpfs>?,
                     val eventId: Long? = null)