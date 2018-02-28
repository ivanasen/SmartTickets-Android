package com.ivanasen.smarttickets.ui


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Fade
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast

import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.util.Utility.Companion.launchActivity
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener


/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private val LOG_TAG = LoginFragment::class.java

    private lateinit var mRootView: View
    private val mViewModel: WelcomeActivityViewModel by lazy {
        ViewModelProviders.of(this).get(WelcomeActivityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_login, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeLiveData()
    }

    private fun observeLiveData() {
        mViewModel.credentials.observe(this, Observer {
            if (it != null) {
                launchActivity(this@LoginFragment.context!!, MainActivity::class.java)
            }
        })

        mViewModel.wrongPasswordAttempts.observe(this, Observer {
            Toast.makeText(this@LoginFragment.context, "Wrong Password", Toast.LENGTH_SHORT)
                    .show()
        })
    }

    private fun setupViews() {
        inputPassword.textChangedListener {
            onTextChanged { text, _, _, _ ->
                if (text != null && text.isNotEmpty()) {
                    unlockWalletBtn.isEnabled = true
                }
            }
        }

        unlockWalletBtn.onClick {
            val password = inputPassword.text.toString()
            mViewModel.unlockWallet(password, this@LoginFragment.context!!)
            showLoadingScreen()
        }
    }

    private fun showLoadingScreen() {
        TransitionManager.beginDelayedTransition(view as ViewGroup, Fade())
        loginForm.visibility = View.GONE
        loadingScreen.visibility = View.VISIBLE
        loginFormContrainer.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
    }
}
