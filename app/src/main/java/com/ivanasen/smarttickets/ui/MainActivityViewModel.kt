package com.ivanasen.smarttickets.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.ivanasen.smarttickets.db.models.Event
import com.ivanasen.smarttickets.repository.SmartTicketsRepository
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.math.BigInteger
import java.net.Inet4Address
import java.net.URL
import java.sql.Time


class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository = SmartTicketsRepository
    private val WALLET_FILE_NAME = "WalletFileName"
    private val MIN_PASSWORD_LENGTH = 8

    val credentials = mRepository.credentials

    var password: LiveData<String> = MutableLiveData()

    var contractExists = mRepository.contractDeployed

    val etherBalance: LiveData<Double> = mRepository.etherBalance
    val usdBalance: LiveData<Double> = mRepository.usdBalance

//    public fun getEvents(): LiveData<List<Event>> {
//
//    }
//
//    public fun getTicketsForUser(): LiveData<List<Ticket>> {
//
//    }

    fun unlockWallet(password: String, context: Context): Boolean {
        val appContext = context.applicationContext
        val walletName = appContext.defaultSharedPreferences.getString(WALLET_FILE_NAME, "")
        require(isThereAWallet(context))

        val wallet = File(appContext.filesDir, walletName)
        return mRepository.unlockWallet(password, wallet)
    }

    fun createNewWallet(password: String, context: Context) {
        require(password.length < MIN_PASSWORD_LENGTH)
        val walletName = mRepository.createWallet(password, context.filesDir)
    }

    fun isThereAWallet(context: Context): Boolean {
        val appContext = context.applicationContext
        return try {
            require(appContext.defaultSharedPreferences.getString(WALLET_FILE_NAME, "") != "")
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun sendEther(address: String, amount: Double) {
        mRepository.sendEtherTo(address, amount)
    }

    fun addEvent(context: Context) {
        mRepository.createEvent(
                context,
                Event(BigInteger.valueOf(1),
                        "sfsrfrg",
                        Time(1),
                        emptyList(),
                        emptyList(),
                        true))
    }
}