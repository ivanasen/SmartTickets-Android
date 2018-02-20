package com.ivanasen.smarttickets

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.ivanasen.smarttickets.api.SmartTicketsCore
import com.ivanasen.smarttickets.api.Web3JProvider
import com.ivanasen.smarttickets.api.Web3JProvider.Companion.web3
import com.ivanasen.smarttickets.api.generateWallet
import com.ivanasen.smarttickets.util.Utility.Companion.loadFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.core.methods.request.Transaction
import java.io.File
import java.math.BigInteger


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        Thread {
//            Log.d(TAG, web3.ethAccounts().send().accounts[0])
            val file = File(this@MainActivity.filesDir, "")
//            val credentials = Credentials.create("c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3")
            val wallet = WalletUtils.generateFullNewWalletFile("123", file)
//            val contract = SmartTicketsCore.deploy(web3, credentials,
//                    web3.ethGasPrice().send().gasPrice,
//                    SmartTicketsCore.GAS_LIMIT).send()
            val contract = SmartTicketsCore.load(
                    "0x8cdaf0cd259887258bc13a92c0a6da92698644c0",
                    web3,
                    WalletUtils.loadCredentials("123", File(file, wallet)),
                    web3.ethGasPrice().send().gasPrice,
                    SmartTicketsCore.GAS_LIMIT)
            val result = contract.ceoAddress().send()
            Log.d(TAG, "ceoAddress: $result")
        }.start()

        setupViews()
    }

    private fun setupViews() {

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
