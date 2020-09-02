package com.consultantvendor.ui.loginSignUp.document

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.AdditionalField
import com.consultantvendor.data.models.responses.Service
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemDocumentsBinding
import com.consultantvendor.databinding.RvItemServiceBinding
import com.consultantvendor.utils.*


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
            //binding.tvAdd.hideShowView(addOption)
            binding.tvAdd.setOnClickListener {
                fragment.addDocument(adapterPosition,null)
            }
        }

        fun bind(item: AdditionalField) = with(binding) {
            tvName.text = item.name

            val adapterItem = DocumentsItemAdapter(fragment, adapterPosition, item.documents)
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

