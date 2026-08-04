package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.BestSellingItemBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.interfaces.OnBestSellingClick
import com.blueray.marasy.model.Item
import com.blueray.marasy.model.Product
import com.bumptech.glide.Glide

class MyOrderDetailsItemsAdapter(val listener: OnBestSellingClick) :
    ListAdapter<Item, MyOrderDetailsItemsAdapter.BestSellingViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Item>() {
            override fun areItemsTheSame(
                oldItem: Item,
                newItem: Item
            ): Boolean {
                return oldItem.order_item_id == newItem.order_item_id
            }

            override fun areContentsTheSame(
                oldItem: Item,
                newItem: Item
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class BestSellingViewHolder(val binding: BestSellingItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSellingViewHolder {
        val binding =
            BestSellingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BestSellingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BestSellingViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            name.text = item.title
            Glide.with(holder.itemView.context)
                .load(HelperUtils.BASE_URL + item.image)
                .placeholder(R.drawable.marasy_logo)
                .into(image)

            holder.itemView.setOnClickListener {
                listener.onProductDetailsClick(item.order_item_id)
            }
        }
    }
}