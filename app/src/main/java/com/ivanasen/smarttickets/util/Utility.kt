package com.ivanasen.smarttickets.util

import android.content.Context
import android.content.Intent
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat.startActivity
import com.ivanasen.smarttickets.ui.MainActivity

class Utility {
    companion object {
        val MIN_PASSWORD_LENGTH = 8
        val ONE_ETHER_IN_WEI = Math.pow(10.0, 18.0).toLong()
        const val INFURA_ETHER_PRICE_IN_USD_URL = "https://api.infura.io/v1/ticker/ethusd"

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
    }

}