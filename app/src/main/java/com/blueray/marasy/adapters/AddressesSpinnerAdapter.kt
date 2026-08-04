package com.blueray.marasy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.blueray.marasy.R
import com.blueray.marasy.model.GetMyAddressData

class AddressesSpinnerAdapter(
    context: Context,
    private val list: List<GetMyAddressData>
) : ArrayAdapter<GetMyAddressData>(context, R.layout.spinner_custom_item, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_custom_item, parent, false)
        val city = list[position]
        val nameTv = view.findViewById<TextView>(R.id.nameTv)
        nameTv.text = city.FullAddress.locality + "," + city.FullAddress.address_line1
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_custom_item, parent, false)
        val city = list[position]
        val nameTv = view.findViewById<TextView>(R.id.nameTv)
        nameTv.text = city.FullAddress.locality + "," + city.FullAddress.address_line1
        return view
    }
}