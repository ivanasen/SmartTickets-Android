package com.ivanasen.smarttickets.ui

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivanasen.smarttickets.R
import kotlinx.android.synthetic.main.fragment_create_event.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class CreateEventFragment : Fragment() {

    val mViewModel: MainActivityViewModel by lazy {
        ViewModelProviders.of(activity as FragmentActivity).get(MainActivityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeLiveData()
    }

    private fun observeLiveData() {

    }

    private fun setupViews() {
        addEventBtn.onClick {
            mViewModel.addEvent(this@CreateEventFragment.context!!)
        }
    }
}
