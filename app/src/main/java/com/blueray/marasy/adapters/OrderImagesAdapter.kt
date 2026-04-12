package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.databinding.OrderCountItemBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.model.Item
import com.bumptech.glide.Glide

class OrderImagesAdapter(
    val list: List<Item>
) : RecyclerView.Adapter<OrderImagesAdapter.OrderImageViewHolder>() {
    inner class OrderImageViewHolder(val binding: OrderCountItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderImageViewHolder {
        val binding =
            OrderCountItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderImageViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: OrderImageViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            Glide.with(holder.itemView.context).load(HelperUtils.BASE_URL + data.image)
                .into(productImage)
            productName.text = data.title
        }
    }
}