package com.consultantvendor.ui.dashboard.feeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Feed
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemArticleBinding
import com.consultantvendor.utils.*


class FeedsAdapter(private val fragment: FeedsFragment, private val items: ArrayList<Feed>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.rv_item_article, parent, false))
        } else {
            ViewHolderLoader(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.item_paging_loader, parent, false))
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemArticleBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cvBlog.setOnClickListener {
                val feedDetailsFragment = FeedDetailsFragment()
                val bundle = Bundle()
                bundle.putSerializable(EXTRA_REQUEST_ID, items[adapterPosition])
                feedDetailsFragment.arguments = bundle
                replaceResultFragment(fragment, feedDetailsFragment, R.id.container, AppRequestCode.ARTICLE_CHANGES)
            }

            binding.ivFavourite.setOnClickListener {
                fragment.clickFavourite(items[adapterPosition])
            }
        }

        fun bind(item: Feed) = with(binding) {
            tvName.text = item.title
            tvDate.text = DateUtils.dateTimeFormatFromUTC(DateFormat.DATE_FORMAT_SLASH_YEAR, item.created_at)
            loadImage(ivImage, item.image, R.drawable.image_placeholder)

            tvViews.text = binding.root.context.getString(R.string.s_views, item.views)

            if (item.is_favorite == true)
                ivFavourite.setImageResource(R.drawable.ic_like_red)
            else
                ivFavourite.setImageResource(R.drawable.ic_like_white)
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
