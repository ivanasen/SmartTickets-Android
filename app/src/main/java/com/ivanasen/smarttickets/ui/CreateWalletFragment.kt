package com.ivanasen.smarttickets.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.util.Utility.Companion.isValidPassword
import kotlinx.android.synthetic.main.fragment_create_wallet.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class CreateWalletFragment : Fragment() {

    private lateinit var mRootView: View
    private lateinit var mViewModel: WelcomeActivityViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_create_wallet, container, false)
        mViewModel = ViewModelProviders.of(this).get(WelcomeActivityViewModel::class.java)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        observeLiveData()
    }

    private fun observeLiveData() {
        mViewModel.password.observe(this, Observer {
            checkPasswords()
        })

        mViewModel.confirmPassword.observe(this, Observer {
            checkPasswords()
        })
    }

    private fun checkPasswords() {
        val password = mViewModel.password.value
        val confirmPassword = mViewModel.confirmPassword.value

        if (isValidPassword(password ?: "") && password == confirmPassword) {
            invalidPasswordsView.visibility = View.INVISIBLE
            attemptCreateWalletBtn.isEnabled = true
        } else if (password != confirmPassword) {
            invalidPasswordsView.text = getString(R.string.passwords_don_t_match_text)
            invalidPasswordsView.visibility = View.VISIBLE
            attemptCreateWalletBtn.isEnabled = false
        } else {
            invalidPasswordsView.text = getString(R.string.password_too_short_text)
            invalidPasswordsView.visibility = View.VISIBLE
            attemptCreateWalletBtn.isEnabled = false
        }
    }

    private fun setupViews() {
        createWalletBtn.onClick {
            TransitionManager.beginDelayedTransition(mRootView as ViewGroup, Slide(Gravity.BOTTOM))
            createWalletForm.visibility = View.VISIBLE
            walletCreationView.visibility = View.GONE
        }

        recoverWalletBtn.onClick {

        }

        cancelCreateWalletBtn.onClick {
            TransitionManager.beginDelayedTransition(mRootView as ViewGroup, Slide(Gravity.BOTTOM))
            createWalletForm.visibility = View.GONE
            walletCreationView.visibility = View.VISIBLE

            inputPassword.setText("")
            confirmPassword.setText("")
        }

        inputPassword.textChangedListener {
            onTextChanged { text, _, _, _ ->
                mViewModel.password.postValue(text.toString())
            }
        }

        confirmPassword.textChangedListener {
            onTextChanged { text, _, _, _ ->
                mViewModel.confirmPassword.postValue(text.toString())
            }
        }

        attemptCreateWalletBtn.onClick {
            mViewModel.createNewWallet(mViewModel.password.value!!,
                    this@CreateWalletFragment.context!!)

            startActivity(Intent(this@CreateWalletFragment.context, MainActivity::class.java))
        }
    }

}// Required empty public constructor
