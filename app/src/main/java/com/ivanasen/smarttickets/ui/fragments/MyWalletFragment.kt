package com.ivanasen.smarttickets.ui.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.adapters.TransactionAdapter
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.WALLET_FILE_NAME_KEY
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import com.ivanasen.smarttickets.util.Utility.Companion.copyToClipboard
import com.ivanasen.smarttickets.util.toPx
import kotlinx.android.synthetic.main.fragment_my_wallet.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import org.web3j.crypto.WalletUtils
import java.io.File
import java.text.DecimalFormat


class MyWalletFragment : Fragment() {

    private val LOG_TAG = MyWalletFragment::class.java.simpleName
    private val QR_CODE_SIZE = 260.toPx

    private val mViewModel: AppViewModel by lazy {
        if (activity != null)
            ViewModelProviders.of(activity as FragmentActivity).get(AppViewModel::class.java)
        else
            ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = getString(R.string.title_wallet)
        setHasOptionsMenu(true)


        return inflater.inflate(R.layout.fragment_my_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeLiveData()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.my_wallet, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.navigation_backup -> {
                val walletName = context?.defaultSharedPreferences
                        ?.getString(WALLET_FILE_NAME_KEY, "")
                val wallet = File(context?.filesDir, walletName)
                Utility.backupWallet(context!!, wallet)
            }
            R.id.navigation_private_key -> {
                showPrivateKeyDialog()
            }
        }
        return true
    }

    private fun showPrivateKeyDialog() {
        val privateKeyDialog = {
            val privateKeyString = mViewModel.credentials.value?.ecKeyPair?.privateKey?.toString(16)
            MaterialDialog.Builder(context!!)
                    .title(getString(R.string.wallet_private_key_title))
                    .content(privateKeyString as CharSequence)
                    .positiveText(getString(R.string.WRITTEN_DOWN))
                    .show()
        }

        MaterialDialog.Builder(context!!)
                .title(getString(R.string.private_key_type_password_text))
                .input(getString(R.string.password_input_hint),
                        "",
                        false,
                        { _, _ -> })
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .positiveText(getString(R.string.show_text))
                .autoDismiss(false)
                .onPositive({ dialog, _ ->
                    val password = dialog.inputEditText?.text.toString()

                    if (mViewModel.checkPassword(context!!, password)) {
                        privateKeyDialog()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context,
                                getString(R.string.wrong_password_text),
                                Toast.LENGTH_SHORT)
                                .show()
                    }
                })
                .show()
    }

    private fun setupViews() {
        val walletAddress = mViewModel.credentials.value?.address
        Log.d(LOG_TAG, walletAddress)

        walletAddressView.text = walletAddress

        showQrCodeBtn.onClick { showAddressDialog() }
        receiveEtherBtn.onClick { showAddressDialog() }

        sendEtherBtn.onClick { showSendEtherDialog() }

        walletRefreshLayout.setColorSchemeColors(resources.getColor(R.color.pink),
                resources.getColor(R.color.yellow),
                resources.getColor(R.color.pink))
        walletRefreshLayout.isRefreshing = true
        walletRefreshLayout.onRefresh {
            mViewModel.fetchBalance()
        }

        val adapter = activity?.let { TransactionAdapter(it, mViewModel.txHistory) }
        txHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
        txHistoryRecyclerView.adapter = adapter
        txHistoryRecyclerView.isNestedScrollingEnabled = false
    }


    private fun showSendEtherDialog() {
        context?.let {
            MaterialDialog.Builder(it)
                    .title(R.string.send_ether_dialog_title)
                    .customView(R.layout.send_ether_layout, true)
                    .autoDismiss(false)
                    .positiveText(getString(R.string.send_text))
                    .onNegative({ dialog, _ -> dialog.dismiss() })
                    .onPositive({ dialog, _ ->
                        val address = dialog.customView!!.findViewById<TextView>(R.id.inputAddress)
                                .text.toString()
                        val amount = dialog.customView!!.findViewById<TextView>(R.id.etherAmount)
                                .text.toString()

                        if (!WalletUtils.isValidAddress(address)) {
                            Toast.makeText(context,
                                    getString(R.string.invalid_address_msg),
                                    Toast.LENGTH_LONG)
                                    .show()
                        } else if (!amount.isNotEmpty() || amount.toDouble() <= 0) {
                            Toast.makeText(context,
                                    getString(R.string.invalid_amount_msg),
                                    Toast.LENGTH_LONG)
                                    .show()
                        } else {
                            mViewModel.sendEther(address, amount.toDouble())
                            dialog.dismiss()
                        }
                    })
                    .negativeText(getString(R.string.cancel_text))
                    .show()
        }
    }

    private fun observeLiveData() {
        mViewModel.etherBalance.observe(this, Observer {
            val df = DecimalFormat(getString(R.string.eth_format))
            etherBalanceView.text = df.format(it)
            walletRefreshLayout.isRefreshing = false
        })

        mViewModel.usdBalance.observe(this, Observer {
            it?.let {
                val usdDollars = it / 100
                val df = DecimalFormat(getString(R.string.usd_format))
                etherInUsdView.text = df.format(usdDollars)
            }
        })

        mViewModel.fetchTxHistory().observe(this, Observer {
            when (it) {
                Utility.Companion.TransactionStatus.PENDING -> {
                    txHistoryProgressBar.visibility = View.VISIBLE
                    txHistoryEmptyView.visibility = View.GONE
                    txHistoryRecyclerView.visibility = View.GONE
                    txHistoryErrorView.visibility = View.GONE
                }

                Utility.Companion.TransactionStatus.SUCCESS -> {
                    txHistoryProgressBar.visibility = View.GONE
                    txHistoryEmptyView.visibility = View.GONE
                    txHistoryRecyclerView.visibility = View.VISIBLE
                    txHistoryErrorView.visibility = View.GONE
                }
                Utility.Companion.TransactionStatus.FAILURE -> {
                    txHistoryProgressBar.visibility = View.GONE
                    txHistoryEmptyView.visibility = View.VISIBLE
                    txHistoryRecyclerView.visibility = View.GONE
                    txHistoryErrorView.visibility = View.GONE
                }
                Utility.Companion.TransactionStatus.ERROR -> {
                    txHistoryProgressBar.visibility = View.GONE
                    txHistoryEmptyView.visibility = View.GONE
                    txHistoryRecyclerView.visibility = View.GONE
                    txHistoryErrorView.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun showAddressDialog() {
        context?.let {
            val dialog = AlertDialog.Builder(context)
                    .setView(R.layout.wallet_address_dialog_layout)
                    .create()
            dialog.show()

            val walletAddressQrView = dialog.findViewById<ImageView>(R.id.walletAddressQr)
            val walletAddressTextView = dialog.findViewById<TextView>(R.id.walletAddressTextView)
            val copyBtn = dialog.findViewById<Button>(R.id.copyAddressBtn)

            val address = walletAddressView.text.toString()
            val walletQrCode = QRCode.from(address).withSize(QR_CODE_SIZE, QR_CODE_SIZE).bitmap()
            walletAddressQrView.imageBitmap = walletQrCode
            walletAddressTextView.text = address

            copyBtn.onClick {
                copyToClipboard(this@MyWalletFragment.context!!,
                        getString(R.string.wallet_address_clipboard_label),
                        address)
            }
        }
    }
}
