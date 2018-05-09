package com.ivanasen.smarttickets.ui.fragments


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.transition.Fade
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast

import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.activities.MainActivity
import com.ivanasen.smarttickets.ui.activities.WelcomeActivity
import com.ivanasen.smarttickets.viewmodels.WelcomeActivityViewModel
import com.ivanasen.smarttickets.util.Utility.Companion.launchActivity
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener


/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private val LOG_TAG = LoginFragment::class.java.simpleName

    private lateinit var mRootView: View
    private val mViewModel: WelcomeActivityViewModel by lazy {
        ViewModelProviders.of(activity as FragmentActivity).get(WelcomeActivityViewModel::class.java)
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
                showLoadingScreen()
                mViewModel.loadInitialAppData()
            }
        })

        mViewModel.wrongPasswordAttempts.observe(this, Observer {
            Toast.makeText(this@LoginFragment.context,
                    getString(R.string.wrong_password_msg),
                    Toast.LENGTH_SHORT)
                    .show()
        })

        mViewModel.contractDeployed.observe(this, Observer {
            if (it == true) {
                val intent = Intent(context, MainActivity::class.java)
                context?.startActivity(intent)
            }
        })
    }

    private fun setupViews() {
        useOtherWalletBtn.onClick {
            (activity as WelcomeActivity).loadWalletCreationFragment()
        }

        unlockWalletBtn.onClick {
            if (inputPassword.text == null || inputPassword.text.toString().isEmpty()) {
                Toast.makeText(this@LoginFragment.context,
                        getString(R.string.password_field_empty_msg),
                        Toast.LENGTH_LONG)
                        .show()
            } else {
                val password = inputPassword.text.toString()
                mViewModel.unlockWallet(password, this@LoginFragment.context!!)
            }

        }
    }

    private fun showLoadingScreen() {
        TransitionManager.beginDelayedTransition(view as ViewGroup, Fade())
        loginForm.visibility = View.GONE
        loadingScreen.visibility = View.VISIBLE
        loginFormContainer.layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT
        (loginFormContainer.layoutParams as RelativeLayout.LayoutParams)
                .setMargins(0, 0, 0, 0)
    }
}
