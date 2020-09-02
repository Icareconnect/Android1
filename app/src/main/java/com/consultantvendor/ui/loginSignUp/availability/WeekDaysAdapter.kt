package com.consultantvendor.ui.loginSignUp.availability

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.databinding.ItemWeekDaysBinding

class WeekDaysAdapter(private val fragment: SetAvailabilityFragment, private val items: ArrayList<Boolean>) :
        RecyclerView.Adapter<WeekDaysAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_week_days, parent, false))
    }

    inner class ViewHolder(val binding: ItemWeekDaysBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(weekDays: Boolean) = with(binding) {

            tvName.text = when (adapterPosition) {
                0 -> fragment.getString(R.string.sunday_w)
                1 -> fragment.getString(R.string.monday_w)
                2 -> fragment.getString(R.string.tuesday_w)
                3 -> fragment.getString(R.string.wednesday_w)
                4 -> fragment.getString(R.string.thursday_w)
                5 -> fragment.getString(R.string.friday_w)
                6 -> fragment.getString(R.string.saturday_w)
                else -> fragment.getString(R.string.sunday_w)
            }

            if (weekDays) {
                tvName.setBackgroundResource(R.drawable.drawable_week_selected)
                tvName.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.colorWhite))
            } else {
                tvName.setBackgroundResource(R.drawable.drawable_week_un_selected)
                tvName.setTextColor(ContextCompat.getColor(fragment.requireContext(), R.color.colorBlack))
            }

            binding.clWeekDays.setOnClickListener {
                items[adapterPosition] = !items[adapterPosition]
                notifyItemChanged(adapterPosition)
                fragment.checkWeekDaySelected()
            }
        }
    }
}
