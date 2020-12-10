package com.consultantvendor.ui.loginSignUp.document

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.AdditionalField
import com.consultantvendor.data.models.responses.AdditionalFieldDocument
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemDocumentsBinding
import com.consultantvendor.utils.hideShowView


class DocumentsAdapter(private val fragment: DocumentsFragment, private val items: ArrayList<AdditionalField>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    private var addOption = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.rv_item_documents, parent, false))
        } else {
            ViewHolderLoader(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                    R.layout.item_paging_loader, parent, false))
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemDocumentsBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvAdd.hideShowView(false)
            binding.tvAdd.setOnClickListener {
                fragment.addDocument(adapterPosition, null)
            }
        }

        fun bind(item: AdditionalField) = with(binding) {
            tvName.text = item.name

            val documents = item.documents
            if (documents.size < 2) {
                documents.add(AdditionalFieldDocument())
            }

            val adapterItem = DocumentsItemAdapter(fragment, adapterPosition, documents)
            rvDocuments.adapter = adapterItem
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }

    fun setAddOption(allLoaded: Boolean) {
        addOption = allLoaded
    }
}

