package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.CategoriesItemBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.interfaces.OnTrademarkClick
import com.blueray.marasy.model.TrademarkData
import com.bumptech.glide.Glide

class TrademarksGridAdapter(
    val onTrademarkClick: OnTrademarkClick
) : ListAdapter<TrademarkData, TrademarksGridAdapter.TrademarkViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TrademarkData>() {
            override fun areItemsTheSame(
                oldItem: TrademarkData,
                newItem: TrademarkData
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: TrademarkData,
                newItem: TrademarkData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class TrademarkViewHolder(val binding: CategoriesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrademarkViewHolder {
        val binding =
            CategoriesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrademarkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrademarkViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            name.text = item.name
            
            // Load image if available, otherwise use placeholder
            if (!item.image.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(HelperUtils.BASE_URL + item.image)
                    .placeholder(R.drawable.marasy_logo)
                    .into(image)
            } else {
                // Use placeholder when image is null
                image.setImageResource(R.drawable.marasy_logo)
            }
        }

        holder.itemView.setOnClickListener {
            onTrademarkClick.onTrademarkClick(position)
        }
    }
}
