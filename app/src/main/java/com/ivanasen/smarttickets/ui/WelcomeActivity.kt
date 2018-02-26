package com.ivanasen.smarttickets.ui

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.util.Utility.Companion.loadFragment

class WelcomeActivity : AppCompatActivity() {

    private val mViewModel: WelcomeActivityViewModel by lazy {
        ViewModelProviders.of(this).get(WelcomeActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        observeLiveData()
    }

    private fun observeLiveData() {
        if (mViewModel.isThereAWallet(this)) {
            loadFragment(R.id.fragmentContainer, supportFragmentManager, LoginFragment())
        } else {
            loadFragment(R.id.fragmentContainer, supportFragmentManager, CreateWalletFragment())
        }
    }

}
