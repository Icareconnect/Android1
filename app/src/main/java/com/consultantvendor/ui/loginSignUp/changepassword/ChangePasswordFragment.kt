package com.consultantvendor.ui.loginSignUp.changepassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentChangePasswordBinding
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.utils.PrefsManager
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.consultantvendor.utils.isConnectedToInternet
import com.consultantvendor.utils.longToast
import com.consultantvendor.utils.showSnackBar
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ChangePasswordFragment : DaggerFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentChangePasswordBinding

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
                DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)
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
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.ivNext.setOnClickListener {
            when {
                binding.etOldPassword.text.toString().trim().isEmpty() -> {
                    binding.etOldPassword.showSnackBar(getString(R.string.old_password))
                }
                binding.etNewPassword.text.toString().trim().isEmpty() -> {
                    binding.etNewPassword.showSnackBar(getString(R.string.new_password))
                }
                binding.etConfirmPassword.text.toString().trim().isEmpty() -> {
                    binding.etConfirmPassword.showSnackBar(getString(R.string.confirm_password))
                }
                binding.etConfirmPassword.text.toString().trim() != binding.etNewPassword.text.toString().trim() -> {
                    binding.etConfirmPassword.showSnackBar(getString(R.string.confirm_password))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    val hashMap = HashMap<String, Any>()
                    hashMap["current_password"] = binding.etOldPassword.text.toString()
                    hashMap["new_password"] = binding.etNewPassword.text.toString()

                    viewModel.changePassword(hashMap)
                }
            }
        }
    }


    private fun bindObservers() {
        viewModel.changePassword.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().longToast(getString(R.string.password_changed))
                    requireActivity().finish()
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
