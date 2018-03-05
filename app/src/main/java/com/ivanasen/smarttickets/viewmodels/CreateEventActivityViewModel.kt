package com.ivanasen.smarttickets.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import com.google.android.gms.location.places.Place
import com.ivanasen.smarttickets.db.models.TicketType
import com.ivanasen.smarttickets.repositories.SmartTicketsRepository
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg
import java.math.BigInteger
import java.util.*

class CreateEventActivityViewModel : ViewModel() {

    private val MIN_EVENT_NAME_LENGTH = 5

    val eventName: MutableLiveData<String> = MutableLiveData()
    val eventDescription: MutableLiveData<String> = MutableLiveData()
    val pickedPlace: MutableLiveData<Place> = MutableLiveData()
    val eventTime: MutableLiveData<Calendar> = MutableLiveData()
    val mRepository = SmartTicketsRepository

    val pickedImages: MutableLiveData<MutableList<String>> = MutableLiveData()
    val ticketTypes: MutableLiveData<MutableList<TicketType>> = MutableLiveData()

    val isValidEvent: MutableLiveData<Boolean> = MutableLiveData()

    fun onPlacePicked(place: Place?) {
        pickedPlace.postValue(place)
    }

    fun attemptCreateEvent(drawables: List<BitmapDrawable>) {
        bg {
            require(!eventName.value?.isEmpty()!!)
            require(eventTime.value?.compareTo(Calendar.getInstance())!! >= 0)
            require(pickedPlace.value != null)

            mRepository.createEvent(eventTime.value!!.timeInMillis / 1000,
                    drawables,
                    ticketTypes.value ?: emptyList())
        }
    }

    fun checkIfIsValidEvent() {
        bg {
            val validName = (eventName.value ?: "").length > MIN_EVENT_NAME_LENGTH
            val validTickets = ticketTypes.value?.isNotEmpty() == true
            val validDate = eventTime.value?.time!! >= Calendar.getInstance().time
            val validPlace = pickedPlace.value != null

            val isValid =
                    validName && validTickets && validDate && validPlace

            isValidEvent.postValue(isValid)
        }
    }

    fun addTicketType(price: Double, supply: Double, refundable: Boolean) {
        bg {
            val priceInCents = (price.toString().toDouble() * 100).toLong()

            val newTicketTypes = mutableListOf<TicketType>()
            newTicketTypes.addAll(ticketTypes.value ?: emptyList())
            newTicketTypes.add(
                    TicketType(BigInteger.ZERO,
                            BigInteger.valueOf(priceInCents),
                            BigInteger.valueOf(supply.toLong()),
                            BigInteger.valueOf(supply.toLong()),
                            refundable)
            )
            ticketTypes.postValue(newTicketTypes)
        }
    }

}
