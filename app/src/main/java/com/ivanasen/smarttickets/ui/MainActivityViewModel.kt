package com.ivanasen.smarttickets.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.db.models.Ticket
import com.ivanasen.smarttickets.repository.SmartTicketsRepository


class MainActivityViewModel : ViewModel() {
    private var mRepository: SmartTicketsRepository

    init {
        mRepository = SmartTicketsRepository.instance
    }

//    public fun getEvents(): LiveData<List<Event>> {
//
//    }
//
//    public fun getTicketsForUser(): LiveData<List<Ticket>> {
//
//    }
}