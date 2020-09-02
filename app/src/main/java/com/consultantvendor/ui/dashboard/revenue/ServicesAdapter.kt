package com.consultantvendor.ui.dashboard.revenue

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Service
import com.consultantvendor.databinding.RvItemServiceRevenueBinding


class ServicesAdapter( private val items: ArrayList<Service>) :
        RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ServicesAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesAdapter.ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.rv_item_service_revenue, parent, false
                )
        )
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: RvItemServiceRevenueBinding) :
            RecyclerView.ViewHolder(binding.root) {


        fun bind(item: Service) = with(binding) {

            try {
                clService.setBackgroundColor(Color.parseColor(item.color_code))
            } catch (e: Exception) {
                clService.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimary))
            }

            tvName.text = item.service_name
            tvCount.text = item.count
        }
    }
}
