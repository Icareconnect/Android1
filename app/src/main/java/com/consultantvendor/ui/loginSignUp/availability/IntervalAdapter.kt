package com.consultantvendor.ui.loginSignUp.availability

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.Interval
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemAddIntervalBinding
import com.consultantvendor.databinding.RvItemIntervalBinding
import com.consultantvendor.utils.gone
import com.consultantvendor.utils.visible


class IntervalAdapter(private val fragment: SetAvailabilityFragment, private val items: ArrayList<Interval>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    private val numberOfInterval = 2

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == LOADING)
            (holder as ViewHolderLoader).bind()
        else
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.rv_item_interval, parent, false
                    )
            )
        } else {
            ViewHolderLoader(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_add_interval, parent, false
                    )
            )
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemIntervalBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvFromV.setOnClickListener {
                fragment.selectTime(true, adapterPosition)
            }

            binding.tvToV.setOnClickListener {
                fragment.selectTime(false, adapterPosition)
            }
        }

        fun bind(item: Interval) = with(binding) {
            tvFromV.setText(item.start_time ?: "")
            tvToV.setText(item.end_time ?: "")

            ivDelete.setOnClickListener {
                if (items.size > 1) {
                    items.removeAt(adapterPosition)
                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class ViewHolderLoader(val binding: ItemAddIntervalBinding) :
            RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvNewInterval.setOnClickListener {
                if (items.size < numberOfInterval) {
                    if (items[items.size - 1].start_time != null && items[items.size - 1].end_time != null) {
                        items.add(Interval())
                        notifyDataSetChanged()
                    }
                }
            }
        }

        fun bind() = with(binding) {
            if (items.size < numberOfInterval) {
                tvNewInterval.visible()
            } else {
                tvNewInterval.gone()
            }
        }

    }

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
