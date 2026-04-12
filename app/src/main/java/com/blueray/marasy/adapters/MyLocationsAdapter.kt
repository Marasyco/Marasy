package com.blueray.marasy.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.AddressItemBinding
import com.blueray.marasy.interfaces.AddressItemListener
import com.blueray.marasy.model.GetMyAddressData

class MyLocationsAdapter(
    var list: List<GetMyAddressData>,
    val listener: AddressItemListener
) : RecyclerView.Adapter<MyLocationsAdapter.MyLocationViewHolder>() {
    inner class MyLocationViewHolder(val binding: AddressItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyLocationViewHolder {
        val binding =
            AddressItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyLocationViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MyLocationViewHolder, position: Int) {
        Log.d("LISTING HERE", list.toString())
        val data = list[position]
        holder.binding.apply {
            val fullAddress = data.FullAddress

            locationName.text =
                fullAddress.address_line1

            locationDetailsTv.text =
                fullAddress?.city_and_area + " , " + fullAddress.detailed_address
                    ?: "No details available"
            if (data.is_default == "1") {
                card.setCardBackgroundColor(
                    holder.itemView.context.resources.getColor(R.color.light_orange)
                )
                locationName.setTextColor(
                    holder.itemView.context.resources.getColor(R.color.black)
                )
                locationDetailsTv.setTextColor(
                    holder.itemView.context.resources.getColor(R.color.black)
                )
//                options.setColorFilter(holder.itemView.context.resources.getColor(R.color.white))
//                delete.setColorFilter(holder.itemView.context.resources.getColor(R.color.white))
            } else {
                card.setCardBackgroundColor(
                    holder.itemView.context.resources.getColor(R.color.white)
                )
                locationName.setTextColor(
                    holder.itemView.context.resources.getColor(R.color.black_text)
                )
                locationDetailsTv.setTextColor(
                    holder.itemView.context.resources.getColor(R.color.black_text)
                )
//                options.setColorFilter(holder.itemView.context.resources.getColor(R.color.red))
//                delete.setColorFilter(holder.itemView.context.resources.getColor(R.color.red))
            }
            root.setOnClickListener {
                listener.onDefaultClick(data.profile_id)
            }
            options.setOnClickListener {
                listener.onOptionsClick(data.profile_id)
            }
            delete.setOnClickListener {
                listener.onDeleteClick(data.profile_id)
            }
        }
    }
}