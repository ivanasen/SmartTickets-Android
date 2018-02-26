package com.ivanasen.smarttickets.ui

import android.arch.lifecycle.ViewModelProviders
import android.net.Credentials
import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivanasen.smarttickets.R

/**
 * A placeholder fragment containing a simple view.
 */
class DiscoverFragment : Fragment() {

    private val LOG_TAG = DiscoverFragment::class.java.simpleName

    private lateinit var mRootView: View
    private val mViewModel: MainActivityViewModel by lazy {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_discover, container, false)
        return mRootView
    }
}
