package com.ivanasen.smarttickets.ui.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.fragments.CreateWalletFragment
import com.ivanasen.smarttickets.ui.fragments.LoginFragment
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.viewmodels.WelcomeActivityViewModel
import com.ivanasen.smarttickets.util.Utility.Companion.loadFragment
import droidninja.filepicker.FilePickerConst

class WelcomeActivity : AppCompatActivity() {

    private val mViewModel: WelcomeActivityViewModel by lazy {
        ViewModelProviders.of(this).get(WelcomeActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        showInfoDialog()
        observeLiveData()
    }

    private fun showInfoDialog() {
        MaterialDialog.Builder(this)
                .content(getString(R.string.test_message))
                .title(R.string.test_dialog_title)
                .positiveText(getString(R.string.OK))
                .show()
    }

    private fun observeLiveData() {
        if (mViewModel.isThereAWallet(this)) {
            loadLoginFragment()
        } else {
            loadWalletCreationFragment()
        }
    }

    fun loadLoginFragment() {
        loadFragment(R.id.fragmentContainer, supportFragmentManager, LoginFragment())
    }

    fun loadWalletCreationFragment() {
        loadFragment(R.id.fragmentContainer, supportFragmentManager, CreateWalletFragment())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Utility.IMPORT_WALLET_REQUEST_CODE -> {
                val walletUri = data?.data
                walletUri?.let {
                    mViewModel.importWallet(it, this)
                            .observe(this, Observer {
                                if (it == true) {
                                    loadLoginFragment()
                                }
                            })
                }
            }
        }
    }
}
