package com.consultantvendor.ui.loginSignUp.verifyotp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.network.ApiKeys
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.ProviderType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentVerifyOtpBinding
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.category.CategoryFragment
import com.consultantvendor.ui.loginSignUp.insurance.InsuranceFragment
import com.consultantvendor.ui.loginSignUp.register.RegisterFragment
import com.consultantvendor.ui.loginSignUp.signup.SignUpFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class VerifyOTPFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository


    private lateinit var binding: FragmentVerifyOtpBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    private var phoneNumber = ""


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_verify_otp, container, false)
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

        phoneNumber = arguments?.getString(COUNTRY_CODE).toString() + arguments?.getString(PHONE_NUMBER).toString()
        binding.tvMsg.text = getString(R.string.we_sent_you_a_code, phoneNumber)
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.ivNext.setOnClickListener {
            when {
                binding.pvOtp.text.toString().length < 4 -> {
                    binding.pvOtp.showSnackBar(getString(R.string.enter_otp))
                }
                binding.pvOtp.text.toString().length == 4 -> {
                    if (isConnectedToInternet(requireContext(), true)) {

                        val hashMap = HashMap<String, Any>()
                        hashMap["country_code"] = arguments?.getString(COUNTRY_CODE).toString()

                        if (arguments?.containsKey(UPDATE_NUMBER) == true) {
                            hashMap["phone"] = arguments?.getString(PHONE_NUMBER).toString()
                            hashMap["otp"] = binding.pvOtp.text.toString()
                            viewModel.updateNumber(hashMap)
                        } else {
                            hashMap["provider_id"] = arguments?.getString(PHONE_NUMBER).toString()
                            hashMap[ApiKeys.PROVIDER_TYPE] = ProviderType.phone
                            hashMap[ApiKeys.PROVIDER_VERIFICATION] = binding.pvOtp.text.toString()
                            hashMap[ApiKeys.USER_TYPE] = APP_TYPE

                            viewModel.login(hashMap)
                        }
                    }
                }
            }
        }

        binding.tvResentOTP.setOnClickListener {
            val hashMap = HashMap<String, Any>()
            hashMap["country_code"] = arguments?.getString(COUNTRY_CODE).toString()
            hashMap["phone"] = arguments?.getString(PHONE_NUMBER).toString()

            viewModel.sendSms(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.login.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    if (userRepository.isUserLoggedIn()) {
                        startActivity(Intent(requireActivity(), HomeActivity::class.java))
                        requireActivity().finish()

                    } else {
                        val fragment = RegisterFragment()
                        val bundle = Bundle()
                        bundle.putBoolean(UPDATE_NUMBER, true)
                        fragment.arguments = bundle

                        replaceFragment(requireActivity().supportFragmentManager,
                                fragment, R.id.container)
                    }

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

        viewModel.sendSMS.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireContext().longToast(getString(R.string.code_sent_to, phoneNumber))

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

        viewModel.updateNumber.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    val appSetting = userRepository.getAppSetting()
                    if (appSetting?.insurance == true || appSetting?.clientFeaturesKeys?.isAddress == true) {
                        val fragment = InsuranceFragment()
                        val bundle = Bundle()
                        if (arguments?.containsKey(UPDATE_PROFILE) == true)
                            bundle.putBoolean(UPDATE_PROFILE, true)
                        fragment.arguments = bundle

                        replaceFragment(requireActivity().supportFragmentManager,
                                fragment, R.id.container)
                    } else
                        replaceFragment(requireActivity().supportFragmentManager,
                                CategoryFragment(), R.id.container)

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
