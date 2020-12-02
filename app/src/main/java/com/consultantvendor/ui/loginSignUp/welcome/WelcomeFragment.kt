package com.consultantvendor.ui.loginSignUp.welcome

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentWelcomeBinding
import com.consultantvendor.ui.loginSignUp.login.LoginFragment
import com.consultantvendor.ui.loginSignUp.loginemail.LoginEmailFragment
import com.consultantvendor.ui.walkthrough.WalkThroughFragment
import com.consultantvendor.ui.walkthrough.WalkThroughFragment.Companion.WALKTHROUGH_SCREEN
import com.consultantvendor.utils.PrefsManager
import com.consultantvendor.utils.hideKeyboard
import com.consultantvendor.utils.replaceFragment
import com.consultantvendor.utils.setAcceptTerms
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class WelcomeFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentWelcomeBinding

    private var rootView: View? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome, container, false)
            rootView = binding.root

            initialise()
            listeners()
        }
        return rootView
    }

    private fun initialise() {
        binding.tvTerms.hideKeyboard()
        binding.tvTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.tvTerms.setText(setAcceptTerms(requireActivity()), TextView.BufferType.SPANNABLE)

        if(!prefsManager.getBoolean(WALKTHROUGH_SCREEN,false)){
            replaceFragment(requireActivity().supportFragmentManager,
                    WalkThroughFragment(), R.id.container)
        }
    }


    private fun listeners() {
        binding.tvSignUpMobile.setOnClickListener {
            val fragment = LoginFragment()
            val bundle = Bundle()
            bundle.putBoolean(EXTRA_SIGNUP, true)
            fragment.arguments = bundle

            replaceFragment(requireActivity().supportFragmentManager,
                    fragment, R.id.container)
        }

        binding.tvLogin.setOnClickListener {
            replaceFragment(requireActivity().supportFragmentManager,
                    LoginFragment(), R.id.container)
        }

        binding.tvSignUpEmail.setOnClickListener {
            val fragment = LoginEmailFragment()
            val bundle = Bundle()
            bundle.putBoolean(EXTRA_SIGNUP, true)
            fragment.arguments = bundle

            replaceFragment(requireActivity().supportFragmentManager,
                    fragment, R.id.container)
        }
    }

    companion object {
        const val EXTRA_SIGNUP = "EXTRA_SIGNUP"
        const val EXTRA_LOGIN = "EXTRA_LOGIN"
    }
}
