package com.consultantvendor.ui.dashboard.feeds

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Feed
import com.consultantvendor.data.network.ApiKeys.AFTER
import com.consultantvendor.data.network.ApiKeys.PER_PAGE
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityListingToolbarBinding
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class FeedsFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityListingToolbarBinding

    private var rootView: View? = null

    private lateinit var viewModel: FeedViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<Feed>()

    private lateinit var adapter: FeedsAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var typeOfBlog = BlogType.BLOG


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.activity_listing_toolbar, container, false)
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
        when (requireActivity().intent.getStringExtra(PAGE_TO_OPEN)) {
            DrawerActivity.BLOGS -> {
                typeOfBlog = BlogType.BLOG
                binding.tvTitle.text = getString(R.string.latest_blogs)
                binding.tvAdd.text = getString(R.string.post_blog_plus)
            }
            DrawerActivity.ARTICLE -> {
                typeOfBlog = BlogType.ARTICLE

                binding.tvTitle.text = getString(R.string.latest_articles)
                binding.tvAdd.text = getString(R.string.post_article_plus)
            }
            else -> {
                typeOfBlog = BlogType.ARTICLE
                binding.tvTitle.text = getString(R.string.latest_articles)
                binding.tvAdd.text = getString(R.string.post_article_plus)
            }
        }

        if (requireActivity().intent.hasExtra(LIST_TYPE)) {
            when (requireActivity().intent.getStringExtra(LIST_TYPE)) {
                FAVOURITE_LIST ->
                    binding.tvTitle.text = getString(R.string.favourites)
                OWN_LIST ->
                    binding.tvTitle.text = getString(R.string.my_articles)
            }
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[FeedViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
        binding.tvAdd.visible()
    }

    private fun setAdapter() {
        adapter = FeedsAdapter(this, items)
        binding.rvListing.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvListing.adapter = adapter
    }

    fun clickFavourite(item: Feed) {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = java.util.HashMap<String, String>()
            hashMap["favorite"] = if (item.is_favorite == true) "0" else "1"
            viewModel.addFavorite(item.id ?: "", hashMap)
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.tvAdd.setOnClickListener {
            replaceResultFragment(this, AddFeedFragment(), R.id.container, AppRequestCode.ARTICLE_CHANGES)
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
            hashMap["type"] = typeOfBlog
            if (requireActivity().intent.hasExtra(LIST_TYPE)) {
                when (requireActivity().intent.getStringExtra(LIST_TYPE)) {
                    FAVOURITE_LIST ->
                        hashMap["favorite"] = "1"
                    OWN_LIST ->
                        hashMap["consultant_id"] = userRepository.getUser()?.id ?:""
                }

            }

            viewModel.getFeeds(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.getFeeds.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.feeds ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                    }

                    items.addAll(tempList)
                    adapter.notifyDataSetChanged()

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.clNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)

                    binding.swipeRefresh.isRefreshing = false
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!isLoadingMoreItems && !binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                }
            }
        })

        viewModel.addFavorite.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    if (isConnectedToInternet(requireContext(), true)) {
                        hitApi(true)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ARTICLE_CHANGES) {
                hitApi(true)
            }
        }
    }

    companion object {
        const val LIST_TYPE = "LIST_TYPE"
        const val FAVOURITE_LIST = "FAVOURITE_LIST"
        const val OWN_LIST = "OWN_LIST"
    }
}
