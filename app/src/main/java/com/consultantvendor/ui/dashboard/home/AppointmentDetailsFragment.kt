package com.consultantvendor.ui.dashboard.home

import android.app.Activity
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
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class AppointmentDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentAppointmentDetailsBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var request: Request


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_appointment_details, container, false)
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
        binding.clLoader.setBackgroundResource(R.color.colorWhite)
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.tvAccept.setOnClickListener {
            proceedRequest()
        }

        binding.tvCancel.setOnClickListener {
            cancelAppointment()
        }

        binding.tvViewMap.setOnClickListener {
            val address=request.extra_detail
            mapIntent(requireActivity(),address?.service_address ?:"",address?.lat?.toDouble() ?:0.0,
                    address?.long?.toDouble() ?:0.0)
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
        binding.tvAccept.visible()
        binding.tvCancel.hideShowView(request.canCancel)

        binding.tvName.text = request.from_user?.name
        binding.tvServiceTypeV.text = request.extra_detail?.filter_name ?:""
        binding.tvLocation.text = request.extra_detail?.service_address
        loadImage(binding.ivPic, request.from_user?.profile_image,
                R.drawable.ic_profile_placeholder)

        binding.tvBookingDateV.text = getDatesComma(request.extra_detail?.working_dates)
        binding.tvBookingTimeV.text = "${request.extra_detail?.start_time ?:""} - ${request.extra_detail?.end_time?:""}"

        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        binding.tvSpecialInstructionsV.text=request.extra_detail?.reason_for_service

        when (request.status) {
            CallAction.PENDING -> {
                binding.tvStatus.text = getString(R.string.new_request)
                binding.tvAccept.text = getString(R.string.accept)
            }
            CallAction.ACCEPT -> {
                binding.tvStatus.text = getString(R.string.accepted)
                binding.tvAccept.text = getString(R.string.start_request)
                binding.tvCancel.gone()
            }
            CallAction.INPROGRESS -> {
                binding.tvStatus.text = getString(R.string.inprogess)
                binding.tvAccept.text = getString(R.string.check_request)
                binding.tvCancel.gone()
                binding.tvAccept.gone()
            }

            CallAction.COMPLETED -> {
                binding.tvStatus.text = getString(R.string.done)
                binding.tvAccept.gone()
                binding.tvCancel.gone()
            }
            CallAction.FAILED -> {
                binding.tvAccept.gone()
                binding.tvStatus.text = getString(R.string.no_show)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNoShow))
                binding.tvCancel.gone()
            }
            CallAction.CANCELED -> {
                binding.tvStatus.text = getString(R.string.canceled)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNoShow))
                binding.tvAccept.gone()
                binding.tvCancel.gone()
            }
            else -> {
                binding.tvStatus.text = getString(R.string.new_request)
            }
        }
    }

    private fun proceedRequest() {
        if (request.status == CallAction.PENDING) {
            showAcceptRequestDialog()
        } else if (request.status == CallAction.ACCEPT) {
            showInitiateRequestDialog()
        }
    }

    private fun showAcceptRequestDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.accept_request,
                R.string.accept_request_message, R.string.accept_request, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        hitApiAcceptRequest()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }


    private fun showInitiateRequestDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.start_request,
                R.string.start_request_message, R.string.start_request, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        hitApiStartRequest()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun hitApiAcceptRequest() {
        if (isConnectedToInternet(requireActivity(), true)) {
            val hashMap = HashMap<String, Any>()
            hashMap["request_id"] = request.id ?: ""

            viewModel.acceptRequest(hashMap)
        }
    }

    private fun hitApiStartRequest() {
        if (isConnectedToInternet(requireActivity(), true)) {
            val hashMap = HashMap<String, Any>()
            hashMap["request_id"] = request.id ?: ""

            viewModel.startRequest(hashMap)

        }
    }

    private fun cancelAppointment() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(),
                R.string.cancel_appointment,
                R.string.cancel_appointment_msg,
                R.string.cancel_appointment,
                R.string.cancel,
                false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        if (isConnectedToInternet(requireContext(), true)) {
                            val hashMap = HashMap<String, String>()
                            hashMap["request_id"] = request.id ?: ""
                            viewModel.cancelRequest(hashMap)
                        }
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
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

        viewModel.acceptRequest.observe(this, Observer {
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

        viewModel.cancelRequest.observe(this, Observer {
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
