package com.ivanasen.smarttickets.ui


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ivanasen.smarttickets.*
import com.ivanasen.smarttickets.util.Utility.Companion.loadFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private val LOG_TAG = MainActivity::class.simpleName

    private val mViewModel by lazy {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        observeLiveData()
        setupViews()
    }

    private fun observeLiveData() {
        mViewModel.contractExists.observe(this, Observer {
            if (it == true) {
                Log.d(LOG_TAG, "Contract loaded successfully!")
            }
        })
    }

    private fun setupViews() {
        supportActionBar?.setDisplayShowTitleEnabled(false)

        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_discover -> {
                    appBarTitle.text = getString(R.string.title_discover)
                    loadFragment(R.id.fragmentContainer,
                            supportFragmentManager, DiscoverFragment())
                    true
                }
                R.id.navigation_my_tickets -> {
                    appBarTitle.text = getString(R.string.title_my_tickets)
                    loadFragment(R.id.fragmentContainer,
                            supportFragmentManager, MyTicketsFragment())
                    true
                }
                R.id.navigation_create -> {
                    appBarTitle.text = getString(R.string.title_create)
                    loadFragment(R.id.fragmentContainer,
                            supportFragmentManager, CreateEventFragment())
                    true
                }
                R.id.navigation_wallet -> {
                    appBarTitle.text = getString(R.string.title_wallet)
                    loadFragment(R.id.fragmentContainer,
                            supportFragmentManager, MyWalletFragment())
                    true
                }
                else -> {
                    true
                }
            }
        }
    }
}
