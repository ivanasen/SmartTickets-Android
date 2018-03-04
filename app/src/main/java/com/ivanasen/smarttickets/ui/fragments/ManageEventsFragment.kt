package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.activities.CreateEventActivity
import com.ivanasen.smarttickets.util.Utility.Companion.launchActivity
import com.ivanasen.smarttickets.viewmodels.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_manage_events.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class ManageEventsFragment : Fragment() {

    val mViewModel: MainActivityViewModel by lazy {
        ViewModelProviders.of(activity as FragmentActivity).get(MainActivityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_manage_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeLiveData()
    }

    private fun observeLiveData() {

    }

    private fun setupViews() {
        addEventBtn.onClick {
            launchActivity(this@ManageEventsFragment.context!!, CreateEventActivity::class.java)
        }
    }
}
