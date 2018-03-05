package com.ivanasen.smarttickets.ui.activities


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ivanasen.smarttickets.*
import com.ivanasen.smarttickets.ui.fragments.ManageEventsFragment
import com.ivanasen.smarttickets.ui.fragments.DiscoverFragment
import com.ivanasen.smarttickets.ui.fragments.MyTicketsFragment
import com.ivanasen.smarttickets.ui.fragments.MyWalletFragment
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import com.ivanasen.smarttickets.util.Utility.Companion.loadFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val LOG_TAG = MainActivity::class.simpleName

    private val mViewModel by lazy {
        ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    private val ACTIVE_FRAGMENT_KEY = "activeFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        observeLiveData()
        setupViews(savedInstanceState)
    }

    private fun observeLiveData() {
        mViewModel.contractExists.observe(this, Observer {
            if (it == true) {
                Log.d(LOG_TAG, "Contract loaded successfully!")
            }
        })
    }

    private fun setupViews(savedInstanceState: Bundle?) {
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val activeFragmentId = savedInstanceState?.getInt(ACTIVE_FRAGMENT_KEY)
        if (activeFragmentId != null) {
            loadFragment(activeFragmentId)
        } else {
            loadFragment(R.id.navigation_discover)
        }

        bottomNavigation.setOnNavigationItemSelectedListener {
            loadFragment(it.itemId)
            true
        }
    }

    private fun loadFragment(itemId: Int) {
        when (itemId) {
            R.id.navigation_discover -> {
                appBarTitle.text = getString(R.string.title_discover)
                loadFragment(R.id.fragmentContainer,
                        supportFragmentManager, DiscoverFragment())
            }
            R.id.navigation_my_tickets -> {
                appBarTitle.text = getString(R.string.title_my_tickets)
                loadFragment(R.id.fragmentContainer,
                        supportFragmentManager, MyTicketsFragment())
            }
            R.id.navigation_create -> {
                appBarTitle.text = getString(R.string.title_create)
                loadFragment(R.id.fragmentContainer,
                        supportFragmentManager, ManageEventsFragment())
            }
            R.id.navigation_wallet -> {
                appBarTitle.text = getString(R.string.title_wallet)
                loadFragment(R.id.fragmentContainer,
                        supportFragmentManager, MyWalletFragment())
            }
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ACTIVE_FRAGMENT_KEY, bottomNavigation.selectedItemId)
        super.onSaveInstanceState(outState)
    }
}
