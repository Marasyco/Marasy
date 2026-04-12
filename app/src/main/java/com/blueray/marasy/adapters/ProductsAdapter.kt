package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ItemProductBinding
import com.blueray.marasy.helpers.HelperUtils
import com.blueray.marasy.interfaces.ProductListener
import com.blueray.marasy.model.Product
import com.bumptech.glide.Glide

class ProductsAdapter(
    val listener: ProductListener,
) : RecyclerView.Adapter<ProductsAdapter.VH>() {

    private val items = mutableListOf<Product>()
    val current: List<Product> get() = items

    fun submitList(newItems: List<Product>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    // Call this from the child handler to flip the visual state
    fun updateFavorite(position: Int, checked: Boolean) {
        if (position in items.indices) {
            val p = items[position]
            try {
                // If Product is a data class with 'fav: Int'
                val newFav = if (checked) 1 else 0
                // If mutable:
                // p.fav = newFav
                // If immutable:
                items[position] = p.copy(fav = newFav)
                notifyItemChanged(position, "fav_only")
            } catch (_: Exception) {
                notifyItemChanged(position) // fallback
            }
        }
    }

    inner class VH(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains("fav_only")) {
            holder.b.favoriteCheckBox.isChecked = (items[position].fav == 1)
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.name.text = item.title

        // Handle unknown_price
        if (item.unknown_price == 1) {
            holder.b.price.text =
                holder.itemView.context.getString(R.string.ask_for_price_on_whatsapp)
            holder.b.price.textSize = 12f
            holder.b.oldPrice.visibility = android.view.View.GONE
        } else {
            val firstVariation = item.variations.firstOrNull()
            if (firstVariation != null) {
                holder.b.price.text = "${firstVariation.price} JD"
                holder.b.price.textSize = 15f
                
                // Handle offer display
                if (firstVariation.offerFlag == 1 && 
                    firstVariation.old_price.isNotEmpty() && 
                    firstVariation.old_price != "0" && 
                    firstVariation.old_price != "null") {
                    
                    try {
                        val oldPrice = firstVariation.old_price.toDoubleOrNull()
                        val newPrice = firstVariation.price.toDoubleOrNull()
                        
                        if (oldPrice != null && newPrice != null && oldPrice > newPrice) {
                            holder.b.oldPrice.visibility = android.view.View.VISIBLE
                            holder.b.oldPrice.text = "${firstVariation.old_price} JD"
                            holder.b.oldPrice.paintFlags = 
                                holder.b.oldPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                        } else {
                            holder.b.oldPrice.visibility = android.view.View.GONE
                        }
                    } catch (e: Exception) {
                        holder.b.oldPrice.visibility = android.view.View.GONE
                    }
                } else {
                    holder.b.oldPrice.visibility = android.view.View.GONE
                }
            } else {
                holder.b.price.text = ""
                holder.b.oldPrice.visibility = android.view.View.GONE
            }
        }

        Glide.with(holder.itemView)
            .load(HelperUtils.BASE_URL + item.images)
            .placeholder(R.drawable.marasy_logo)
            .into(holder.b.image)

        // Just bind state
        holder.b.favoriteCheckBox.isChecked = (item.fav == 1)

        // Remove previous listeners to prevent duplicates
        holder.b.addToCartButton.setOnClickListener(null)
        holder.itemView.setOnClickListener(null)
        holder.b.favoriteCheckBox.setOnClickListener(null)

        // Add to cart button opens product details
        holder.b.addToCartButton.setOnClickListener {
            listener.onProductClick(item)
        }

        // Favorite checkbox click listener
        holder.b.favoriteCheckBox.setOnClickListener {
            listener.onFavoriteToggle(item, holder.b.favoriteCheckBox.isChecked)
        }

        // Item click also opens product details (but allow scrolling)
        holder.itemView.setOnClickListener {
            listener.onProductClick(item)
        }
    }
}
