package com.example.inasafe

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inasafe.data.network.NearbyBusStop

class BusStopsAdapter(private var stops: List<NearbyBusStop>) : RecyclerView.Adapter<BusStopsAdapter.StopViewHolder>() {

    class StopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStopName: TextView = view.findViewById(R.id.tvStopName)
        val tvStopId: TextView = view.findViewById(R.id.tvLocation) // Reusing tvLocation for stop ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_stop, parent, false)
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val stop = stops[position]
        holder.tvStopName.text = stop.name
        holder.tvStopId.text = stop.id
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, BusArrivalsActivity::class.java).apply {
                putExtra(BusArrivalsActivity.EXTRA_STOP_ID, stop.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = stops.size

    fun updateStops(newStops: List<NearbyBusStop>) {
        stops = newStops
        notifyDataSetChanged()
    }
}