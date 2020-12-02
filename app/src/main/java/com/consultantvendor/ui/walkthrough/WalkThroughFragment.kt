package com.consultantvendor.ui.walkthrough

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.consultantvendor.R
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentWalkthroughBinding
import com.consultantvendor.ui.adapter.CommonFragmentPagerAdapter
import com.consultantvendor.utils.POSITION
import com.consultantvendor.utils.PrefsManager
import com.consultantvendor.utils.hideShowView
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class WalkThroughFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentWalkthroughBinding

    private var rootView: View? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_walkthrough, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setBanners()
        }
        return rootView
    }

    private fun initialise() {
        requireActivity().setResult(Activity.RESULT_OK)
    }

    private fun setBanners() {
        val adapter = CommonFragmentPagerAdapter(requireActivity().supportFragmentManager)
        for (i in 0..2) {
            val fragment = WalkthroughDetailFragment()
            val bundle = Bundle()
            bundle.putInt(POSITION, i)
            fragment.arguments = bundle
            adapter.addTab("", fragment)
        }

        binding.viewPager.adapter = adapter

        binding.pageIndicatorView.setViewPager(binding.viewPager)
    }

    private fun listeners() {
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val show = position == binding.viewPager.adapter?.count?.minus(1)

                binding.tvSkip.hideShowView(!show)
                binding.tvGetStarted.hideShowView(show)
            }

        })

        binding.tvGetStarted.setOnClickListener {
            prefsManager.save(WALKTHROUGH_SCREEN, true)
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    companion object {
        const val WALKTHROUGH_SCREEN = "WALKTHROUGH_SCREEN"
    }
}
