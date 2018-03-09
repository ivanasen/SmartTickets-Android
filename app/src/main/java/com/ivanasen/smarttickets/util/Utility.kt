package com.ivanasen.smarttickets.util

import android.content.Context
import android.content.Intent
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import com.ivanasen.smarttickets.R
import java.io.File
import com.ivanasen.smarttickets.BuildConfig
import java.io.ByteArrayOutputStream
import android.support.v4.content.ContextCompat.startActivity
import org.jetbrains.anko.startActivity
import com.ivanasen.smarttickets.ui.activities.MainActivity
import android.support.v4.content.FileProvider


class Utility {
    companion object {
        val MIN_PASSWORD_LENGTH = 8
        val ONE_ETHER_IN_WEI = Math.pow(10.0, 18.0).toLong()
        val IPFS_HASH_HEADER = "ipfs-hash"
        val WALLET_FILE_NAME_KEY = "WalletFileNameKey"

        private val PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"

        fun loadFragment(@IdRes containerViewId: Int, fragmentManager: FragmentManager, fragment: Fragment) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(containerViewId, fragment)
            transaction.addToBackStack(null)
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
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            require(walletFile.exists())

            val walletUri = FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, walletFile)
            sendIntent.putExtra(Intent.EXTRA_STREAM, walletUri)
            sendIntent.type = "text/plain"

            context.startActivity(Intent.createChooser(sendIntent,
                    context.getString(R.string.backup_wallet_to_text)))
        }

        fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray? {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }

        fun convertByteArrayToBitmap(byteArray: ByteArray): Bitmap? =
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

        fun getIpfsImageUrl(imageHash: String): String =
                "${BuildConfig.IPFS_GATEWAY_URL}/ipfs/$imageHash"

//        enum
    }

}