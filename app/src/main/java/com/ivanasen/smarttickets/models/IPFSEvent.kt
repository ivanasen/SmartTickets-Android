package com.ivanasen.smarttickets.models

import com.google.android.gms.maps.model.LatLng

data class IPFSEvent(val name: String?,
                     val description: String?,
                     val latLong: LatLng?,
                     val locationName: String?,
                     val locationAddress: String?,
                     val images: List<String>?)