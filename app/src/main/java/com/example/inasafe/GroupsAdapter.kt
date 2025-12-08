package com.example.inasafe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.inasafe.data.network.NearbyBusStop

class GroupsAdapter(context: Context, private val stops: List<NearbyBusStop>) :
    ArrayAdapter<NearbyBusStop>(context, 0, stops) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false)
        }

        val stop = stops[position]

        val tvStopId = view!!.findViewById<TextView>(R.id.tvStopId)
        val tvStopName = view.findViewById<TextView>(R.id.tvStopName)

        tvStopId.text = stop.id
        // Clean up the stop name to be more readable
        tvStopName.text = stop.name.substringAfter(stop.id).removePrefix("-")

        return view
    }
}