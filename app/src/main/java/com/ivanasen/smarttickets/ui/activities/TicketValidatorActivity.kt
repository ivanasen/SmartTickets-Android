package com.ivanasen.smarttickets.ui.activities

import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ivanasen.smarttickets.R
import android.nfc.NdefRecord
import android.util.Log


class TicketValidatorActivity : AppCompatActivity() {

    private val LOG_TAG = TicketValidatorActivity::class.java.simpleName

    private val mNfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_validator)
        title = getString(R.string.ticket_validator_title)
    }

    override fun onResume() {
        super.onResume()

        val action = intent.action
        if (action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            val inNdefMessage = parcelables[0] as NdefMessage
            val inNdefRecords = inNdefMessage.records
            val ndefRecord0 = inNdefRecords[0]
            val inMsg = String(ndefRecord0.payload)

            Log.d(LOG_TAG, inMsg)
        }
    }
}
