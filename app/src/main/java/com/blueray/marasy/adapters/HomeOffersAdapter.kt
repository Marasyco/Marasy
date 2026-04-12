package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.HomeOfferItemBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.interfaces.HomeOffersListener
import com.blueray.marasy.model.Product
import com.bumptech.glide.Glide

class HomeOffersAdapter(val listener: HomeOffersListener) :
    ListAdapter<Product, HomeOffersAdapter.OfferViewHolder>(DiffCallback) {

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

    inner class OfferViewHolder(val binding: HomeOfferItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding =
            HomeOfferItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            name.text = item.title
            oldPrice.text = "${item.variations.firstOrNull()?.old_price.orEmpty()} JD"
            newPrice.text = "${item.variations.firstOrNull()?.price.orEmpty()} JD"
            Glide.with(holder.itemView.context)
                .load(HelperUtils.BASE_URL + item.images)
                .placeholder(R.drawable.marasy_logo)
                .into(image)
        }

        holder.itemView.setOnClickListener {
            listener.onProductClick(item.pid)
        }

    }
}