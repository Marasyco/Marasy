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
import com.blueray.marasy.model.Product
import com.bumptech.glide.Glide

class BestSellingAdapter(val listener: OnBestSellingClick) :
    ListAdapter<Product, BestSellingAdapter.BestSellingViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(
                oldItem: Product,
                newItem: Product
            ): Boolean {
                return oldItem.pid == newItem.pid
            }

            override fun areContentsTheSame(
                oldItem: Product,
                newItem: Product
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
                .load(HelperUtils.BASE_URL + item.images)
                .placeholder(R.drawable.marasy_logo)
                .into(image)

            holder.itemView.setOnClickListener {
                listener.onProductDetailsClick(item.pid)
            }
        }
    }
}