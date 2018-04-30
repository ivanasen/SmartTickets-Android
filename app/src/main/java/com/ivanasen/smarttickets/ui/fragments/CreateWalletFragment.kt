package com.ivanasen.smarttickets.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout

import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.ui.activities.WelcomeActivity
import com.ivanasen.smarttickets.util.Utility.Companion.importWallet
import com.ivanasen.smarttickets.viewmodels.WelcomeActivityViewModel
import com.ivanasen.smarttickets.util.Utility.Companion.isValidPassword
import com.ivanasen.smarttickets.util.toPx
import kotlinx.android.synthetic.main.create_wallet_form_layout.*
import kotlinx.android.synthetic.main.fragment_create_wallet.*
import kotlinx.android.synthetic.main.wallet_created_layout.*
import kotlinx.android.synthetic.main.welcome_layout.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.animation.ScaleAnimation


class CreateWalletFragment : Fragment() {

    private val LOG_TAG = CreateWalletFragment::class.java.simpleName

    private val WELCOME_LAYOUT_ANIMATION_SCALE = 0.7f

    private lateinit var mRootView: View
    private val mViewModel: WelcomeActivityViewModel by lazy {
        ViewModelProviders.of(this).get(WelcomeActivityViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_create_wallet, container, false)
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

        mViewModel.walletExists.observe(this, Observer {
            if (it == true) {
                showWalletCreatedScreen()
            }
        })
    }

    private fun showWalletCreatedScreen() {
        TransitionManager.beginDelayedTransition(mRootView as ViewGroup, Fade())
        createWalletForm.visibility = View.GONE
        walletCreatedView.visibility = View.VISIBLE
        createWalletContainer.layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT
        (createWalletContainer.layoutParams as RelativeLayout.LayoutParams)
                .setMargins(0, 0, 0, 0)
    }

    private fun checkPasswords() {
        val password = mViewModel.password.value
        val confirmPassword = mViewModel.confirmPassword.value

        if (isValidPassword(password ?: "") && password == confirmPassword) {
            invalidPasswordsView.visibility = View.INVISIBLE
            attemptCreateWalletBtn.isEnabled = true
        } else if (!password.equals(confirmPassword)) {
            invalidPasswordsView.text = getString(R.string.passwords_don_t_match_text)
            invalidPasswordsView.visibility = View.VISIBLE
            attemptCreateWalletBtn.isEnabled = false
        } else if (password != null && password.isNotEmpty()) {
            invalidPasswordsView.text = getString(R.string.password_too_short_text)
            invalidPasswordsView.visibility = View.VISIBLE
            attemptCreateWalletBtn.isEnabled = false
        } else if (password != null && confirmPassword != null && password.isEmpty() && confirmPassword.isEmpty()) {
            invalidPasswordsView.visibility = View.INVISIBLE
        }
    }

    private fun setupViews() {
        createWalletBtn.onClick {
            TransitionManager.beginDelayedTransition(mRootView as ViewGroup, Slide(Gravity.BOTTOM))
            createWalletContainer.visibility = View.VISIBLE
            walletCreationView.visibility = View.GONE

            val scale = WELCOME_LAYOUT_ANIMATION_SCALE
            val scaleAnimation = ScaleAnimation(1f, scale, 1f, scale,
                    welcomeLayout.width / 2.0f, welcomeLayout.height / 2.0f)
            scaleAnimation.duration =
                    resources.getInteger(R.integer.scale_animation_duration).toLong()
            scaleAnimation.fillAfter = true
            scaleAnimation.setInterpolator(this@CreateWalletFragment.context,
                    android.R.interpolator.accelerate_decelerate)
            welcomeLayout.startAnimation(scaleAnimation)
        }

        recoverWalletBtn.onClick {
            activity?.let { importWallet(it) }
        }

        cancelCreateWalletBtn.onClick {
            TransitionManager.beginDelayedTransition(mRootView as ViewGroup, Slide(Gravity.BOTTOM))
            createWalletContainer.visibility = View.GONE
            walletCreationView.visibility = View.VISIBLE

            val scale = WELCOME_LAYOUT_ANIMATION_SCALE
            val scaleAnimation = ScaleAnimation(scale, 1f, scale, 1f,
                    welcomeLayout.width / 2.0f, welcomeLayout.height / 2.0f)
            scaleAnimation.fillAfter = true
            scaleAnimation.duration =
                    resources.getInteger(R.integer.scale_animation_duration).toLong()
            scaleAnimation.setInterpolator(this@CreateWalletFragment.context,
                    android.R.interpolator.accelerate_decelerate)
            welcomeLayout.startAnimation(scaleAnimation)

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

        }

        backupBtn.onClick {
            mViewModel.backupWallet(activity!!)
        }

        skipBackupBtn.onClick {
            (activity as WelcomeActivity).loadLoginFragment()
        }
    }
}
