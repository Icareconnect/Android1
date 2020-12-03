package com.consultantvendor.ui.loginSignUp.loginemail

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
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
import com.consultantvendor.databinding.FragmentLoginEmailBinding
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.forgotpassword.ForgotPasswordFragment
import com.consultantvendor.ui.loginSignUp.login.LoginFragment
import com.consultantvendor.ui.loginSignUp.register.RegisterFragment
import com.consultantvendor.ui.loginSignUp.welcome.WelcomeFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import okhttp3.RequestBody
import javax.inject.Inject

class LoginEmailFragment : DaggerFragment() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentLoginEmailBinding

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
                DataBindingUtil.inflate(inflater, R.layout.fragment_login_email, container, false)
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

        if (arguments?.containsKey(WelcomeFragment.EXTRA_SIGNUP) == true) {
            binding.tvTitle.text = getString(R.string.sign_up_care_connect)

            binding.tvLoginScreen.gone()
            binding.tvLoginTitle.gone()
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.tvLoginScreen.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            replaceFragment(requireActivity().supportFragmentManager,
                    LoginFragment(), R.id.container)
        }

        binding.ivNext.setOnClickListener {
            when {
                binding.etEmail.text.toString().isEmpty() -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_email))
                }
                !Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches() -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_correct_email))
                }
                binding.etPassword.text.toString().length < 8 -> {
                    binding.etPassword.showSnackBar(getString(R.string.enter_password))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    if (arguments?.containsKey(WelcomeFragment.EXTRA_SIGNUP) == true) {
                        val hashMap = HashMap<String, RequestBody>()
                        hashMap["name"] = getRequestBody(DUMMY_NAME)
                        hashMap["email"] = getRequestBody(binding.etEmail.text.toString().trim())
                        hashMap["password"] = getRequestBody(binding.etPassword.text.toString().trim())
                        hashMap["user_type"] = getRequestBody(APP_TYPE)
                        viewModel.register(hashMap)
                    }else{
                        val hashMap = HashMap<String, Any>()
                        hashMap[ApiKeys.PROVIDER_TYPE] = ProviderType.email
                        hashMap["provider_id"] = binding.etEmail.text.toString()
                        hashMap[ApiKeys.PROVIDER_VERIFICATION] = binding.etPassword.text.toString()
                        hashMap[ApiKeys.USER_TYPE] = APP_TYPE
                        viewModel.login(hashMap)
                    }
                }
            }
        }

        binding.tvForgetPass.setOnClickListener {
            replaceFragment(
                requireActivity().supportFragmentManager,
                ForgotPasswordFragment(), R.id.container
            )
        }
    }


    private fun bindObservers() {
        viewModel.login.observe(requireActivity(), Observer {
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

        viewModel.register.observe(requireActivity(), Observer {
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
    }

    companion object{
        const val DUMMY_NAME="1#123@123"
    }
}
