package com.consultantvendor.ui.loginSignUp.register

import android.app.Activity
import android.content.Intent
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
import com.consultantvendor.databinding.FragmentRegisterBinding
import com.consultantvendor.utils.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class RegisterFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentRegisterBinding

    private var rootView: View? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        binding.cvHomeCare.gone()
        binding.ilNotSelf.gone()
        binding.etNameOther.gone()

        binding.cbTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.cbTerms.setText(setAcceptTerms(requireActivity()), TextView.BufferType.SPANNABLE)
    }


    private fun listeners() {

        binding.tvSelectHomeCare.setOnClickListener {
            binding.cvHomeCare.hideShowView(binding.cvHomeCare.visibility == View.GONE)
        }

        binding.rgRequestService.setOnCheckedChangeListener { radioGroup, id ->
            val isNotSelf = id != R.id.rbSelf
            binding.ilNotSelf.hideShowView(isNotSelf)
            binding.etNameOther.hideShowView(isNotSelf)
        }

        binding.etServiceDate.setOnClickListener {
            /*replaceFragment(requireActivity().supportFragmentManager,
                    DateTimeFragment(), R.id.container)*/
        }

        binding.tvContinue.setOnClickListener {
           /* replaceFragment(requireActivity().supportFragmentManager,
                    WaitingAllocationFragment(), R.id.container)*/
        }
    }

    private fun bindObservers() {

    }
}
