package com.ivanasen.smarttickets.ui.activities

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.viewmodels.CreateEventActivityViewModel
import kotlinx.android.synthetic.main.activity_create_event.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class CreateEventActivity : AppCompatActivity() {

    private val mViewModel: CreateEventActivityViewModel by lazy {
        ViewModelProviders.of(this).get(CreateEventActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        setupViews()
        observeLiveData()
    }

    private fun observeLiveData() {

    }

    private fun setupViews() {
        cancelCreateEventBtn.onClick {
            onBackPressed()
        }
    }
}
