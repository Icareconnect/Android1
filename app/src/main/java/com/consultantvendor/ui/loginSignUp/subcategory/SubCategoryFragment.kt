package com.consultantvendor.ui.loginSignUp.subcategory

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
import com.consultantvendor.data.models.responses.Categories
import com.consultantvendor.data.network.ApiKeys.AFTER
import com.consultantvendor.data.network.ApiKeys.PER_PAGE
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentCategoryBinding
import com.consultantvendor.ui.drawermenu.classes.ClassesViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.prefrence.PrefrenceFragment
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment
import com.consultantvendor.utils.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SubCategoryFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentCategoryBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private lateinit var viewModelLogin: LoginViewModel

    private var items = ArrayList<Categories>()

    private lateinit var adapter: SubCategoryAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var categoryData: Categories? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category, container, false)
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

        binding.rvListing.layoutManager = LinearLayoutManager(requireContext())
        categoryData = arguments?.getSerializable(CATEGORY_PARENT_ID) as Categories

        binding.tvTitle.text = getString(R.string.select_sub_category)
    }

    private fun setAdapter() {
        adapter = SubCategoryAdapter(this, items)
        binding.rvListing.adapter = adapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
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

    private fun hitApi(firstHit: Boolean) {
        if (firstHit) {
            isFirstPage = true
            isLastPage = false
        }

        val hashMap = HashMap<String, String>()
        if (isConnectedToInternet(requireContext(), true)) {
            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()
            hashMap["parent_id"] = categoryData?.id ?: ""

            viewModel.categories(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.categories.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    isLoadingMoreItems = false

                    val tempList = it.data?.classes_category ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                    }

                    items.addAll(tempList)
                    adapter.notifyDataSetChanged()

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)
                    binding.clLoader.gone()

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })
    }


    fun clickItem(item: Categories?) {
        val fragment = when {
            item?.is_subcategory == true -> {
                SubCategoryFragment()
            }
            item?.is_additionals == true -> {
                DocumentsFragment()
            }
            item?.is_filters == true -> {
                PrefrenceFragment()
            }
            else -> {
                ServiceFragment()
            }
        }

        val bundle = Bundle()
        bundle.putSerializable(CATEGORY_PARENT_ID, item)
        fragment.arguments = bundle

        replaceFragment(requireActivity().supportFragmentManager,
                fragment, R.id.container)
    }

    companion object {
        const val CATEGORY_PARENT_ID = "CATEGORY_PARENT_ID"
        const val SERVICE_ID = "SERVICE_ID"
    }
}
