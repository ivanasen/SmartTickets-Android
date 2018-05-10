package com.ivanasen.smarttickets.ui.activities

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ivanasen.smarttickets.R
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.blikoon.qrcodescanner.QrCodeActivity
import com.ivanasen.smarttickets.util.Utility
import com.ivanasen.smarttickets.util.Utility.Companion.showSnackBar
import com.ivanasen.smarttickets.viewmodels.AppViewModel
import kotlinx.android.synthetic.main.activity_ticket_validator.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class TicketValidatorActivity : AppCompatActivity() {

    private val LOG_TAG = TicketValidatorActivity::class.java.simpleName

    private val REQUEST_CODE_QR_SCAN = 101
    private val PERMISSIONS_CAMERA = 1
    private val REQUEST_QR_CODE_RESULT_EXTRA = "com.blikoon.qrcodescanner.got_qr_scan_relult"

    private val mViewModel: AppViewModel by lazy {
        ViewModelProviders.of(this).get(AppViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_validator)

        setupViews()
    }

    private fun setupViews() {
        title = getString(R.string.ticket_validator_title)
        supportActionBar?.elevation = 0f
        tapToScanView.start()

        scanTicketBtn.onClick { openQrScanner() }
        scanAnotherTicketBtnError.onClick { openQrScanner() }
        scanAnotherTicketBtnSuccess.onClick { openQrScanner() }
    }

    private fun openQrScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSIONS_CAMERA)
        } else {
            val scanQrCodeIntent = Intent(this@TicketValidatorActivity,
                    QrCodeActivity::class.java)
            startActivityForResult(scanQrCodeIntent, REQUEST_CODE_QR_SCAN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_QR_SCAN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = data?.getStringExtra(REQUEST_QR_CODE_RESULT_EXTRA)
                    Log.d(LOG_TAG, result)
                    result?.let { verifyTicket(it) }
                } else {
                    showSnackBar(this, R.string.qr_code_error)
                }
            }
        }
    }

    private fun verifyTicket(qrCodeString: String) {
        Log.d(LOG_TAG, qrCodeString)
        mViewModel.verifyTicket(qrCodeString).observe(this, Observer {
            when (it) {
                Utility.Companion.TransactionStatus.PENDING -> showValidationInProcessView()
                Utility.Companion.TransactionStatus.SUCCESS -> showValidationSuccessView()
                Utility.Companion.TransactionStatus.FAILURE,
                Utility.Companion.TransactionStatus.ERROR -> showValidationErrorView()
            }
        })
    }

    private fun showValidationInProcessView() {
        tapToScanView.visibility = View.GONE
        ticketValidatingErrorView.visibility = View.GONE
        ticketValidatingSuccessView.visibility = View.GONE
        ticketValidatingInProcessView.visibility = View.VISIBLE
    }

    private fun showValidationSuccessView() {
        tapToScanView.visibility = View.GONE
        ticketValidatingErrorView.visibility = View.GONE
        ticketValidatingSuccessView.visibility = View.VISIBLE
        ticketValidatingInProcessView.visibility = View.GONE
    }

    private fun showValidationErrorView() {
        tapToScanView.visibility = View.GONE
        ticketValidatingErrorView.visibility = View.VISIBLE
        ticketValidatingSuccessView.visibility = View.GONE
        ticketValidatingInProcessView.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_CAMERA -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    openQrScanner()
                } else {
                    showSnackBar(this, R.string.camera_permission_message)
                }
                return
            }
        }
    }

}
