package com.consultantvendor.ui.loginSignUp.availability

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
import com.consultantvendor.data.models.requests.DatesAvailability
import com.consultantvendor.data.models.requests.Interval
import com.consultantvendor.data.models.requests.SetAvailability
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentSetAvailabilityBinding
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.SERVICE_ID
import com.consultantvendor.utils.*
import com.consultantvendor.utils.DateUtils.dateFormatFromMillis
import dagger.android.support.DaggerFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SetAvailabilityFragment : DaggerFragment(), OnTimeSelected {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSetAvailabilityBinding

    private var rootView: View? = null

    private lateinit var viewModel: GetSlotsViewModel

    private var itemsInterval = ArrayList<Interval>()

    private var itemWeekDays = ArrayList<Boolean>()

    private var itemDays = ArrayList<DatesAvailability>()

    private lateinit var adapter: IntervalAdapter

    private lateinit var weekDaysAdapter: WeekDaysAdapter

    private lateinit var datesAdapter: DatesAdapter

    private var timePositionSelected = 0

    private var setAvailability = SetAvailability()

    private var dateSelected = DatesAvailability()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.fragment_set_availability,
                    container,
                    false
            )
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()

            if (requireActivity().intent.hasExtra(UPDATE_CATEGORY) ||
                    requireActivity().intent.hasExtra(UPDATE_AVAILABILITY)) {
                setDatesAdapter()
                binding.tvWeekDays.text = getString(R.string.select_date)
            } else
                setWeekAdapter()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GetSlotsViewModel::class.java]

        if (arguments?.getSerializable(WORKING_TIME) != null)
            setAvailability = arguments?.getSerializable(WORKING_TIME) as SetAvailability
    }

    private fun setWeekAdapter() {
        itemWeekDays.clear()

        if (setAvailability.days == null) {
            itemWeekDays.add(false)
            itemWeekDays.add(false)
            itemWeekDays.add(false)
            itemWeekDays.add(false)
            itemWeekDays.add(false)
            itemWeekDays.add(false)
            itemWeekDays.add(false)
        } else {
            itemWeekDays.addAll(setAvailability.days ?: emptyList())
            checkWeekDaySelected()
        }

        weekDaysAdapter = WeekDaysAdapter(this, itemWeekDays)
        binding.rvWeek.adapter = weekDaysAdapter
    }

    private fun setDatesAdapter() {
        itemDays.clear()
        var calendar: Calendar
        var date: DatesAvailability
        for (i in 0..100) {
            calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, i)

            date = DatesAvailability()
            date.displayName =
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            date.date = calendar.timeInMillis
            itemDays.add(date)
        }

        datesAdapter = DatesAdapter(this, itemDays)
        binding.rvWeek.adapter = datesAdapter
    }

    private fun setAdapter() {
        itemsInterval.clear()
        if (setAvailability.slots == null) {
            itemsInterval.add(Interval())
        } else {
            itemsInterval.addAll(setAvailability.slots ?: emptyList())
        }
        adapter = IntervalAdapter(this, itemsInterval)
        binding.rvListing.adapter = adapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.tvNext.setOnClickListener {
            val setAvailability = SetAvailability()

            var daysSelected = false
            itemWeekDays.forEach {
                if (it) {
                    daysSelected = true
                    return@forEach
                }
            }

            if (daysSelected) {
                setAvailability.days = ArrayList()
                setAvailability.days?.addAll(itemWeekDays)
            } else {
                binding.tvNext.showSnackBar(getString(R.string.select_working_days))
                return@setOnClickListener
            }

            if (itemsInterval[itemsInterval.size - 1].start_time == null) {
                binding.tvNext.showSnackBar(getString(R.string.select_time))
                return@setOnClickListener
            } else {
                setAvailability.slots = ArrayList()
                setAvailability.slots?.addAll(itemsInterval)
            }
            setAvailability.applyoption = AvailabilityType.WEEK_WISE

            val intent = Intent()
            intent.putExtra(WORKING_TIME, setAvailability)
            resultFragmentIntent(
                    this, targetFragment ?: this,
                    AppRequestCode.ADD_AVAILABILITY, intent
            )
        }

        binding.tvDate.setOnClickListener {
            makeActionAvailability(AvailabilityType.SPECIFIC_DATE)
        }

        binding.tvDays.setOnClickListener {
            makeActionAvailability(AvailabilityType.SPECIFIC_DAY)
        }

        binding.tvAllWeekDays.setOnClickListener {
            makeActionAvailability(AvailabilityType.WEEKDAYS)
        }
    }

    private fun makeActionAvailability(applyOption: String) {
        val setAvailability = SetAvailability()

        setAvailability.date = dateFormatFromMillis(DateFormat.DATE_FORMAT, dateSelected.date
                ?: 0)

        if (itemsInterval[itemsInterval.size - 1].start_time == null) {
            binding.tvNext.showSnackBar(getString(R.string.select_time))
            return
        } else {
            setAvailability.slots = ArrayList()
            setAvailability.slots?.addAll(itemsInterval)
        }
        setAvailability.applyoption = applyOption

        val intent = Intent()
        intent.putExtra(WORKING_TIME, setAvailability)
        resultFragmentIntent(
                this, targetFragment ?: this,
                AppRequestCode.ADD_AVAILABILITY, intent
        )

    }

    private fun hitApi(date: String) {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["doctor_id"] = userRepository.getUser()?.id ?: ""
            hashMap["date"] = date
            hashMap["service_id"] = arguments?.getString(SERVICE_ID, "") ?: ""
            hashMap["category_id"] = arguments?.getString(CATEGORY_PARENT_ID, "") ?: ""

            viewModel.getSlots(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.getSlots.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    itemsInterval.clear()
                    if (it.data?.slots.isNullOrEmpty()) {
                        itemsInterval.add(Interval())
                    } else {
                        itemsInterval.addAll(it.data?.slots ?: emptyList())
                    }
                    adapter.notifyDataSetChanged()

                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    adapter.setAllItemsLoaded(true)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                    binding.clLoader.visible()
                }
            }
        })
    }

    fun checkWeekDaySelected() {
        var enable = false
        itemWeekDays.forEach {
            if (it) {
                enable = true
                return@forEach
            }
        }

        if (enable) {
            binding.tvAddAvailability.visible()
            binding.rvListing.visible()
            binding.tvNext.visible()
        } else {
            binding.tvAddAvailability.gone()
            binding.rvListing.gone()
            binding.tvNext.invisible()
        }
    }

    fun clickItem(item: Interval?) {

    }

    fun selectTime(start: Boolean, position: Int) {
        timePositionSelected = position

        val item = itemsInterval[position]

        DateUtils.getTime(
                requireContext(),
                item.start_time ?: "",
                item.end_time ?: "",
                isStart = start,
                listener = this
        )
    }


    override fun onTimeSelected(time: Triple<String, Boolean, Boolean>) {
        if (!time.third) {
            var addInterval = true

            val sdf = SimpleDateFormat(DateFormat.TIME_FORMAT, Locale.ENGLISH)

            val intervalTime = sdf.parse(time.first)

            /*Check if new interval is not in between of old interval*/
            itemsInterval.forEachIndexed { index, interval ->
                if (index != timePositionSelected && interval.start_time != null && interval.end_time != null) {
                    val timeStart = sdf.parse(interval.start_time)
                    val timeEnd = sdf.parse(interval.end_time)

                    val indexPos = itemsInterval.size - 1
                    val timeStartCurrent = if (itemsInterval[indexPos].start_time == null) null else sdf.parse(itemsInterval[indexPos].start_time)
                    val timeEndCurrent = if (itemsInterval[indexPos].end_time == null) null else sdf.parse(itemsInterval[indexPos].end_time)

                    if (intervalTime.after(timeStart) && intervalTime.before(timeEnd)) {
                        addInterval = false
                    } else if ((time.second && timeEndCurrent != null) &&
                            ((intervalTime == timeStart && timeEndCurrent == timeEnd)
                                    || (intervalTime.before(timeStart) && timeEndCurrent.after(timeEnd))
                                    || (intervalTime.before(timeStart) && timeEndCurrent == timeEnd)
                                    || (intervalTime == timeStart && timeEndCurrent.after(timeEnd)))) {
                        addInterval = false
                    } else if (timeStartCurrent != null &&
                            ((intervalTime == timeEnd && timeStartCurrent == timeStart)
                                    || (intervalTime.after(timeEnd) && timeStartCurrent.before(timeStart))
                                    || (intervalTime.after(timeEnd) && timeStartCurrent == timeStart)
                                    || (intervalTime == timeEnd && timeStartCurrent.before(timeStart)))) {
                        addInterval = false
                    }

                    if (!addInterval) {
                        binding.tvWeekDays.showSnackBar(getString(R.string.error_in_between_interval))
                        return
                    }
                }
            }

            if (addInterval) {
                if (time.second)
                    itemsInterval[timePositionSelected].start_time = time.first
                else
                    itemsInterval[timePositionSelected].end_time = time.first

                adapter.notifyDataSetChanged()
            }
        } else {
            binding.tvWeekDays.showSnackBar(getString(R.string.greater_time))
        }
    }

    fun onDateSelected(item: DatesAvailability) {
        binding.rvWeek.smoothScrollToPosition(itemDays.indexOf(item))

        dateSelected = item

        binding.tvAddAvailability.visible()
        binding.rvListing.visible()

        val date = dateFormatFromMillis(DateFormat.MON_DATE_YEAR, item.date ?: 0)
        binding.tvDate.visible()
        binding.tvDate.text = getString(R.string.for_s, date)
        binding.tvDays.visible()
        binding.tvDays.text = getString(R.string.all_s, item.displayName)
        binding.tvAllWeekDays.visible()

        hitApi(dateFormatFromMillis(DateFormat.DATE_FORMAT, item.date ?: 0))
    }

    companion object {
        const val WORKING_TIME = "WORKING_TIME"
    }


}

interface OnTimeSelected {
    fun onTimeSelected(time: Triple<String, Boolean, Boolean>)
}
