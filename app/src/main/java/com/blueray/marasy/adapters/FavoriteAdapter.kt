package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ItemProductBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.interfaces.OnFavoriteProductClick
import com.blueray.marasy.model.Product
import com.bumptech.glide.Glide

class FavoriteAdapter(val onProductClick: OnFavoriteProductClick)
    : ListAdapter<Product, FavoriteAdapter.FavoriteViewHolder>(DiffCallback) {

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

    inner class FavoriteViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding =
            ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val item = getItem(position)


        holder.binding.apply {
            name.text = item.title
            price.text = item.variations[0].price + " JD"
            Glide.with(holder.itemView.context)
                .load(HelperUtils.BASE_URL + item.images)
                .placeholder(R.drawable.marasy_logo)
                .into(image)
            favoriteCheckBox.isChecked = true
        }
        holder.itemView.setOnClickListener {
            onProductClick.onProductDetailsClick(item.pid)
        }
        holder.binding.favoriteCheckBox.setOnClickListener {
            onProductClick.onFavoriteClick(item.pid, position)
        }
    }
}