package com.consultantvendor.ui.loginSignUp.prefrence

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Filter
import com.consultantvendor.data.models.responses.FilterOption
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemPrefrenceBinding
import com.consultantvendor.ui.loginSignUp.covid.CovidFragment
import com.consultantvendor.utils.PreferencesType


class PrefrenceAdapter(private val fragment: Fragment, private val items: ArrayList<Filter>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.rv_item_prefrence, parent, false
                    )
            )
        } else {
            ViewHolderLoader(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_paging_loader, parent, false
                    )
            )
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemPrefrenceBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            if (fragment is CovidFragment) {
                when (fragment.prefrenceType) {
                    PreferencesType.PERSONAL_INTEREST, PreferencesType.WORK_ENVIRONMENT,
                    PreferencesType.PROVIDABLE_SERVICES -> {
                        binding.tvName.gravity = Gravity.CENTER_HORIZONTAL

                        val layoutManager = LinearLayoutManager(fragment.requireContext())
                        binding.rvListing.layoutManager = layoutManager
                    }
                }

            }

            binding.root.setOnClickListener {
                if (fragment is PrefrenceFragment)
                    fragment.clickItem(items[adapterPosition])
            }

        }

        fun bind(item: Filter) = with(binding) {
            tvName.text = item.preference_name

            val listOptions = ArrayList<FilterOption>()
            listOptions.addAll(item.options ?: emptyList())
            val prefrenceItemAdapter = PrefrenceItemAdapter(item.is_multi == "1", listOptions)
            rvListing.adapter = prefrenceItemAdapter
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}

