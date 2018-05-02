package com.ivanasen.smarttickets.db.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import java.math.BigInteger

data class Event(val eventId: Long,
                 val metaDescriptionHash: String,
                 val name: String,
                 val description: String,
                 var timestamp: Long,
                 val latLong: LatLng,
                 val locationName: String,
                 val locationAddress: String,
                 val images: List<String>,
                 val tickets: List<TicketType>,
                 val earnings: BigInteger)