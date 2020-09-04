package com.consultantvendor.ui.walkthrough

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.network.ApiKeys
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.ProviderType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentWalkthroughBinding
import com.consultantvendor.databinding.FragmentWelcomeBinding
import com.consultantvendor.ui.adapter.CommonFragmentPagerAdapter
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.login.LoginFragment
import com.consultantvendor.ui.loginSignUp.loginemail.LoginEmailFragment
import com.consultantvendor.ui.loginSignUp.signup.SignUpFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class WalkThroughFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentWalkthroughBinding

    private var rootView: View? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_walkthrough, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setBanners()
        }
        return rootView
    }

    private fun initialise() {
    }

    private fun setBanners() {
        val adapter = CommonFragmentPagerAdapter(requireActivity().supportFragmentManager)
        for (i in 0..2) {
            val fragment = WalkthroughDetailFragment()
            val bundle = Bundle()
            bundle.putInt(POSITION, i)
            fragment.arguments = bundle
            adapter.addTab("", fragment)
        }

        binding.viewPager.adapter = adapter

        binding.pageIndicatorView.setViewPager(binding.viewPager)
    }

    private fun listeners() {
        binding.tvGetStarted.setOnClickListener {
            prefsManager.save(WALKTHROUGH_SCREEN, true)
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    companion object {
        const val WALKTHROUGH_SCREEN = "WALKTHROUGH_SCREEN"
    }
}
