package com.consultantvendor.ui.dashboard.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemAppointmentBinding
import com.consultantvendor.utils.*


class AppointmentAdapter(
        private val fragment: AppointmentFragment,
        private val items: ArrayList<Request>
) :
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
                            R.layout.rv_item_appointment, parent, false
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

    inner class ViewHolder(val binding: RvItemAppointmentBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvAccept.setOnClickListener {
                fragment.proceedRequest(items[adapterPosition])
            }

            binding.tvCancel.setOnClickListener {
                fragment.cancelAppointment(items[adapterPosition])
            }
            binding.clAppointment.setOnClickListener {
                fragment.viewDetails(items[adapterPosition])
            }
        }

        fun bind(request: Request) = with(binding) {
            val context = binding.root.context

            tvAccept.visible()
            tvCancel.hideShowView(request.canCancel)

            tvName.text = request.from_user?.name
            tvServiceTypeV.text = request.extra_detail?.filter_name ?:""
            tvDistanceV.text = request.extra_detail?.distance ?:""
            tvLocation.text = request.extra_detail?.service_address
            loadImage(binding.ivPic, request.from_user?.profile_image,
                    R.drawable.ic_profile_placeholder)

            tvDateTime.text = "${DateUtils.dateTimeFormatFromUTC(
                    DateFormat.MON_YEAR_FORMAT, request.created_at)} & " +
                    "${DateUtils.dateTimeFormatFromUTC(DateFormat.TIME_FORMAT, request.created_at)}"

            tvBookingDateV.text = getDatesComma(request.extra_detail?.working_dates)
            tvBookingTimeV.text = "${request.extra_detail?.start_time?:""} - ${request.extra_detail?.end_time?:""}"

            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            binding.tvAccept.setBackgroundResource(R.drawable.drawable_bg_accept)
            when (request.status) {
                CallAction.PENDING -> {
                    tvStatus.text = context.getString(R.string.new_request)
                    tvAccept.text = context.getString(R.string.accept)
                }
                CallAction.ACCEPT -> {
                    tvStatus.text = context.getString(R.string.accepted)
                    tvAccept.text = context.getString(R.string.start_request)
                    binding.tvAccept.setBackgroundResource(R.drawable.drawable_bg_theme)
                    tvCancel.gone()
                }
                CallAction.START -> {
                    tvStatus.text = context.getString(R.string.inprogess)
                    tvAccept.text = context.getString(R.string.track_status)
                    binding.tvAccept.setBackgroundResource(R.drawable.drawable_bg_theme)
                    tvCancel.gone()
                    //tvAccept.gone()
                }
                CallAction.REACHED -> {
                    tvStatus.text = context.getString(R.string.reached_destination)
                    tvAccept.text = context.getString(R.string.track_status)
                    binding.tvAccept.setBackgroundResource(R.drawable.drawable_bg_theme)
                    //tvAccept.gone()
                    tvCancel.gone()
                }
                CallAction.START_SERVICE -> {
                    tvStatus.text = context.getString(R.string.started)
                    tvAccept.text = context.getString(R.string.track_status)
                    binding.tvAccept.setBackgroundResource(R.drawable.drawable_bg_theme)
                    tvCancel.gone()
                }
                CallAction.COMPLETED -> {
                    tvStatus.text = context.getString(R.string.done)
                    tvAccept.gone()
                    tvCancel.gone()
                }
                CallAction.FAILED -> {
                    tvAccept.gone()
                    tvStatus.text = context.getString(R.string.no_show)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    tvCancel.gone()
                }
                CallAction.CANCELED -> {
                    tvStatus.text = context.getString(R.string.canceled)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    tvAccept.gone()
                    tvCancel.gone()
                }
                CallAction.CANCEL_SERVICE -> {
                    tvStatus.text = context.getString(R.string.canceled_service)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    tvCancel.gone()
                    tvAccept.gone()
                }
                else -> {
                    tvStatus.text = context.getString(R.string.new_request)
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
