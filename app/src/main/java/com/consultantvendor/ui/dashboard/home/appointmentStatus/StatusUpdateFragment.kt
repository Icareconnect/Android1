package com.consultantvendor.ui.dashboard.home.appointmentStatus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.ApiKeys
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentAppointmentDetailsBinding
import com.consultantvendor.databinding.FragmentStatusUpdateBinding
import com.consultantvendor.ui.dashboard.home.AppointmentViewModel
import com.consultantvendor.ui.dashboard.home.appointmentStatus.AppointmentStatusActivity
import com.consultantvendor.ui.loginSignUp.document.add.DialogAddDocumentFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class StatusUpdateFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentStatusUpdateBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var request: Request


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status_update, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        progressDialog = ProgressDialog(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
        binding.clLoader.setBackgroundResource(R.color.colorWhite)
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.etStatus.setOnClickListener {
            val fragment = DialogStatusFragment(this)
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        }
    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["request_id"] = requireActivity().intent.getStringExtra(EXTRA_REQUEST_ID) ?: ""
            viewModel.requestDetail(hashMap)
        }
    }

    private fun setData() {

    }

    private fun bindObservers() {
        viewModel.requestDetail.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.setBackgroundResource(0)
                    binding.clLoader.gone()
                    request = it.data?.request_detail ?: Request()
                    setData()

                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })


        viewModel.startRequest.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()

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
