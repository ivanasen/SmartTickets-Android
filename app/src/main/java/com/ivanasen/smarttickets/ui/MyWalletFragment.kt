package com.ivanasen.smarttickets.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Fade
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ivanasen.smarttickets.R
import kotlinx.android.synthetic.main.fragment_my_wallet.*

class MyWalletFragment : Fragment() {

    private val mViewModel: MainActivityViewModel by lazy {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateViews()
        observeLiveData()
    }

    private fun populateViews() {
        val walletAddress = mViewModel.credentials.value?.address
        walletAddressView.text = walletAddress

        val url = String.format(getString(R.string.gravatar_url), walletAddress)
        Glide.with(this)
                .load(url)
                .into(walletIdenticonView)

        availableEtherView.text = mViewModel.availableEther.value.toString()
    }

    private fun observeLiveData() {
        mViewModel.availableEther.observe(this, Observer {
            availableEtherView.text = it.toString()
        })
    }
}
