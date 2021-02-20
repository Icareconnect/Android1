package com.consultantvendor.ui.dashboard.home

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
import com.consultantvendor.data.models.responses.Filter
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentAppointmentDetailsBinding
import com.consultantvendor.ui.dashboard.home.appointmentStatus.AppointmentStatusActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.collections.set


class AppointmentDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

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
            val address = request.extra_detail
            mapIntent(requireActivity(), address?.service_address ?: "", address?.lat?.toDouble()
                    ?: 0.0,
                    address?.long?.toDouble() ?: 0.0)
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

        /*Approval*/
        binding.tvUserApprovalT.gone()
        binding.tvUserApproval.gone()
        binding.tvUserApprovalComment.gone()
        binding.view1.gone()

        binding.tvName.text = request.from_user?.name
        loadImage(binding.ivPic, request.from_user?.profile_image,
                R.drawable.ic_profile_placeholder)

        /*Work Environment user*/
        val workExperience = ArrayList<Filter>()
        val covid = ArrayList<Filter>()
        request.from_user?.master_preferences?.forEach {
            when (it.preference_type) {
                PreferencesType.COVID ->
                    covid.add(it)
                PreferencesType.WORK_ENVIRONMENT ->
                    workExperience.add(it)
            }
        }

        if (workExperience.isNotEmpty()) {
            var workText = ""
            workExperience.forEach {
                it.options?.forEach {
                    if (it.isSelected) {
                        workText += it.option_name + ", "
                    }
                }
            }
            binding.tvWorkEnvironment.text = workText.removeSuffix(", ")
            binding.tvWorkEnvironment.hideShowView(workText.isNotEmpty())
        } else {
            binding.tvWorkEnvironment.gone()
        }

        if (covid.isNotEmpty()) {
            var covidText = ""
            covid.forEach {
                covidText += it.preference_name + "\n"

                it.options?.forEach {
                    if (it.isSelected) {
                        covidText += it.option_name + "\n\n"
                    }
                }
            }
            binding.tvCovidV.text = covidText

            binding.tvCovid.hideShowView(covidText.isNotEmpty())
            binding.tvCovidV.hideShowView(covidText.isNotEmpty())
        } else {
            binding.tvCovid.gone()
            binding.tvCovidV.gone()
        }


        binding.tvServiceTypeV.text = request.extra_detail?.filter_name ?: ""
        binding.tvServiceType.gone()
        binding.tvServiceTypeV.gone()

        binding.tvServiceForV.text = request.extra_detail?.service_for ?: ""
        binding.tvServiceName.text = request.extra_detail?.first_name ?: ""
        binding.tvDistanceV.text = request.extra_detail?.distance ?: ""
        binding.tvLocation.text = request.extra_detail?.service_address

        binding.tvBookingDateV.text = getDatesComma(request.extra_detail?.working_dates)
        binding.tvBookingTimeV.text = "${request.extra_detail?.start_time ?: ""} - ${request.extra_detail?.end_time ?: ""}"

        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        /*Hide now*/
        val specialInstruction = request.extra_detail?.reason_for_service
        binding.tvSpecialInstructionsV.text = specialInstruction
        binding.tvSpecialInstructions.hideShowView(specialInstruction?.isNotEmpty() == true)
        binding.tvSpecialInstructionsV.hideShowView(specialInstruction?.isNotEmpty() == true)


        var services = ""
        request.duties?.forEach {
            services += it.option_name + ", "
        }
        binding.tvServicesV.text = services.removeSuffix(", ")
        binding.tvServices.hideShowView(services.isNotEmpty())
        binding.tvServicesV.hideShowView(services.isNotEmpty())

        when (request.status) {
            CallAction.PENDING -> {
                binding.tvStatus.text = getString(R.string.new_request)
                binding.tvAccept.text = getString(R.string.accept)
            }
            CallAction.ACCEPT -> {
                binding.tvStatus.text = getString(R.string.accepted)
                binding.tvAccept.text = getString(R.string.start_request)
                binding.tvAccept.setBackgroundResource(R.drawable.drawable_theme_8)
                binding.tvCancel.gone()
            }
            CallAction.START -> {
                binding.tvStatus.text = getString(R.string.inprogess)
                binding.tvAccept.text = getString(R.string.track_status)
                binding.tvAccept.setBackgroundResource(R.drawable.drawable_theme_8)
                binding.tvCancel.gone()
//                binding.tvAccept.gone()
            }
            CallAction.REACHED -> {
                binding.tvStatus.text = getString(R.string.reached_destination)
                binding.tvAccept.text = getString(R.string.track_status)
                binding.tvAccept.setBackgroundResource(R.drawable.drawable_theme_8)
                binding.tvCancel.gone()
            }
            CallAction.START_SERVICE -> {
                binding.tvStatus.text = getString(R.string.started)
                binding.tvAccept.text = getString(R.string.track_status)
                binding.tvAccept.setBackgroundResource(R.drawable.drawable_theme_8)
                binding.tvCancel.gone()
            }

            CallAction.COMPLETED -> {
                binding.tvStatus.text = getString(R.string.done)
                binding.tvAccept.gone()
                binding.tvCancel.gone()

                when (request.user_status) {
                    CallAction.APPROVED, CallAction.DECLINED -> {
                        binding.tvUserApprovalT.visible()
                        binding.tvUserApproval.visible()
                        binding.tvUserApprovalComment.visible()
                        binding.view1.visible()

                        binding.tvUserApproval.text = request.user_status
                        binding.tvUserApprovalComment.text = request.user_comment

                        if (request.user_status == CallAction.APPROVED)
                            binding.tvUserApproval.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                        else if (request.user_status == CallAction.DECLINED)
                            binding.tvUserApproval.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNoShow))

                    }
                }
            }
            CallAction.FAILED -> {
                binding.tvAccept.gone()
                binding.tvStatus.text = getString(R.string.no_show)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNoShow))
                binding.tvCancel.gone()
            }
            CallAction.CANCELED -> {
                binding.tvStatus.text = if (request.canceled_by?.id == userRepository.getUser()?.id)
                    getString(R.string.declined)
                else getString(R.string.canceled)


                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNoShow))
                binding.tvAccept.gone()
                binding.tvCancel.gone()
            }
            CallAction.CANCEL_SERVICE -> {
                binding.tvStatus.text = getString(R.string.canceled_service)
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
        when (request.status) {
            CallAction.PENDING -> {
                showAcceptRequestDialog()
            }
            CallAction.ACCEPT -> {
                showInitiateRequestDialog()
            }
            CallAction.START, CallAction.REACHED -> {
                startActivityForResult(Intent(requireActivity(), AppointmentStatusActivity::class.java)
                        .putExtra(EXTRA_REQUEST_ID, request), AppRequestCode.APPOINTMENT_DETAILS)
            }
            CallAction.START_SERVICE -> {
                startActivityForResult(Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.UPDATE_SERVICE)
                        .putExtra(EXTRA_REQUEST_ID, request.id), AppRequestCode.APPOINTMENT_DETAILS)
            }
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
            hashMap["status"] = CallAction.START

            viewModel.callStatus(hashMap)

        }
    }

    private fun cancelAppointment() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(),
                R.string.cancel_appointment,
                R.string.cancel_appointment_msg,
                R.string.decline,
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
        viewModel.requestDetail.observe(requireActivity(), Observer {
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

        viewModel.acceptRequest.observe(requireActivity(), Observer {
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

        viewModel.callStatus.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()

                    request.status = CallAction.START
                    startActivityForResult(Intent(requireActivity(), AppointmentStatusActivity::class.java)
                            .putExtra(EXTRA_REQUEST_ID, request), AppRequestCode.APPOINTMENT_DETAILS)

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

        viewModel.cancelRequest.observe(requireActivity(), Observer {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.APPOINTMENT_DETAILS) {
                requireActivity().setResult(Activity.RESULT_OK)
                hitApi()
            }
        }
    }

}
