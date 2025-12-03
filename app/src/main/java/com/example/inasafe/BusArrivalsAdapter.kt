package com.example.inasafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.inasafe.data.PreferencesManager
import com.example.inasafe.data.network.BusService

class BusArrivalsAdapter(private var services: List<BusService>) : RecyclerView.Adapter<BusArrivalsAdapter.ArrivalViewHolder>() {

    private var walkingTime: Int? = null
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArrivalViewHolder {
        preferencesManager = PreferencesManager(parent.context)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_arrival, parent, false)
        return ArrivalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArrivalViewHolder, position: Int) {
        val service = services[position]
        holder.bind(service, preferencesManager, walkingTime)
    }

    override fun getItemCount() = services.size

    fun updateServices(newServices: List<BusService>, walkingTime: Int?) {
        this.services = newServices
        this.walkingTime = walkingTime
        notifyDataSetChanged()
    }

    class ArrivalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        private val tvDestination: TextView = view.findViewById(R.id.tvDestination)
        private val btnFavorite: ImageButton = view.findViewById(R.id.btnFavorite)

        private val llArrival1: LinearLayout = view.findViewById(R.id.llArrival1)
        private val tvArrival1: TextView = view.findViewById(R.id.tvArrival1)
        private val tvPlate1: TextView = view.findViewById(R.id.tvPlate1)

        private val llArrival2: LinearLayout = view.findViewById(R.id.llArrival2)
        private val tvArrival2: TextView = view.findViewById(R.id.tvArrival2)
        private val tvPlate2: TextView = view.findViewById(R.id.tvPlate2)

        private val tvDepartureTime: TextView = view.findViewById(R.id.tvDepartureTime)

        fun bind(service: BusService, preferencesManager: PreferencesManager, walkingTime: Int?) {
            tvServiceName.text = service.serviceName
            tvDestination.text = "Hacia ${service.destination}"

            // First bus
            if (!service.arrival1.isNullOrEmpty()) {
                llArrival1.visibility = View.VISIBLE
                tvArrival1.text = service.arrival1
                tvPlate1.text = service.plate1
            } else {
                llArrival1.visibility = View.GONE
            }

            // Second bus
            if (!service.arrival2.isNullOrEmpty()) {
                llArrival2.visibility = View.VISIBLE
                tvArrival2.text = service.arrival2
                tvPlate2.text = service.plate2
            } else {
                llArrival2.visibility = View.GONE
            }

            // Departure time
            if (walkingTime != null && !service.arrival1.isNullOrEmpty()) {
                val arrivalMinutes = parseArrival(service.arrival1)
                if (arrivalMinutes != null) {
                    val departureTime = arrivalMinutes - walkingTime - 1 // 1 minute buffer
                    tvDepartureTime.visibility = View.VISIBLE
                    tvDepartureTime.text = if (departureTime > 0) {
                        "Sal en $departureTime min para llegar a tiempo"
                    } else {
                        "¡Sal ahora para alcanzar el bus!"
                    }
                } else {
                    tvDepartureTime.visibility = View.GONE
                }
            } else {
                tvDepartureTime.visibility = View.GONE
            }

            // Favorite button
            updateFavoriteIcon(preferencesManager.getFavoriteServices().contains(service.serviceName))
            btnFavorite.setOnClickListener {
                if (preferencesManager.getFavoriteServices().contains(service.serviceName)) {
                    preferencesManager.removeFavoriteService(service.serviceName)
                    updateFavoriteIcon(false)
                } else {
                    preferencesManager.addFavoriteService(service.serviceName)
                    updateFavoriteIcon(true)
                }
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            if (isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_star)
            } else {
                btnFavorite.setImageResource(R.drawable.ic_star_border)
            }
        }

        private fun parseArrival(arrivalText: String): Int? {
            return when {
                arrivalText.contains("Llegando") -> 0
                arrivalText.contains("En menos de") -> arrivalText.filter { it.isDigit() }.toIntOrNull()?.let { it - 1 } ?: 0
                arrivalText.contains("Entre") -> arrivalText.split(" ")[1].toIntOrNull()
                else -> arrivalText.filter { it.isDigit() }.toIntOrNull()
            }
        }
    }
}