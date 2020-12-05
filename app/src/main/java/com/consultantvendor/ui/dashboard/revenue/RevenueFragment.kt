package com.consultantvendor.ui.dashboard.revenue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.MonthlyRevenue
import com.consultantvendor.data.models.responses.Service
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentRevenueBinding
import com.consultantvendor.utils.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RevenueFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentRevenueBinding

    private var rootView: View? = null

    private lateinit var viewModel: RevenueViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_revenue, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            viewModel.revenue(HashMap())
        }
        return rootView
    }

    private fun initialise() {
        binding.lineChart.isDoubleTapToZoomEnabled = false
        binding.lineChart.setPinchZoom(false)
        binding.clLoader.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))

        viewModel = ViewModelProvider(this, viewModelFactory)[RevenueViewModel::class.java]

        if (requireActivity().intent.hasExtra(PAGE_TO_OPEN)) {
            binding.toolbar.visible()
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }
    }

    private fun bindObservers() {
        viewModel.revenue.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    val revenueData = it.data

                    /*Services selected*/
                    /*val serviceList = ArrayList<Service>()
                    serviceList.addAll(revenueData?.services ?: emptyList())
                    val adapter = ServicesAdapter(serviceList)
                    binding.rvServices.adapter = adapter*/

                    binding.tvAppointmentV.text = revenueData?.totalRequest
                    binding.tvCompleted.text = getString(R.string.s_completed, revenueData?.completedRequest)
                    binding.tvUnsuccessful.text = getString(R.string.unsuccessful, revenueData?.unSuccesfullRequest)
                    binding.tvTotalRevenueV.text = getCurrency(revenueData?.totalRevenue)

                    if ((revenueData?.totalRequest?.toInt() ?: 0) > 0) {
                        val completeProgress = ((revenueData?.completedRequest?.toInt()?.times(100))?.div(revenueData.totalRequest?.toInt()
                                ?: 0))
                        binding.progressCompleted.progress = completeProgress ?: 0
                    } else
                        binding.progressCompleted.progress = 0

                    if ((revenueData?.totalRequest?.toInt() ?: 0) > 0) {
                        val unsuccefullProgress = ((revenueData?.unSuccesfullRequest?.toInt()?.times(100))?.div(revenueData.totalRequest?.toInt()
                                ?: 0))
                        binding.progressUnsuccessful.progress = unsuccefullProgress ?: 0
                    } else
                        binding.progressUnsuccessful.progress = 0

                    setData(revenueData?.monthlyRevenue)


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

    }

    private fun setData(monthlyRevenue: List<MonthlyRevenue>?) {
        if (monthlyRevenue?.size != 0) {
            binding.lineChart.zoomOut()

            val lines = ArrayList<ILineDataSet>()
//        val xAxis = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

            val xAxis = ArrayList<String>()
            val entriesC = ArrayList<Entry>()

            if (monthlyRevenue?.size == 1) {
                xAxis.add("")
                entriesC.add(Entry(0.0f, 0))
            }

            monthlyRevenue?.forEachIndexed { index, revenueItem ->

                xAxis.add(revenueItem.monthName ?: "")

                val indexNew = if (monthlyRevenue.size == 1) index + 1 else index
                entriesC.add(Entry(revenueItem.revenue ?: 0.0f, indexNew))
            }

            val lDataSet = LineDataSet(entriesC, null)
            lDataSet.setDrawFilled(true)
            lDataSet.valueTextSize = resources.getDimension(R.dimen.sp_0)
            lDataSet.circleRadius = 0f

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.drawable_map_transparent)
            lDataSet.fillDrawable = drawable
            lDataSet.lineWidth = resources.getDimension(R.dimen.dp_1)
            lDataSet.setCircleColors(intArrayOf(R.color.graph), requireContext())
            lDataSet.setColors(intArrayOf(R.color.graph), requireContext())
            lines.add(lDataSet)


            //Set y axis
            binding.lineChart.axisLeft.setValueFormatter { value, yAxis ->
                when {
                    value > 99999 -> "${String.format(Locale.ENGLISH, "%.1f", (value / 100000))}m"
                    value > 999 -> "${String.format(Locale.ENGLISH, "%.1f", (value / 1000))}k"
                    value < 0 -> "0"
                    else -> value.toInt().toString()
                }
            }

            //Set final Chart values
            binding.lineChart.data = LineData(xAxis, lines)

            binding.lineChart.xAxis.setDrawAxisLine(false)
            binding.lineChart.xAxis.setDrawGridLines(true)
            binding.lineChart.xAxis.enableGridDashedLine(30f, 20f, 0f)
            binding.lineChart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.textColor)
            binding.lineChart.xAxis.textSize = resources.getDimension(R.dimen.sp_4)
            binding.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

            binding.lineChart.axisLeft.setDrawAxisLine(false)
            binding.lineChart.axisLeft.setDrawGridLines(true)
            binding.lineChart.axisLeft.enableGridDashedLine(30f, 20f, 0f)
            binding.lineChart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.textColor)
            binding.lineChart.axisLeft.textSize = resources.getDimension(R.dimen.sp_4)
            binding.lineChart.axisLeft.setLabelCount(6, false)
            //binding.lineChart.axisLeft.setStartAtZero(true)
            //binding.lineChart.axisLeft.setAxisMinValue(0f)

            binding.lineChart.axisRight.setDrawAxisLine(false)
            binding.lineChart.axisRight.setDrawGridLines(false)
            binding.lineChart.axisRight.textColor = ContextCompat.getColor(requireContext(), android.R.color.white)
            //binding.lineChart.axisRight.setAxisMinValue(0f)
            //binding.lineChart.axisRight.setStartAtZero(true)

            binding.lineChart.animateXY(0, 1500)
            binding.lineChart.legend.isEnabled = false
            binding.lineChart.setDescription("")
            binding.lineChart.setExtraOffsets(0f, 5f, 0f, 8f)
        }
    }

}