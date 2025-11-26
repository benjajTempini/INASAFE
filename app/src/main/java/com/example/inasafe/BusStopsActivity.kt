package com.example.inasafe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BusStopsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_stops)

        val rvBusStops = findViewById<RecyclerView>(R.id.rvBusStops)
        rvBusStops.layoutManager = LinearLayoutManager(this)
        rvBusStops.adapter = BusStopsAdapter(BusStopRepository.getNearbyStops())
    }
}