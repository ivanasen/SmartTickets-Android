package com.ivanasen.smarttickets.ui

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.util.Utility.Companion.copyToClipboard
import kotlinx.android.synthetic.main.fragment_my_wallet.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.sdk25.coroutines.onClick


class MyWalletFragment : Fragment() {

    private val LOG_TAG = MyWalletFragment::class.java.simpleName
    private val DIALOG_WIDTH: Int = 1000

    private val mViewModel: MainActivityViewModel by lazy {
        ViewModelProviders.of(activity as FragmentActivity).get(MainActivityViewModel::class.java)
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
        Log.d(LOG_TAG, walletAddress)

        val url = String.format(getString(R.string.gravatar_url), walletAddress)
        Glide.with(this)
                .load(url)
                .into(walletIdenticonView)

        etherBalanceView.text = String.format("%.5f", mViewModel.etherBalance.value)
        etherInUsdView.text = String.format("%.2f", mViewModel.usdBalance.value)

        showQrCodeBtn.onClick { showAddressDialog() }
        receiveEtherBtn.onClick { showAddressDialog() }
    }

    private fun observeLiveData() {
        mViewModel.etherBalance.observe(this, Observer {
            etherBalanceView.text = it.toString()
        })
    }

    private fun showAddressDialog() {
        context?.let {
            val dialog = AlertDialog.Builder(context)
                    .setView(createAddressDialogView())
                    .create()
            dialog.show()

            dialog.window.setLayout(DIALOG_WIDTH,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    private fun createAddressDialogView(): View? {
        val address = walletAddressView.text.toString()
        val walletAddressDialog = layoutInflater
                .inflate(R.layout.wallet_address_dialog_layout, view as ViewGroup, false)
        val walletAddressQrView = walletAddressDialog.findViewById<ImageView>(R.id.walletAddressQr)
        val walletAddressTextView = walletAddressDialog.findViewById<TextView>(R.id.walletAddressTextView)
        val copyBtn = walletAddressDialog.findViewById<Button>(R.id.copyAddressBtn)

        val walletBitmap = QRCode.from(address).withSize(800, 800).bitmap()
        walletAddressQrView.imageBitmap = walletBitmap
        walletAddressTextView.text = address

        copyBtn.onClick {
            copyToClipboard(this@MyWalletFragment.context!!,
                    getString(R.string.wallet_address_clipboard_label),
                    address)
        }
        return walletAddressDialog
    }
}
