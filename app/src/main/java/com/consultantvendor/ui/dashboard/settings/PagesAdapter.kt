package com.consultantvendor.ui.dashboard.settings

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Page
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagesTitleBinding
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.ui.webview.WebViewActivity


class PagesAdapter(private val items: List<Page>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                    R.layout.item_pages_title, parent, false
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

    inner class ViewHolder(val binding: ItemPagesTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvPage.setOnClickListener {
                binding.root.context.startActivity(
                    Intent(binding.root.context, WebViewActivity::class.java)
                        .putExtra(WebViewActivity.LINK_TITLE, items[adapterPosition].title)
                        .putExtra(WebViewActivity.LINK_URL, items[adapterPosition].slug))
            }
        }

        fun bind(item: Page) = with(binding) {
            tvPage.text = item.title

        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
