package com.ivanasen.smarttickets.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Resources
import android.widget.Toast
import com.ivanasen.smarttickets.R
import java.io.File
import com.ivanasen.smarttickets.BuildConfig
import android.support.v4.content.FileProvider
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.util.TypedValue
import java.math.BigInteger


class Utility {
    companion object {
        val MIN_PASSWORD_LENGTH = 8
        val ONE_ETHER_IN_WEI = Math.pow(10.0, 18.0).toLong()
        val IPFS_HASH_HEADER = "ipfs-hash"
        val WALLET_FILE_NAME_KEY = "WalletFileNameKey"
        val IPFS_URL_PATH = "ipfs"

        val IMPORT_WALLET_REQUEST_CODE = 42


        private val PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"

        enum class TransactionStatus {
            PENDING, SUCCESS, FAILURE, ERROR
        }

        fun loadFragment(@IdRes containerViewId: Int, fragmentManager: FragmentManager, fragment: Fragment) {
            val transaction = fragmentManager.beginTransaction()
                    .replace(containerViewId, fragment)
                    .addToBackStack(null)
            transaction.commit()
        }

        fun isValidPassword(password: String): Boolean {
            return password.length >= MIN_PASSWORD_LENGTH
        }

        fun launchActivity(context: Context, cls: Class<*>) {
            val intent = Intent(context, cls)
            context.startActivity(intent)
        }

        fun copyToClipboard(context: Context, label: String, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.primaryClip = clip

            Toast.makeText(context,
                    context.getString(R.string.copied_to_clipboard),
                    Toast.LENGTH_SHORT)
                    .show()
        }

        fun backupWallet(context: Context, walletFile: File) {
            // Create the text message with a string
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND


            val walletUri = FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, walletFile)
            sendIntent.putExtra(Intent.EXTRA_STREAM, walletUri)
            sendIntent.type = "text/plain"

            context.startActivity(Intent.createChooser(sendIntent,
                    context.getString(R.string.backup_wallet_to_text)))
        }

        fun importWallet(activity: Activity) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/plain"
            startActivityForResult(activity, intent, IMPORT_WALLET_REQUEST_CODE, null)
        }

        fun getIpfsImageUrl(imageHash: String): String =
                "${BuildConfig.IPFS_GATEWAY_URL}/$IPFS_URL_PATH/$imageHash"


    }
}

val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val BigInteger.toEther: Double
    get() = (this.toDouble() / Math.pow(10.0, 18.0))