package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ShopperOrderProductItemBinding
import com.blueray.marasy.model.ShopperOrderItem
import com.bumptech.glide.Glide

class ShopperOrderProductsAdapter(
    var list: List<ShopperOrderItem>,
    var category: String,
    private val onAvailableClick: (ShopperOrderItem) -> Unit,
    private val onUnAvailableClick: (ShopperOrderItem) -> Unit
) : RecyclerView.Adapter<ShopperOrderProductsAdapter.ShopperProductViewHolder>() {

    inner class ShopperProductViewHolder(val binding: ShopperOrderProductItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopperProductViewHolder {
        val binding = ShopperOrderProductItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShopperProductViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ShopperProductViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            productName.text = data.title
            Glide.with(holder.itemView.context).load(data.image).placeholder(R.drawable.marasy_logo)
                .into(productImage)
            categoryName.text = category.toString()
            quantityTv.text = data.quantity.toString()
            priceTv.text = data.total_unit_price + " JD"
            availableButton.setOnClickListener {
                onAvailableClick(data)
            }
            editButton.setOnClickListener {
                onUnAvailableClick(data)
            }
        }
    }
}