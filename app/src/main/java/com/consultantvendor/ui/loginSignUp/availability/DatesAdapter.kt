package com.consultantvendor.ui.loginSignUp.availability

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.DatesAvailability
import com.consultantvendor.databinding.ItemDatesBinding
import com.consultantvendor.utils.DateFormat.MON_DATE_YEAR
import com.consultantvendor.utils.DateUtils.dateFormatFromMillis
import java.util.*

class DatesAdapter(private val fragment: SetAvailabilityFragment, private val items: ArrayList<DatesAvailability>) :
        RecyclerView.Adapter<DatesAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_dates, parent, false))
    }

    inner class ViewHolder(val binding: ItemDatesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DatesAvailability) = with(binding) {

            cbName.text = when (adapterPosition) {
                0 -> fragment.getString(R.string.today)
                1 -> fragment.getString(R.string.tomorrow)
                else -> item.displayName
            }

            cbDate.text = dateFormatFromMillis(MON_DATE_YEAR, item.date ?: 0)

            cbName.isChecked = item.isSelected
            cbDate.isChecked = item.isSelected

            if (item.isSelected) {
                clDate.setBackgroundResource(R.drawable.drawable_theme_stroke)
            } else {
                clDate.setBackgroundResource(R.drawable.drawable_bg_button)
            }


            clDate.setOnClickListener {
                if (!item.isSelected) {
                    for (count: Int in 0 until items.size) {
                        items[count].isSelected = count == adapterPosition
                        notifyItemChanged(count)
                    }
                    fragment.onDateSelected(item)
                }
            }
        }
    }
}
