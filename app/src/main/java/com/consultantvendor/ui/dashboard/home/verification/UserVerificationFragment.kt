package com.consultantvendor.ui.dashboard.home.verification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantvendor.R
import com.consultantvendor.data.network.PushType
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentUserVerificationBinding
import com.consultantvendor.ui.loginSignUp.SignUpActivity
import com.consultantvendor.ui.loginSignUp.welcome.WelcomeFragment.Companion.EXTRA_LOGIN
import com.consultantvendor.utils.EXTRA_IS_FIRST
import com.consultantvendor.utils.PrefsManager
import com.consultantvendor.utils.USER_DATA
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class UserVerificationFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentUserVerificationBinding

    private var rootView: View? = null

    private var isReceiverRegistered = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_verification, container, false)
            rootView = binding.root

            initialise()
        }
        return rootView
    }

    private fun initialise() {

        if (requireActivity().intent.hasExtra(EXTRA_IS_FIRST) &&
                requireActivity().intent.getBooleanExtra(EXTRA_IS_FIRST, false)) {

            Timer().schedule(4000) {
                prefsManager.remove(USER_DATA)
                ActivityCompat.finishAffinity(requireActivity())

                startActivity(Intent(requireActivity(), SignUpActivity::class.java)
                        .putExtra(EXTRA_LOGIN,true)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }


    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.PROFILE_APPROVED)
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshRequests, intentFilter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshRequests)
            isReceiverRegistered = false
        }
    }

    private val refreshRequests = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == PushType.PROFILE_APPROVED) {
                requireActivity().finish()
            }
        }
    }


}
