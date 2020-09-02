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
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Feed
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentFeedDetailsBinding
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.google.android.material.appbar.AppBarLayout
import dagger.android.support.DaggerFragment
import javax.inject.Inject
import kotlin.math.abs


class FeedDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentFeedDetailsBinding

    private var rootView: View? = null

    private var details: Feed? = null

    private lateinit var viewModel: FeedViewModel

    private lateinit var progressDialog: ProgressDialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed_details, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[FeedViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
        binding.clLoader.setBackgroundResource(R.color.colorWhite)

        details = arguments?.getSerializable(EXTRA_REQUEST_ID) as Feed
        setData()

        if (isConnectedToInternet(requireContext(), true)) {
            viewModel.viewFeeds(details?.id ?: "")
        }
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            binding.toolbar.title = ""

            when {
                abs(verticalOffset) == appBarLayout.totalScrollRange -> {
                    // Collapsed
                    binding.toolbar.title = getDoctorName(details?.user_data)
                    binding.tvName.invisible()
                }
                verticalOffset == 0 -> {
                    // Expanded
                    binding.tvName.visible()
                }
                else -> {
                    // Somewhere in between
                    binding.tvName.visible()
                }
            }
        })

        binding.ivFavourite.setOnClickListener {
            if (isConnectedToInternet(requireContext(), true)) {
                val hashMap = HashMap<String, String>()
                hashMap["favorite"] = if (details?.is_favorite == true) "0" else "1"
                viewModel.addFavorite(details?.id ?: "", hashMap)
            }
        }
    }

    private fun setData() {
        binding.tvName.text = getDoctorName(details?.user_data)
        binding.tvDate.text = DateUtils.dateTimeFormatFromUTC(DateFormat.DATE_FORMAT_SLASH_YEAR, details?.created_at)

        binding.toolbarLayout.isTitleEnabled = false
        loadImage(binding.ivImage, details?.image, R.drawable.image_placeholder)
        binding.tvTitle.text = details?.title
        binding.tvDec.text = details?.description

        if (details?.is_favorite == true)
            binding.ivFavourite.setImageResource(R.drawable.ic_like_red)
        else
            binding.ivFavourite.setImageResource(R.drawable.ic_like_white)
    }

    private fun bindObservers() {
        viewModel.viewFeeds.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.clLoader.setBackgroundResource(0)

                    details = it.data?.feed
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

        viewModel.addFavorite.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    if (isConnectedToInternet(requireContext(), true)) {
                        viewModel.viewFeeds(details?.id ?: "")
                    }


                    if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                        resultFragmentIntentNoPop(this, targetFragment ?: this,
                                AppRequestCode.ARTICLE_CHANGES, Intent())
                    else
                        requireActivity().setResult(Activity.RESULT_OK)
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
