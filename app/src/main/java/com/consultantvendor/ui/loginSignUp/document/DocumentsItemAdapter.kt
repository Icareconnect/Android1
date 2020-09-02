package com.consultantvendor.ui.loginSignUp.document

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.AdditionalFieldDocument
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemDocumentItemBinding
import com.consultantvendor.utils.loadImage


class DocumentsItemAdapter(private val fragment: DocumentsFragment, private val positionMain: Int,
                           private val items: ArrayList<AdditionalFieldDocument>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.rv_item_document_item, parent, false))
        } else {
            ViewHolderLoader(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.item_paging_loader, parent, false))
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemDocumentItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.ivEdit.setOnClickListener {
                fragment.addDocument(positionMain, adapterPosition)
            }

            binding.ivDelete.setOnClickListener {
                items.removeAt(adapterPosition)
                notifyDataSetChanged()
            }
        }

        fun bind(item: AdditionalFieldDocument) = with(binding) {
            tvName.text = item.title
            tvDesc.text = item.description
            loadImage(ivImage, item.file_name, R.drawable.image_placeholder)
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}

