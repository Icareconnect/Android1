package com.consultantvendor.ui.dashboard.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.ApiKeys.AFTER
import com.consultantvendor.data.network.ApiKeys.PER_PAGE
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.PushType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentAppointmentBinding
import com.consultantvendor.ui.dashboard.home.appointmentStatus.AppointmentStatusActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.item_no_data.view.*
import javax.inject.Inject

class AppointmentFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentAppointmentBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AppointmentViewModel

    private var items = ArrayList<Request>()

    private lateinit var adapter: AppointmentAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var requestItem: Request? = null

    private var isReceiverRegistered = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_appointment, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
            hitApi(true)
        }
        return rootView
    }


    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun setAdapter() {
        adapter = AppointmentAdapter(this, items)
        binding.rvListing.adapter = adapter
        binding.rvListing.itemAnimator = null
    }

    private fun listeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            hitApi(true)
        }

        binding.rvListing.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvListing.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMoreItems && !isLastPage && lastVisibleItemPosition >= totalItemCount) {
                    isLoadingMoreItems = true
                    hitApi(false)
                }
            }
        })
    }


    fun hitApiDate() {
        hitApi(true)
    }

    private fun hitApi(firstHit: Boolean) {
        if (isConnectedToInternet(requireContext(), true)) {
            if (firstHit) {
                isFirstPage = true
                isLastPage = false
            }
            val hashMap = HashMap<String, String>()
            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()

            hashMap["service_type"] = arguments?.getString(POSITION) ?: CallType.ALL
            viewModel.request(hashMap)
        } else
            binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun bindObservers() {
        viewModel.pendingRequest.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.clLoader.gone()

                    isLoadingMoreItems = false

                    val tempList = it.data?.requests ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                        items.addAll(tempList)

                        adapter.notifyDataSetChanged()
                    } else {
                        val oldSize = items.size
                        items.addAll(tempList)

                        adapter.notifyItemRangeInserted(oldSize, items.size)
                    }

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.clNoData.hideShowView(items.isEmpty())

                    if (it.data?.isAprroved == false) {
                        binding.clNoData.setBackgroundResource(R.color.colorWhite)
                        binding.clNoData.hideShowView(true)
                        binding.clNoData.ivNoData.setImageResource(R.drawable.ic_verification)
                        binding.clNoData.tvNoData.text = getString(R.string.verification_may_take_some_time)
                        binding.clNoData.tvNoDataDesc.text = getString(R.string.thank_you_for_your_interest)
                    } else {
                        binding.clNoData.ivNoData.setImageResource(R.drawable.ic_no_request)
                        binding.clNoData.tvNoData.text = getString(R.string.no_requests)
                        binding.clNoData.tvNoDataDesc.text = getString(R.string.no_requests_desc)
                    }

                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)

                    binding.clLoader.gone()
                    binding.swipeRefreshLayout.isRefreshing = false
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!binding.swipeRefreshLayout.isRefreshing && !isLoadingMoreItems)
                        binding.clLoader.visible()
                }
            }
        })

        viewModel.acceptRequest.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    hitApi(true)
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
                    hitApi(true)

                    requestItem?.status = CallAction.START
                    startActivityForResult(Intent(requireActivity(), AppointmentStatusActivity::class.java)
                            .putExtra(EXTRA_REQUEST_ID, requestItem), AppRequestCode.APPOINTMENT_DETAILS)
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

                    hitApi(true)
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


    fun proceedRequest(request: Request) {
        requestItem = request

        when (request.status) {
            CallAction.PENDING -> {
                showAcceptRequestDialog()
            }
            CallAction.ACCEPT -> {
                showInitiateRequestDialog()
            }
            CallAction.START, CallAction.REACHED -> {
//                requestItem?.status = CallAction.REACHED

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

    fun viewDetails(request: Request) {
        startActivityForResult(Intent(requireContext(), DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, DrawerActivity.APPOINTMENT_DETAILS)
                .putExtra(EXTRA_REQUEST_ID, request.id), AppRequestCode.APPOINTMENT_DETAILS)
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
            hashMap["request_id"] = requestItem?.id ?: ""

            viewModel.acceptRequest(hashMap)
        }
    }

    private fun hitApiStartRequest() {
        if (isConnectedToInternet(requireActivity(), true)) {
            val hashMap = HashMap<String, Any>()
            hashMap["request_id"] = requestItem?.id ?: ""
            hashMap["status"] = CallAction.START

            viewModel.callStatus(hashMap)

        }
    }

    fun cancelAppointment(item: Request) {
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
                            hashMap["request_id"] = item.id ?: ""
                            viewModel.cancelRequest(hashMap)
                        }
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }


    companion object {
        const val EXTRA_NUMBER = "extra number"
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.REQUEST_COMPLETED)
            intentFilter.addAction(PushType.NEW_REQUEST)
            intentFilter.addAction(PushType.CANCELED_REQUEST)
            intentFilter.addAction(PushType.REQUEST_FAILED)
            intentFilter.addAction(PushType.RESCHEDULED_REQUEST)
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
            when (intent.action) {
                PushType.REQUEST_COMPLETED, PushType.NEW_REQUEST, PushType.CANCELED_REQUEST, PushType.REQUEST_FAILED,
                PushType.RESCHEDULED_REQUEST, PushType.PROFILE_APPROVED -> {
                    hitApi(true)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.APPOINTMENT_DETAILS) {
                hitApi(true)
            }
        }
    }
}