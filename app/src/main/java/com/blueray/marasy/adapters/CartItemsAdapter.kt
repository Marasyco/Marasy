package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.CartItemBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.interfaces.CartItemListener
import com.blueray.marasy.model.CartItem
import com.bumptech.glide.Glide

class CartItemsAdapter(val listener: CartItemListener) :
    ListAdapter<CartItem, CartItemsAdapter.CartViewHolder>(DiffCallback) {

    inner class CartViewHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root)
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(
                oldItem: CartItem,
                newItem: CartItem
            ): Boolean {
                return oldItem.pid == newItem.pid
            }

            override fun areContentsTheSame(
                oldItem: CartItem,
                newItem: CartItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            Glide.with(holder.itemView.context)
                .load(HelperUtils.BASE_URL + item.image)
                .placeholder(R.drawable.marasy_logo)
                .into(image)

            name.text = item.title
            price.text = item.total_unit_price + " JD"
            quantity.text = item.quantity.toString()

            plusButton.setOnClickListener {
                quantity.text = (item.quantity + 1).toString()
                listener.onQuantityChange(
                    item.order_item_id,
                    holder.binding.quantity.text.toString(),
                )
            }
            minusButton.setOnClickListener {
                if (item.quantity != 1) {
                    quantity.text = (item.quantity - 1).toString()
                    listener.onQuantityChange(
                        item.order_item_id,
                        quantity.text.toString(),
                    )
                } else {
                    listener.onItemDelete(item.order_item_id)
                }
            }

            delete.setOnClickListener {
                listener.onItemDelete(item.order_item_id)
            }
        }
    }
}