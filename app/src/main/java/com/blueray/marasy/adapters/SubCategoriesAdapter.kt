package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.CategoriesItemBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.interfaces.OnCategoryClick
import com.blueray.marasy.model.CategoriesData
import com.blueray.marasy.model.SubCategoriesData
import com.bumptech.glide.Glide

class SubCategoriesAdapter( val listener: OnCategoryClick) :
    ListAdapter<SubCategoriesData, SubCategoriesAdapter.SubCategoriesViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SubCategoriesData>() {
            override fun areItemsTheSame(
                oldItem: SubCategoriesData,
                newItem: SubCategoriesData
            ): Boolean {
                return oldItem.tid == newItem.tid
            }

            override fun areContentsTheSame(
                oldItem: SubCategoriesData,
                newItem: SubCategoriesData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class SubCategoriesViewHolder(val binding: CategoriesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoriesViewHolder {
        val binding =
            CategoriesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubCategoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubCategoriesViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            name.text = item.name
            // Enable marquee for scrolling text (will only scroll if text is longer than width)
            name.isSelected = true
            Glide.with(holder.itemView.context)
                .load(HelperUtils.BASE_URL + item.image)
                .placeholder(R.drawable.marasy_logo)
                .into(image)
        }
        holder.itemView.setOnClickListener {
            listener.onCategoryItemClick(position)
        }

    }
}