package com.consultantvendor.ui.loginSignUp.document

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.AdditionalFieldDocument
import com.consultantvendor.data.network.Config
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemDocumentItemBinding
import com.consultantvendor.utils.*


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

                fragment.notifyDocument()
            }

            binding.ivPic.setOnClickListener {
                if (items[adapterPosition].file_name == null)
                    fragment.addDocument(positionMain, adapterPosition)
                else {
                    val itemImages = java.util.ArrayList<String>()
                    itemImages.add("${Config.imageURL}${ImageFolder.UPLOADS}${items[adapterPosition].file_name}")
                    viewImageFull(fragment.requireActivity(), itemImages, 0)
                }
            }
        }

        fun bind(item: AdditionalFieldDocument) = with(binding) {
            val context = binding.root.context

            /*tvName.text = item.title
            tvDesc.text = item.description*/

            binding.tvStatus.hideShowView(!item.status.isNullOrEmpty())
            binding.ivEdit.visible()
            binding.ivDelete.visible()

            if (item.file_name == null) {
                ivPic.setImageResource(R.drawable.bt_ic_camera)
                tvUpload.visible()
                binding.ivEdit.gone()
                binding.ivDelete.gone()
            } else {
                loadImage(ivPic, item.file_name, R.drawable.image_placeholder)
                tvUpload.gone()
            }

            when (item.status) {
                DocumentStatus.APPROVED -> {
                    binding.ivEdit.gone()
                    binding.ivDelete.gone()
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.textColorGreen))
                    binding.tvStatus.text = context.getString(R.string.approved)
                }
                DocumentStatus.DECLINED -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    binding.tvStatus.text = context.getString(R.string.declined)
                }
                else -> {
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.bgCall))
                    binding.tvStatus.text = context.getString(R.string.in_progress)
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

