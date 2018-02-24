package com.ivanasen.smarttickets.ui


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ivanasen.smarttickets.*
import com.ivanasen.smarttickets.util.Utility.Companion.loadFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val LOG_TAG = MainActivity::class.simpleName

    public val viewModel by lazy {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setupViews()
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel.unlockedWallet.observe(this, Observer<Boolean> {
            if (it == true) logIn() else promptUnlock()
        })
    }

    private fun logIn() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun promptUnlock() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
