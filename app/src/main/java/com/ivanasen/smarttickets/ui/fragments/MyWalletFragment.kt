package com.ivanasen.smarttickets.ui.fragments

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
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import com.ivanasen.smarttickets.util.Utility.Companion.copyToClipboard
import kotlinx.android.synthetic.main.fragment_my_wallet.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.web3j.crypto.WalletUtils
import java.text.DecimalFormat


class MyWalletFragment : Fragment() {

    private val LOG_TAG = MyWalletFragment::class.java.simpleName
    private val DIALOG_WIDTH: Int = 1000

    private val mViewModel: AppViewModel by lazy {
        ViewModelProviders.of(activity as FragmentActivity).get(AppViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeLiveData()
    }

    private fun setupViews() {
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

        sendEtherBtn.onClick { showSendEtherDialog() }
    }

    private fun showSendEtherDialog() {
        context?.let {
            MaterialDialog.Builder(it)
                    .title(R.string.send_ether_dialog_title)
                    .customView(R.layout.send_ether_layout, true)
                    .positiveText(getString(R.string.send_text))
                    .positiveColor(resources.getColor(R.color.colorPrimary))
                    .onPositive({ dialog, _ ->
                        val address = dialog.customView!!.findViewById<TextView>(R.id.inputAddress)
                                .text.toString()
                        val amount = dialog.customView!!.findViewById<TextView>(R.id.etherAmount)
                                .text.toString()
                                .toDouble()

                        require(WalletUtils.isValidAddress(address))
                        require(amount > 0)

                        Log.d(LOG_TAG, "$address, $amount")

                        mViewModel.sendEther(address, amount)
                    })
                    .negativeText(getString(R.string.cancel_text))
                    .show()
        }
    }

    private fun observeLiveData() {
        mViewModel.etherBalance.observe(this, Observer {
            val df = DecimalFormat(getString(R.string.eth_format))
            etherBalanceView.text = df.format(it)
        })

        mViewModel.usdBalance.observe(this, Observer {
            val df = DecimalFormat(getString(R.string.usd_format))
            etherInUsdView.text = df.format(it)
        })
    }

    private fun showAddressDialog() {
        context?.let {
            val dialog = AlertDialog.Builder(context)
                    .setView(R.layout.wallet_address_dialog_layout)
                    .create()
            dialog.show()

            dialog.window.setLayout(DIALOG_WIDTH,
                    ViewGroup.LayoutParams.WRAP_CONTENT)

            val walletAddressQrView = dialog.findViewById<ImageView>(R.id.walletAddressQr)
            val walletAddressTextView = dialog.findViewById<TextView>(R.id.walletAddressTextView)
            val copyBtn = dialog.findViewById<Button>(R.id.copyAddressBtn)

            val address = walletAddressView.text.toString()
            val walletBitmap = QRCode.from(address).withSize(800, 800).bitmap()
            walletAddressQrView.imageBitmap = walletBitmap
            walletAddressTextView.text = address

            copyBtn.onClick {
                copyToClipboard(this@MyWalletFragment.context!!,
                        getString(R.string.wallet_address_clipboard_label),
                        address)
            }
        }
    }
}
