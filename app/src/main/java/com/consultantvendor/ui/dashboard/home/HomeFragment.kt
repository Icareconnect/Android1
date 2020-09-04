package com.consultantvendor.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.consultantvendor.R
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentHomeBinding
import com.consultantvendor.ui.adapter.CommonFragmentPagerAdapter
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity.Companion.PROFILE
import com.consultantvendor.utils.*
import dagger.android.support.DaggerFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class HomeFragment : DaggerFragment(), OnDateSelected {

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentHomeBinding

    private var rootView: View? = null

    private lateinit var adapter: CommonFragmentPagerAdapter

    var selectedDate = ""

    var calendar: Calendar? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setUserData()
        }
        return rootView
    }

    private fun setUserData() {
        val userData = userRepository.getUser()

        //loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)
    }


    private fun initialise() {
        /*Get today date*/
       /* calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat(DateFormat.MON_YEAR_FORMAT)
        selectedDate = sdf.format(calendar?.time)
        binding.tvDate.text = selectedDate*/


        adapter = CommonFragmentPagerAdapter(requireActivity().supportFragmentManager)
        val titles = arrayOf(getString(R.string.all_Requests))

        titles.forEachIndexed { index, s ->

            adapter.addTab(titles[index],  AppointmentFragment(this))
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1

        binding.tabLayout.setupWithViewPager(binding.viewPager)

    }

    private fun listeners() {

       /* binding.tvDate.setOnClickListener {
            DateUtils.openDatePicker(requireActivity(), this, false, false)
        }*/

       /* binding.ivPic.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, PROFILE), AppRequestCode.PROFILE_UPDATE)
        }*/
    }

    override fun onDateSelected(date: String) {
        binding.tvDate.text = DateUtils.dateFormatChange(DateFormat.DATE_FORMAT_SLASH,
                DateFormat.MON_YEAR_FORMAT, date)

        selectedDate = binding.tvDate.text.toString()

        /*Refresh pages*/
        (adapter.fragments[0] as AppointmentFragment).hitApiDate()
    }
}