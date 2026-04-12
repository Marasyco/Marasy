package com.blueray.marasy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.blueray.marasy.R
import com.blueray.marasy.model.GetCitiesData

class CustomSpinnerAdapter(
    context: Context,
    private val cities: List<GetCitiesData>
) : ArrayAdapter<GetCitiesData>(context, R.layout.spinner_custom_item, cities) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_custom_item, parent, false)
        val city = cities[position]
        val nameTv = view.findViewById<TextView>(R.id.nameTv)
        nameTv.text = city.name
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_custom_item, parent, false)
        val city = cities[position]
        val nameTv = view.findViewById<TextView>(R.id.nameTv)
        nameTv.text = city.name
        return view
    }
}
