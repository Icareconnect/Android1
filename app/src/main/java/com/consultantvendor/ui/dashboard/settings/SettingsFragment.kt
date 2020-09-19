package com.consultantvendor.ui.dashboard.settings

import android.app.Activity
import android.content.Intent
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
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentSettingsBinding
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity.Companion.CLASSES
import com.consultantvendor.ui.drawermenu.DrawerActivity.Companion.NOTIFICATION
import com.consultantvendor.ui.drawermenu.DrawerActivity.Companion.PROFILE
import com.consultantvendor.ui.drawermenu.DrawerActivity.Companion.USER_CHAT
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SettingsFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSettingsBinding

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
                    DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            setUserProfile()

            if (isConnectedToInternet(requireContext(), true))
                viewModel.getPages()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun setUserProfile() {
        val userData = userRepository.getUser()

        binding.tvName.text = getDoctorName(userData)
        binding.tvAge.text =
                "${getString(R.string.age)} ${getAge(userData?.profile?.dob)}"
        loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)

        binding.tvVersion.text =
                getString(R.string.version, getVersion(requireActivity()).versionName)
    }

    private fun listeners() {
        binding.ivPic.setOnClickListener {
            goToProfile()
        }

        binding.tvName.setOnClickListener {
            goToProfile()
        }

        binding.tvChat.setOnClickListener {
            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                            .putExtra(PAGE_TO_OPEN, USER_CHAT))
        }

        binding.tvBlogs.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.BLOGS), AppRequestCode.ARTICLE_CHANGES)
        }

        binding.tvArticle.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.ARTICLE), AppRequestCode.ARTICLE_CHANGES)
        }

        binding.tvNotification.setOnClickListener {
            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                            .putExtra(PAGE_TO_OPEN, NOTIFICATION))
        }

        binding.tvClasses.setOnClickListener {
            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                            .putExtra(PAGE_TO_OPEN, CLASSES))
        }

        binding.tvLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.tvInvite.setOnClickListener {
            shareDeepLink(DeepLink.INVITE, requireActivity(),userRepository)
        }
    }

    private fun showLogoutDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(
                requireContext(), R.string.sign_out,
                R.string.logout_dialog_message, R.string.yes, R.string.no, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        viewModel.logout()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun bindObservers() {
        viewModel.logout.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    logoutUser(requireActivity(), prefsManager)
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

        viewModel.pagesLink.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    val adapter = PagesAdapter(it.data?.pages ?: emptyList())
                    binding.rvPages.adapter = adapter
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

    private fun goToProfile() {
        startActivityForResult(
                Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, PROFILE), AppRequestCode.PROFILE_UPDATE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            setUserProfile()
        }
    }

}