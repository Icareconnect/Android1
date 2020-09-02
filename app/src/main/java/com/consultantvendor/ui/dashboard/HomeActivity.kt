package com.consultantvendor.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.consultantvendor.R
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityHomeBinding
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.google.firebase.iid.FirebaseInstanceId
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class HomeActivity : DaggerAppCompatActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var appSocket: AppSocket

    lateinit var binding: ActivityHomeBinding

    private var currentNavController: LiveData<NavController>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        initialise()

        setNavigation()
        listeners()
    }

    private fun setNavigation() {
        val navGraphIds = listOf(
                R.navigation.navigation_home,
                R.navigation.navigation_wallet,
                R.navigation.navigation_revenue,
                R.navigation.navigation_profile)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = binding.bottomNav.setupWithNavController(
                navGraphIds = navGraphIds,
                fragmentManager = supportFragmentManager,
                containerId = R.id.nav_host_fragment,
                intent = intent
        )

        currentNavController = controller

        if (intent.hasExtra(EXTRA_TAB)) {
            if (intent.getStringExtra(EXTRA_TAB) == "1") {
                binding.bottomNav.selectedItemId = R.id.navigation_wallet
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    private fun initialise() {
        appSocket.init()

        Log.d("AccessToken", userRepository.getUser()?.token ?: "")

        /* Fetch Notification Token */

        userRepository.pushTokenUpdate()

        if (userRepository.getUser()?.isApproved == true) {
            startActivity(Intent(this, DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.USER_VERIFICATION))
        }
    }


    private fun listeners() {
    }


}
