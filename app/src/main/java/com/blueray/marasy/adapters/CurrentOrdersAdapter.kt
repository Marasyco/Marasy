package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.databinding.OrdersItemBinding
import com.blueray.marasy.interfaces.OrderItemListener
import com.blueray.marasy.model.ViewOrdersData

class CurrentOrdersAdapter(
    var list: List<ViewOrdersData>,
    val listener: OrderItemListener
) : RecyclerView.Adapter<CurrentOrdersAdapter.OrderViewHolder>() {
    inner class OrderViewHolder(val binding: OrdersItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = OrdersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            dateTv.text = data.date
            orderNumberTv.text = "#" + data.order_number
            quantityTv.text = data.order_items_count.toString()
            priceTv.text = data.total_order_price + " JD"
        }
        holder.itemView.setOnClickListener {
            listener.onOrderClick(data.order_id)
        }
    }
}