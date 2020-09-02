package com.consultantvendor.ui.loginSignUp.service

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.*
import com.consultantvendor.data.models.responses.Categories
import com.consultantvendor.data.models.responses.Service
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentServiceBinding
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.drawermenu.classes.ClassesViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.availability.SetAvailabilityFragment
import com.consultantvendor.ui.loginSignUp.availability.SetAvailabilityFragment.Companion.WORKING_TIME
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.SERVICE_ID
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ServiceFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentServiceBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private lateinit var viewModelLogin: LoginViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<Service>()

    private lateinit var adapter: ServiceAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var categoryData: Categories? = null

    private var availabilityPos = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service, container, false)
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
        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        viewModelLogin = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.tvTitle.text = getString(R.string.service_type)
        categoryData = arguments?.getSerializable(CATEGORY_PARENT_ID) as Categories
    }

    private fun setAdapter() {
        adapter = ServiceAdapter(this, items)
        binding.rvListing.adapter = adapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
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

        binding.tvNext.setOnClickListener {
            val updateServices = UpdateServices()
            updateServices.category_services_type = ArrayList()

            var serviceSelected = false
            items.forEach {
                if (it.isSelected) {
                    serviceSelected = true

                    /*Set Services*/
                    val setService = SetService()
                    setService.id = it.id
                    setService.available = "1"
                    setService.isAvailabilityChanged = it.isAvailabilityChanged
                    if (it.price_type == PriceType.PRICE_RANGE) {
                        if (it.price.isNullOrEmpty()) {
                            binding.tvNext.showSnackBar(getString(R.string.add_price_for, it.name))
                            return@setOnClickListener
                        } else {
                            val price = it.price?.toInt()
                            if (price ?: 0 < it.price_minimum ?: 0 || price ?: 0 > it.price_maximum ?: 0) {
                                binding.tvNext.showSnackBar(
                                    getString(
                                        R.string.add_price__in_s, it.price_minimum.toString(),
                                        it.price_maximum.toString(), it.name
                                    )
                                )
                                return@setOnClickListener
                            }
                        }

                        /*Set price*/
                        setService.price = it.price?.toInt()
                    } else {
                        setService.price = it.price_fixed
                    }

                    if (it.need_availability == "1" && !it.isAvailabilityLocal) {
                        binding.tvNext.showSnackBar(getString(R.string.add_availability_s, it.name))
                        return@setOnClickListener
                    }

                    /*Set Availability*/
//                    convert time to 24 hours format
                    val setAvailability = SetAvailability()
                    setAvailability.applyoption = it.setAvailability?.applyoption
                    setAvailability.days = it.setAvailability?.days
                    setAvailability.date = it.setAvailability?.date
                    setAvailability.slots = ArrayList()

                    it.setAvailability?.slots?.forEach {
                        val interval = Interval()
                        interval.start_time = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                            DateFormat.TIME_FORMAT_24, it.start_time ?: "")
                        interval.end_time = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                            DateFormat.TIME_FORMAT_24, it.end_time ?: "")
                        setAvailability.slots?.add(interval)
                    }

                    setService.availability = setAvailability
                    updateServices.category_services_type?.add(setService)
                } else {
                    val setService = SetService()
                    setService.id = it.id
                    setService.available = "0"

                    if (it.price_type == PriceType.PRICE_RANGE) {
                        setService.price = if(it.price.isNullOrEmpty()) 0 else it.price?.toInt()
                    } else {
                        setService.price = it.price_fixed
                    }

                    updateServices.category_services_type?.add(setService)
                }
            }

            /*Set final data*/
            if (serviceSelected) {
                updateServices.category_id = categoryData?.id
                if (arguments?.containsKey(FILTER_DATA) == true) {
                    updateServices.filters = ArrayList()
                    updateServices.filters?.addAll(arguments?.getSerializable(FILTER_DATA) as ArrayList<SetFilter>)
                }

                if (isConnectedToInternet(requireContext(), true)) {
                    viewModelLogin.updateServices(updateServices)
                }

            } else {
                binding.tvNext.showSnackBar(getString(R.string.select_service))
                return@setOnClickListener
            }
        }
    }

    private fun hitApi(firstHit: Boolean) {
        if (firstHit) {
            isFirstPage = true
            isLastPage = false
        }

        val hashMap = HashMap<String, String>()
        if (isConnectedToInternet(requireContext(), true)) {

            hashMap["category_id"] = categoryData?.id ?: ""

            viewModel.services(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.services.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false
                    isLoadingMoreItems = false

                    val tempList = it.data?.services ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                    }

                    items.addAll(tempList)

                    /*If Update*/
                    if (requireActivity().intent.hasExtra(UPDATE_AVAILABILITY)) {
                        val selectedService = userRepository.getUser()?.services
                        items.forEachIndexed { index, service ->
                            selectedService?.forEachIndexed { indexSelected, serviceSelected ->
                                if (service.service_id == serviceSelected.service_id) {
                                    items[index].price = serviceSelected.price
                                    if (serviceSelected.available == "1") {
                                        items[index].isSelected = true
                                        items[index].isAvailabilityLocal = true
                                        return@forEachIndexed
                                    }
                                }
                            }
                        }
                    }

                    adapter.notifyDataSetChanged()

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                }
            }
        })

        viewModelLogin.updateServices.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    if (requireActivity().intent.hasExtra(UPDATE_AVAILABILITY) ||
                        requireActivity().intent.hasExtra(UPDATE_CATEGORY)) {
                        requireActivity().setResult(Activity.RESULT_OK)
                        requireActivity().finish()
                    } else {
                        startActivity(Intent(requireContext(), HomeActivity::class.java))
                        requireActivity().finish()
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

    fun clickAddAvailability(position: Int) {
        availabilityPos = position
        val fragment = SetAvailabilityFragment()
        val bundle = Bundle()
        bundle.putSerializable(WORKING_TIME, items[position].setAvailability)
        bundle.putString(SERVICE_ID, items[position].service_id)
        bundle.putString(CATEGORY_PARENT_ID, categoryData?.id)
        fragment.arguments = bundle

        replaceResultFragment(this, fragment, R.id.container, AppRequestCode.ADD_AVAILABILITY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ADD_AVAILABILITY) {
                val setAvailability = data?.getSerializableExtra(WORKING_TIME) as SetAvailability
                items[availabilityPos].setAvailability = setAvailability
                items[availabilityPos].isAvailabilityChanged = true
                items[availabilityPos].isAvailabilityLocal = true
                adapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val FILTER_DATA = "FILTER_DATA"
    }
}
