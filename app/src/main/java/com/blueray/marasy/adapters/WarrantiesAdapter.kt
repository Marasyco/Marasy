package com.blueray.marasy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.WarrantyItemBinding
import com.blueray.marasy.model.WarrantiesData

class WarrantiesAdapter : ListAdapter<WarrantiesData, WarrantiesAdapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<WarrantiesData>() {
            override fun areItemsTheSame(
                oldItem: WarrantiesData,
                newItem: WarrantiesData
            ): Boolean {
                return oldItem.invoice_no == newItem.invoice_no
            }

            override fun areContentsTheSame(
                oldItem: WarrantiesData,
                newItem: WarrantiesData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(val binding: WarrantyItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            WarrantyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            productName.text = item.body
            expireDate.text =
                holder.itemView.context.getString(R.string.expires_in) + item.warranty_expiry_date

        }
    }
}