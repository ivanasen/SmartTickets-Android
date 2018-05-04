package com.ivanasen.smarttickets.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import com.ivanasen.smarttickets.repositories.ApplicationRepository
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.WALLET_FILE_NAME_KEY
import com.ivanasen.smarttickets.util.Utility.Companion.isValidPassword
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.defaultSharedPreferences
import org.web3j.crypto.Credentials
import java.io.File


class WelcomeActivityViewModel : ViewModel() {
    private val mRepository = ApplicationRepository

    var walletExists: MutableLiveData<Boolean> = MutableLiveData()
    var password: MutableLiveData<String> = MutableLiveData()
    var confirmPassword: MutableLiveData<String> = MutableLiveData()
    var credentials: MutableLiveData<Credentials> = mRepository.credentials
    var contractDeployed: LiveData<Boolean> = mRepository.contractDeployed
    var wrongPasswordAttempts: MutableLiveData<Int> = MutableLiveData()
    lateinit var walletFile: File

    fun unlockWallet(password: String, context: Context) {
        launch(UI) {
            val appContext = context.applicationContext
            require(isThereAWallet(appContext))

            bg {
                val walletName = appContext.defaultSharedPreferences
                        .getString(Utility.WALLET_FILE_NAME_KEY, "")
                val wallet = File(appContext.filesDir, walletName)
                val wasWalletUnlocked = mRepository.unlockWallet(password, wallet)

                if (!wasWalletUnlocked) {
                    wrongPasswordAttempts.postValue(wrongPasswordAttempts.value ?: 0+1)
                }
            }
        }
    }

    fun createNewWallet(password: String, context: Context) {
        launch(UI) {
            require(isValidPassword(password))
            val walletName = bg { mRepository.createWallet(password, context.filesDir) }
            saveNewWallet(context, walletName.await())
        }
    }

    private fun saveNewWallet(context: Context, walletName: String) {
        launch(UI) {
            val appContext = context.applicationContext
            appContext.defaultSharedPreferences
                    .edit()
                    .putString(WALLET_FILE_NAME_KEY, walletName)
                    .apply()

            walletFile = File(context.filesDir, walletName)
            walletExists.value = true
        }
    }


    fun isThereAWallet(context: Context): Boolean {
        val appContext = context.applicationContext
        return try {
            val walletName = appContext.defaultSharedPreferences.getString(WALLET_FILE_NAME_KEY, "")
            require(walletName != "")
            require(File(context.filesDir, walletName).exists())
            walletExists.postValue(true)
            true
        } catch (e: Exception) {
            walletExists.postValue(false)
            false
        }
    }

    fun loadInitialAppData() {
        mRepository.loadInitialAppData()
    }

    fun backupWallet(context: Context) {
        mRepository.backupWallet(context, walletFile)
    }

    fun importWallet(walletUri: Uri, context: Context): LiveData<Boolean> {
        val liveData: MutableLiveData<Boolean> = MutableLiveData()
        launch(UI) {
            val wallet = bg { mRepository.importWallet(context, walletUri, context.filesDir) }
            saveNewWallet(context, wallet.await().name)
            liveData.postValue(true)
        }
        return liveData
    }
}