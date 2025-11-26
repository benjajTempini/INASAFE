package com.example.inasafe

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BusStopsAdapter(private val stops: List<BusStop>) : RecyclerView.Adapter<BusStopsAdapter.StopViewHolder>() {

    class StopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStopName: TextView = view.findViewById(R.id.tvStopName)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val arrivalsContainer: LinearLayout = view.findViewById(R.id.arrivalsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_stop, parent, false)
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val stop = stops[position]
        holder.tvStopName.text = stop.name
        holder.tvDistance.text = stop.distance
        holder.tvLocation.text = stop.location

        holder.arrivalsContainer.removeAllViews()
        for (arrival in stop.arrivals) {
            val arrivalView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_bus_arrival, holder.arrivalsContainer, false)

            val tvRoute = arrivalView.findViewById<TextView>(R.id.tvRoute)
            val tvTime = arrivalView.findViewById<TextView>(R.id.tvTime)
            val tvStatus = arrivalView.findViewById<TextView>(R.id.tvStatus)

            tvRoute.text = arrival.route
            tvTime.text = arrival.time
            tvStatus.text = arrival.status

            if (arrival.status == "Retrasado") {
                tvStatus.setTextColor(Color.RED)
            } else {
                tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            }

            holder.arrivalsContainer.addView(arrivalView)
        }
    }

    override fun getItemCount() = stops.size
}