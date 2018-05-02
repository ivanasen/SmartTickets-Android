package com.ivanasen.smarttickets.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.android.gms.location.places.Place
import com.ivanasen.smarttickets.models.TicketTypeIpfs
import com.ivanasen.smarttickets.repositories.SmartTicketsRepository
import com.ivanasen.smarttickets.util.Utility
import org.jetbrains.anko.coroutines.experimental.bg
import java.math.BigInteger
import java.util.*

class CreateEventActivityViewModel : ViewModel() {

    private val MIN_EVENT_NAME_LENGTH = 5

    val eventName: MutableLiveData<String> = MutableLiveData()
    val eventDescription: MutableLiveData<String> = MutableLiveData()
    val pickedPlace: MutableLiveData<Place> = MutableLiveData()
    val eventTime: MutableLiveData<GregorianCalendar> = MutableLiveData()
    val mRepository = SmartTicketsRepository

    val pickedImages: MutableLiveData<MutableList<String>> = MutableLiveData()
    val ticketTypes: MutableLiveData<MutableList<TicketTypeIpfs>> = MutableLiveData()

    val isValidEvent: MutableLiveData<Boolean> = MutableLiveData()

    fun onPlacePicked(place: Place?) {
        pickedPlace.postValue(place)
    }

    fun attemptCreateEvent(): LiveData<Utility.Companion.TransactionStatus> {
        require(!eventName.value?.isEmpty()!!)
        require(eventTime.value?.compareTo(Calendar.getInstance())!! >= 0)
        require(pickedPlace.value != null)

        return mRepository.createEvent(eventName.value!!,
                eventDescription.value ?: "",
                eventTime.value!!.timeInMillis,
                pickedPlace.value!!.latLng,
                pickedPlace.value!!.name.toString(),
                pickedPlace.value!!.address.toString(),
                pickedImages.value ?: emptyList(),
                ticketTypes.value ?: emptyList())
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

            val newTicketTypes = mutableListOf<TicketTypeIpfs>()
            newTicketTypes.addAll(ticketTypes.value ?: emptyList())
            newTicketTypes.add(
                    TicketTypeIpfs(
                            BigInteger.valueOf(priceInCents),
                            BigInteger.valueOf(supply.toLong()),
                            BigInteger.valueOf(supply.toLong()),
                            refundable)
            )
            ticketTypes.postValue(newTicketTypes)
        }
    }

}
