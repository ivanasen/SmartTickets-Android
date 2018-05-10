package com.ivanasen.smarttickets.ui.activities


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ivanasen.smarttickets.*
import com.ivanasen.smarttickets.ui.fragments.ManageEventsFragment
import com.ivanasen.smarttickets.ui.fragments.DiscoverFragment
import com.ivanasen.smarttickets.ui.fragments.MyTicketsFragment
import com.ivanasen.smarttickets.ui.fragments.MyWalletFragment
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import com.ivanasen.smarttickets.util.Utility.Companion.loadFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.transition.Fade
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import com.ivanasen.smarttickets.util.Utility.Companion.NOTIFICATION_CHANNEL_ID
import kotlinx.android.synthetic.main.activity_create_event.*


class MainActivity : AppCompatActivity() {

    companion object {
        private val LOG_TAG = MainActivity::class.simpleName
        private const val ACTIVE_FRAGMENT_KEY = "activeFragment"
    }

    private val mViewModel by lazy {
        ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        createNotificationChannel()
        setupViews(savedInstanceState)
    }

    override fun onBackPressed() {
        if (ticketDetailView.visibility == View.VISIBLE) {
            hideTicketDetailView()
            return
        }

        moveTaskToBack(true)
    }

    private fun hideTicketDetailView() {
        ticketDetailView.visibility = View.GONE
    }

    private fun setupViews(savedInstanceState: Bundle?) {
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
                loadFragment(R.id.fragmentContainer,
                        supportFragmentManager, DiscoverFragment())
            }
            R.id.navigation_my_tickets -> {
                loadFragment(R.id.fragmentContainer,
                        supportFragmentManager, MyTicketsFragment())
            }
            R.id.navigation_create -> {
                loadFragment(R.id.fragmentContainer,
                        supportFragmentManager, ManageEventsFragment())
            }
            R.id.navigation_wallet -> {
                loadFragment(R.id.fragmentContainer,
                        supportFragmentManager, MyWalletFragment())
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ACTIVE_FRAGMENT_KEY, bottomNavigation.selectedItemId)
        super.onSaveInstanceState(outState)
    }
}
