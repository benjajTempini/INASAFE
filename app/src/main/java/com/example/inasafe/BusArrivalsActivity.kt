package com.example.inasafe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class BusArrivalsActivity : AppCompatActivity() {

    private lateinit var busStopRepository: BusStopRepository
    private lateinit var busArrivalsAdapter: BusArrivalsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_arrivals)

        busStopRepository = BusStopRepository()
        val stopId = intent.getStringExtra(EXTRA_STOP_ID)

        val rvBusArrivals = findViewById<RecyclerView>(R.id.rvBusArrivals)
        rvBusArrivals.layoutManager = LinearLayoutManager(this)
        busArrivalsAdapter = BusArrivalsAdapter(emptyList())
        rvBusArrivals.adapter = busArrivalsAdapter

        if (stopId == null) {
            Toast.makeText(this, "Stop ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val arrivalsResponse = busStopRepository.getStopArrivals(stopId)
                if (arrivalsResponse.rawData != null) {
                    title = arrivalsResponse.rawData.stopName
                    busArrivalsAdapter.updateServices(arrivalsResponse.rawData.services.items)
                } else {
                    Toast.makeText(this@BusArrivalsActivity, "No arrival information available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BusArrivalsActivity, "Failed to load arrivals", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_STOP_ID = "EXTRA_STOP_ID"
    }
}