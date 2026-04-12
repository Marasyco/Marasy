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
import com.bumptech.glide.Glide

class CategoriesAdapter(
    val onCategoryClick: OnCategoryClick
) : ListAdapter<CategoriesData, CategoriesAdapter.CategoryViewHolder>(DiffCallback) {
    private var lastAnimatedPosition = -1
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CategoriesData>() {
            override fun areItemsTheSame(
                oldItem: CategoriesData,
                newItem: CategoriesData
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: CategoriesData,
                newItem: CategoriesData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class CategoryViewHolder(val binding: CategoriesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            CategoriesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {

//        val displayMetrics = holder.itemView.context.resources.displayMetrics
//        val screenWidth = displayMetrics.widthPixels
//        holder.itemView.layoutParams.width = (screenWidth * 0.35).toInt()

        val item = getItem(position)
        holder.binding.apply {
            name.text = item.name
            Glide.with(holder.itemView.context)
                .load(HelperUtils.BASE_URL + item.image)
                .placeholder(R.drawable.marasy_logo)
                .into(image)
        }
//        if (position > lastAnimatedPosition) {
//            val animation = android.view.animation.AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in)
//
//
////            animation.startOffset = (position * 50).toLong()
//            holder.itemView.startAnimation(animation)
//
//            lastAnimatedPosition = position
//        } else {
//            holder.itemView.clearAnimation()
//        }
        holder.itemView.setOnClickListener {
            onCategoryClick.onCategoryItemClick(position)
        }
    }
}