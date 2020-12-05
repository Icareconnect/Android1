package com.consultantvendor.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.NonNull
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.ProviderType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentHomeBinding
import com.consultantvendor.ui.adapter.CommonFragmentPagerAdapter
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.SignUpActivity
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.google.android.material.navigation.NavigationView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.nav_header_home.view.*
import javax.inject.Inject


class HomeFragment : DaggerFragment(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var binding: FragmentHomeBinding

    private var rootView: View? = null

    private lateinit var adapter: CommonFragmentPagerAdapter

    private lateinit var viewModelLogin: LoginViewModel

    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
            rootView = binding.root

            initialise()
            listeners()
            handleHeader()
            bindObservers()
            hideMenuItem()
        }
        return rootView
    }


    private fun initialise() {
        binding.navView.itemIconTintList = null
        binding.navView.setNavigationItemSelectedListener(this)

        progressDialog = ProgressDialog(requireActivity())
        viewModelLogin = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        adapter = CommonFragmentPagerAdapter(requireActivity().supportFragmentManager)
        val titles = arrayOf(getString(R.string.all_Requests))

        titles.forEachIndexed { index, s ->

            adapter.addTab(titles[index], AppointmentFragment())
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1

        binding.tabLayout.setupWithViewPager(binding.viewPager)

    }

    private fun handleHeader() {
        val userData = userRepository.getUser()
        val headerView = binding.navView.getHeaderView(0)
// set User Name
        headerView.tvName.text = getDoctorName(userData)
        headerView.tvEmail.text = userData?.email ?: ""
        loadImage(headerView.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)

        headerView.ivPic.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(requireActivity(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.PROFILE))
        }

        headerView.ivCross.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun hideMenuItem() {
        val nav_Menu: Menu = binding.navView.menu
        nav_Menu.findItem(R.id.changePassword).isVisible = (userRepository.getUser()?.provider_type == ProviderType.email)
    }

    private fun listeners() {
        binding.ivDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.ivNotification.setOnClickListener {
            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.NOTIFICATION))
        }
    }

    override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.editProfile -> {
                startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                        .putExtra(UPDATE_PROFILE, true), AppRequestCode.PROFILE_UPDATE)
            }
            R.id.documents -> {
                startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                        .putExtra(SubCategoryFragment.CATEGORY_PARENT_ID, userRepository.getUser()?.categoryData)
                        .putExtra(DocumentsFragment.UPDATE_DOCUMENTS, true), AppRequestCode.PROFILE_UPDATE)
            }
            R.id.changePassword -> {
                startActivity(Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.CHANGE_PASSWORD))
            }

            R.id.myEarnings -> {
                startActivity(Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.REVENUE))
            }


            R.id.logout -> {
                showLogoutDialog()
            }
        }

        //close navigation drawer
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogoutDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(
                requireContext(), R.string.sign_out,
                R.string.logout_dialog_message, R.string.yes, R.string.no, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        viewModelLogin.logout()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun bindObservers() {
        viewModelLogin.logout.observe(requireActivity(), Observer {
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
    }

    override fun onResume() {
        super.onResume()
        handleHeader()
    }

}