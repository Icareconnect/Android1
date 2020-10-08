package com.consultantvendor.ui.dashboard.home.appointmentStatus

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentStatusUpdateBinding
import com.consultantvendor.ui.dashboard.home.AppointmentViewModel
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


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
            hitApi()
        }
        return rootView
    }

    private fun initialise() {
        progressDialog = ProgressDialog(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
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
        if (request.status == CallAction.COMPLETED) {
            binding.tvComplete.isChecked = true
            binding.viewComplete.alpha = 1f
            binding.tvStatusUpdate.gone()
            binding.etStatus.gone()
        }

    }

    fun hitApiStartRequest() {
        if (isConnectedToInternet(requireActivity(), true)) {
            val hashMap = HashMap<String, Any>()
            hashMap["request_id"] = request.id ?: ""
            hashMap["status"] = CallAction.COMPLETED

            viewModel.callStatus(hashMap)

        }
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


        viewModel.callStatus.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()

                    binding.clCompleted.visible()


                    Timer().schedule(3000) {
                        requireActivity().runOnUiThread {
                            binding.clCompleted.gone()
                        }
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

}
