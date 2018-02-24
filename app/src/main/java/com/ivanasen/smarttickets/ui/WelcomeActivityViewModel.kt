package com.ivanasen.smarttickets.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.ivanasen.smarttickets.repository.SmartTicketsRepository
import com.ivanasen.smarttickets.util.Utility.Companion.isValidPassword
import org.jetbrains.anko.defaultSharedPreferences
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import java.io.File


class WelcomeActivityViewModel : ViewModel() {
    private val mRepository = SmartTicketsRepository
    private val WALLET_FILE_NAME = "WalletFileName"

    val unlockedWallet = mRepository.unlockedWallet

    var walletExists: MutableLiveData<Boolean> = MutableLiveData()
    var password: MutableLiveData<String> = MutableLiveData()
    var confirmPassword: MutableLiveData<String> = MutableLiveData()
    var credentials: MutableLiveData<Credentials> = mRepository.credentials

    fun unlockWallet(password: String, context: Context): Boolean {
        val appContext = context.applicationContext
        val walletName = appContext.defaultSharedPreferences.getString(WALLET_FILE_NAME, "")
        require(isThereAWallet(context))

        val wallet = File(appContext.filesDir, walletName)
        return mRepository.unlockWallet(password, wallet)
    }

    fun createNewWallet(password: String, context: Context) {
        require(isValidPassword(password))
        val walletName = mRepository.createWallet(password, context.filesDir)

        val appContext = context.applicationContext
        appContext.defaultSharedPreferences
                .edit()
                .putString(WALLET_FILE_NAME, walletName)
                .apply()
    }

    fun isThereAWallet(context: Context): Boolean {
        val appContext = context.applicationContext
        return try {
            val walletName = appContext.defaultSharedPreferences.getString(WALLET_FILE_NAME, "")
            require(walletName != "")
            require(File(context.filesDir, walletName).exists())
            walletExists.postValue(true)
            true
        } catch (e: Exception) {
            walletExists.postValue(false)
            false
        }
    }
}