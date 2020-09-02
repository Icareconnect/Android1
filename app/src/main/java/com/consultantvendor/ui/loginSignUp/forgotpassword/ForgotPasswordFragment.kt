package com.consultantvendor.ui.loginSignUp.forgotpassword

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentForgotPasswordBinding
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.utils.PrefsManager
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.consultantvendor.utils.isConnectedToInternet
import com.consultantvendor.utils.longToast
import com.consultantvendor.utils.showSnackBar
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ForgotPasswordFragment : DaggerFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentForgotPasswordBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_forgot_password, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.ivNext.setOnClickListener {
            when {
                binding.etEmail.text.toString().isEmpty() -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_email))
                }
                !Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches() -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_correct_email))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    val hashMap = HashMap<String, Any>()
                    hashMap["email"] = binding.etEmail.text.toString()

                    viewModel.forgotPassword(hashMap)
                }
            }
        }
    }


    private fun bindObservers() {

        viewModel.forgotPassword.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().longToast(getString(R.string.sent_password))
                    requireActivity().supportFragmentManager.popBackStack()
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })
    }
}
