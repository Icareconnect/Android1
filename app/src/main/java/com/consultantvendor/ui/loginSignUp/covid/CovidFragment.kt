package com.consultantvendor.ui.loginSignUp.covid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.SetFilter
import com.consultantvendor.data.models.responses.Filter
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentServiceBinding
import com.consultantvendor.ui.AppVersionViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.prefrence.PrefrenceAdapter
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment.Companion.FILTER_DATA
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import okhttp3.RequestBody
import javax.inject.Inject

class CovidFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentServiceBinding

    private var rootView: View? = null

    private lateinit var viewModel: LoginViewModel

    private lateinit var viewModelAppVersion: AppVersionViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<Filter>()

    private lateinit var adapter: PrefrenceAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    var prefrenceType = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
            hitApi()
        }
        return rootView
    }

    private fun initialise() {
        viewModelAppVersion = ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.tvNext.text = getString(R.string.save)
        prefrenceType = arguments?.getString(MASTER_PREFRENCE_TYPE)
                ?: PreferencesType.PERSONAL_INTEREST
        when (prefrenceType) {
            PreferencesType.PERSONAL_INTEREST -> {
                binding.tvTitle.text = getString(R.string.personal_interests)
            }
            PreferencesType.PROVIDABLE_SERVICES -> {
                binding.tvTitle.text = getString(R.string.providable_services)
            }
            PreferencesType.WORK_ENVIRONMENT -> {
                binding.tvTitle.text = getString(R.string.work_environment)
            }
            PreferencesType.COVID -> {
                binding.tvTitle.text = getString(R.string.covid_19)
            }
        }
    }

    private fun setAdapter() {
        adapter = PrefrenceAdapter(this, items)
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
            hitApi()
        }

        binding.rvListing.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvListing.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMoreItems && !isLastPage && lastVisibleItemPosition >= totalItemCount) {
                    isLoadingMoreItems = true
                    hitApi()
                }
            }
        })

        binding.tvNext.setOnClickListener {
            /*Check selected Filter*/
            val filterArray = ArrayList<SetFilter>()

            var setFilter: SetFilter

            items.forEachIndexed { index, filter ->
                setFilter = SetFilter()

                /*Set filter Id*/
                setFilter.preference_id = filter.id
                setFilter.option_ids = ArrayList()

                var selectedOption = false
                filter.options?.forEach {
                    if (it.isSelected) {
                        selectedOption = true

                        setFilter.option_ids?.add(it.id ?: "")
                    }
                }

                if (selectedOption) {
                    filterArray.add(setFilter)
                } else {
                    binding.toolbar.showSnackBar(filter.preference_name ?: "")
                    return@setOnClickListener
                }
            }

            val hashMap = HashMap<String, RequestBody>()
            hashMap["master_preferences"] = getRequestBody(Gson().toJson(filterArray))
            viewModel.updateProfile(hashMap)
        }
    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            if(prefrenceType==PreferencesType.PROVIDABLE_SERVICES){
                hashMap["filter_ids"] = requireActivity().intent.getStringExtra(FILTER_DATA)
                viewModelAppVersion.duty(hashMap)
            }else {
                hashMap["type"] = PreferencesType.ALL
                hashMap["preference_type"] = prefrenceType
                viewModelAppVersion.preferences(hashMap)
            }
        } else
            binding.swipeRefresh.isRefreshing = false
    }

    private fun bindObservers() {
        viewModelAppVersion.preferences.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.preferences ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                    }

                    items.addAll(tempList)
                    adapter.notifyDataSetChanged()

                    if (items.isNotEmpty())
                        binding.tvNext.visible()

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)
                    binding.clLoader.gone()

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                    binding.tvNext.gone()
                }
            }
        })

        viewModel.updateProfile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    val fragment: Fragment
                    val bundle = Bundle()

                    when (prefrenceType) {
                        PreferencesType.PERSONAL_INTEREST -> {
                            fragment = CovidFragment()
                            bundle.putString(MASTER_PREFRENCE_TYPE, PreferencesType.PROVIDABLE_SERVICES)
                        }
                        PreferencesType.PROVIDABLE_SERVICES -> {
                            fragment = CovidFragment()
                            bundle.putString(MASTER_PREFRENCE_TYPE, PreferencesType.WORK_ENVIRONMENT)
                        }
                        PreferencesType.WORK_ENVIRONMENT -> {
                            fragment = CovidFragment()
                            bundle.putString(MASTER_PREFRENCE_TYPE, PreferencesType.COVID)
                        }
                        PreferencesType.COVID -> {
                            fragment = DocumentsFragment()
                        }
                        else -> {
                            fragment = DocumentsFragment()
                        }
                    }

                    fragment.arguments = bundle
                    replaceFragment(requireActivity().supportFragmentManager,
                            fragment, R.id.container)

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


    fun clickItem(item: Filter?) {

    }

    companion object {
        const val MASTER_PREFRENCE_TYPE = "MASTER_PREFRENCE_TYPE"
    }
}
