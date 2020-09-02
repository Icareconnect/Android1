package com.consultantvendor.ui.drawermenu.classes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.ClassData
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemClassBinding
import com.consultantvendor.utils.*


class ClassesAdapter(private val fragment: ClassesFragment, private val items: ArrayList<ClassData>) :
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
                            R.layout.rv_item_class, parent, false
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

    inner class ViewHolder(val binding: RvItemClassBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvStartClass.setOnClickListener {
                fragment.startClass(adapterPosition)
            }

            binding.tvCompleteClass.setOnClickListener {
                fragment.completeClass(adapterPosition)
            }
        }

        fun bind(item: ClassData) = with(binding) {
            val context = binding.root.context
            slideRecyclerItem(binding.root, context)

            tvName.text = item.created_by?.name
            tvDesc.text = item.created_by?.categoryData?.name ?: context.getString(R.string.na)

            loadImage(binding.ivPic, item.created_by?.profile_image, R.drawable.ic_profile_placeholder)

            tvClassName.text = item.name

            val classTime = DateUtils.dateTimeFormatFromUTC(DateFormat.DATE_TIME_FORMAT, item.bookingDateUTC)
            tvClassTime.text = classTime
            tvClassPrice.text = getCurrency(item.price)
            tvUsers.text = "${context.getString(R.string.user_joined)} : ${item.totalAssignedUser}"

            when (item.status) {
                ClassType.STARTED -> {
                    tvCompleteClass.visible()
                    tvStartClass.visible()
                }
                ClassType.COMPLETED -> {
                    tvCompleteClass.gone()
                    tvStartClass.gone()
                }
                ClassType.ADDED -> {
                    tvCompleteClass.gone()
                    tvStartClass.visible()
                }
            }
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
