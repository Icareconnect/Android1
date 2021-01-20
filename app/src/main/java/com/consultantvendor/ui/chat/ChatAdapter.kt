package com.consultantvendor.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.chat.ChatList
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemChatListingBinding
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.utils.*
import com.consultantvendor.utils.DateFormat.DATE_TIME_FORMAT
import com.consultantvendor.utils.DocType.IMAGE


class ChatAdapter(private val fragment: ChatFragment, private val items: ArrayList<ChatList>) :
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
                            R.layout.item_chat_listing, parent, false
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

    inner class ViewHolder(val binding: ItemChatListingBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatList) = with(binding) {
           // slideRecyclerItem(binding.root, binding.root.context)

            if (item.last_message?.messageType == IMAGE) {
                tvTextMessage.gone()
                ivCamera.visible()
                tvPhoto.visible()
            } else {
                tvTextMessage.visible()
                ivCamera.gone()
                tvPhoto.gone()
            }
            loadImage(ivUserImage, item.from_user?.profile_image, R.drawable.image_placeholder)
            tvUserName.text = "${item.to_user?.name} (${item.status})"
            tvTextMessage.text = item.last_message?.message
            tvDate.text = DateUtils.dateFormatFromMillis(DATE_TIME_FORMAT, item.last_message?.sentAt)

            if (item.unReadCount > 0) {
                tvUnreadCount.visible()
                tvUnreadCount.text = getCountFormat(2, item.unReadCount)
            } else {
                tvUnreadCount.gone()
            }

            itemView.setOnClickListener {
                fragment.startActivity(item)
                item.unReadCount = 0
                notifyItemChanged(adapterPosition)
            }

        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
