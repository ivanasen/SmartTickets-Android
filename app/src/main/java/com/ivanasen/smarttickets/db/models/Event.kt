package com.ivanasen.smarttickets.db.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

data class Event(val eventId: Long,
                 val metaDescriptionHash: String,
                 val name: String,
                 val description: String,
                 val timestamp: Long,
                 val latLong: LatLng,
                 val locationName: String,
                 val locationAddress: String,
                 val images: List<String>,
                 val tickets: List<TicketType>)