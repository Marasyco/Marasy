package com.blueray.marasy.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.databinding.ShopperOrderItemBinding
import com.blueray.marasy.interfaces.ShopperOrdersListener
import com.blueray.marasy.model.ShopperOrdersData

class ShopperOrdersAdapter(
    val listener: ShopperOrdersListener
) : ListAdapter<ShopperOrdersData, ShopperOrdersAdapter.ShopperOrderViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ShopperOrdersData>() {
            override fun areItemsTheSame(
                oldItem: ShopperOrdersData,
                newItem: ShopperOrdersData
            ): Boolean {
                return oldItem.order_id == newItem.order_id
            }

            override fun areContentsTheSame(
                oldItem: ShopperOrdersData,
                newItem: ShopperOrdersData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ShopperOrderViewHolder(val binding: ShopperOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopperOrderViewHolder {
        val binding =
            ShopperOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopperOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopperOrderViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            clientNameTv.text = item.field_full_name
            orderNumberTv.text = "#" + item.order_number
            dateTv.text = item.date
            priceTv.text = item.total_order_price + " JD"
            numberOfItemsTv.text = item.order_items_count.toString()
            locationButton.setOnClickListener {
                val uri =
                    Uri.parse("geo:${item.billing_information.lat},${item.billing_information.lon}?q=${item.billing_information.lat},${item.billing_information.lon}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")

                if (intent.resolveActivity(holder.itemView.context.packageManager) != null) {
                    holder.itemView.context.startActivity(intent)
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Google Maps is not installed.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
        holder.itemView.setOnClickListener {
            listener.onOrderClick(item.order_id, position)
        }

    }


}