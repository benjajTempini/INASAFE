package com.example.inasafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inasafe.data.network.BusService

class BusArrivalsAdapter(private var services: List<BusService>) : RecyclerView.Adapter<BusArrivalsAdapter.ArrivalViewHolder>() {

    class ArrivalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvDestination: TextView = view.findViewById(R.id.tvDestination)
        val tvArrival1: TextView = view.findViewById(R.id.tvArrival1)
        val tvArrival2: TextView = view.findViewById(R.id.tvArrival2)
        val tvPlate1: TextView = view.findViewById(R.id.tvPlate1)
        val tvPlate2: TextView = view.findViewById(R.id.tvPlate2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArrivalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_arrival, parent, false)
        return ArrivalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArrivalViewHolder, position: Int) {
        val service = services[position]
        holder.tvServiceName.text = service.serviceName
        holder.tvDestination.text = service.destination
        
        // Primer bus
        holder.tvArrival1.text = if (service.arrival1.isNullOrEmpty()) "Sin info" else service.arrival1
        holder.tvPlate1.text = if (service.plate1.isNullOrEmpty()) "---" else service.plate1

        // Segundo bus
        holder.tvArrival2.text = if (service.arrival2.isNullOrEmpty()) "Sin info" else service.arrival2
        holder.tvPlate2.text = if (service.plate2.isNullOrEmpty()) "---" else service.plate2
    }

    override fun getItemCount() = services.size

    fun updateServices(newServices: List<BusService>) {
        services = newServices
        notifyDataSetChanged()
    }
}