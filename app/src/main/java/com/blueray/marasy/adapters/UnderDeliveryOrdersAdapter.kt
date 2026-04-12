package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.databinding.WaitingDeliveryItemBinding
import com.blueray.marasy.helpers.HelperUtils.openGoogleMaps
import com.blueray.marasy.helpers.ViewUtils.hide
import com.blueray.marasy.interfaces.DriverOrdersListener
import com.blueray.marasy.model.DriverOrdersData

class UnderDeliveryOrdersAdapter(
    private val listener: DriverOrdersListener
) : ListAdapter<DriverOrdersData, UnderDeliveryOrdersAdapter.DriverOrderViewHolder>(DiffCallback) {


    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DriverOrdersData>() {
            override fun areItemsTheSame(
                oldItem: DriverOrdersData,
                newItem: DriverOrdersData
            ): Boolean {
                return oldItem.order_id == newItem.order_id
            }

            override fun areContentsTheSame(
                oldItem: DriverOrdersData,
                newItem: DriverOrdersData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


    inner class DriverOrderViewHolder(val binding: WaitingDeliveryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var lastAnimatedPosition = -1



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverOrderViewHolder {
        val binding =
            WaitingDeliveryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DriverOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DriverOrderViewHolder, position: Int) {
        val data = getItem(position)
        holder.binding.apply {
            clientName.text = data.field_full_name
            orderState.text = data.state_name
            orderNumberTv.text = "#" + data.order_number
            orderPriceTv.text = data.total_order_price + " JD"
            numberOfItemsTv.text = data.order_items_count.toString()
            addressTv.text = data.billing_information.address_line1

            locationButton.setOnClickListener {
                openGoogleMaps(
                    holder.itemView.context,
                    data.billing_information.lat,
                    data.billing_information.lon
                )
            }

            deliveryButton.hide()
        }

        holder.itemView.setOnClickListener {
            listener.onOrderClick(data.order_id)
        }

//        if (position > lastAnimatedPosition) {
//            val animation = android.view.animation.AnimationUtils.loadAnimation(
//                holder.itemView.context,
//                R.anim.slide_in_bottom
//            )
//            animation.startOffset = (position * 200).toLong()
//            holder.itemView.startAnimation(animation)
//            lastAnimatedPosition = position
//        } else {
//            holder.itemView.clearAnimation()
//        }
    }

}
