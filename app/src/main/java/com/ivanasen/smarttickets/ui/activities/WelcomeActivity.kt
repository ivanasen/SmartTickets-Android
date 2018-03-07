package com.ivanasen.smarttickets.ui.activities

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.fragments.CreateWalletFragment
import com.ivanasen.smarttickets.ui.fragments.LoginFragment
import com.ivanasen.smarttickets.viewmodels.WelcomeActivityViewModel
import com.ivanasen.smarttickets.util.Utility.Companion.loadFragment

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
            loadFragment(R.id.fragmentContainer, supportFragmentManager, LoginFragment())
        } else {
            loadFragment(R.id.fragmentContainer, supportFragmentManager, CreateWalletFragment())
        }
    }

    fun loadWalletCreationScreen() {
        loadFragment(R.id.fragmentContainer, supportFragmentManager, CreateWalletFragment())
    }

}
