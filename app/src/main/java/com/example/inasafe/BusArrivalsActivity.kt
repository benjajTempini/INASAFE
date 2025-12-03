package com.example.inasafe

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.inasafe.data.PreferencesManager
import com.example.inasafe.data.network.BusService
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class BusArrivalsActivity : AppCompatActivity() {

    private lateinit var busStopRepository: BusStopRepository
    private lateinit var busArrivalsAdapter: BusArrivalsAdapter
    private lateinit var preferencesManager: PreferencesManager
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var stopLat: Double = 0.0
    private var stopLon: Double = 0.0
    private var allServices = listOf<BusService>()
    private var walkingTime: Int? = null
    private var isFilterEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_arrivals)

        busStopRepository = BusStopRepository()
        preferencesManager = PreferencesManager(this)

        val stopId = intent.getStringExtra(EXTRA_STOP_ID)
        stopLat = intent.getDoubleExtra(EXTRA_STOP_LAT, 0.0)
        stopLon = intent.getDoubleExtra(EXTRA_STOP_LON, 0.0)

        val rvBusArrivals = findViewById<RecyclerView>(R.id.rvBusArrivals)
        rvBusArrivals.layoutManager = LinearLayoutManager(this)
        busArrivalsAdapter = BusArrivalsAdapter(emptyList())
        rvBusArrivals.adapter = busArrivalsAdapter

        if (stopId == null) {
            Toast.makeText(this, "Stop ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Check if user location is passed from MapActivity
        if (intent.hasExtra(EXTRA_USER_LAT) && intent.hasExtra(EXTRA_USER_LON)) {
            val userLat = intent.getDoubleExtra(EXTRA_USER_LAT, 0.0)
            val userLon = intent.getDoubleExtra(EXTRA_USER_LON, 0.0)
            val userLocation = Location("").apply {
                latitude = userLat
                longitude = userLon
            }
            calculateWalkingTime(userLocation)
            fetchArrivals(stopId)
        } else {
            loadArrivals(stopId)
        }
    }

    private fun loadArrivals(stopId: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fetchArrivals(stopId)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { userLocation ->
            if(userLocation != null) {
                calculateWalkingTime(userLocation)
            }
            fetchArrivals(stopId)
        }.addOnFailureListener {
            fetchArrivals(stopId)
        }
    }
    
    private fun calculateWalkingTime(userLocation: Location) {
        val stopLocation = Location("").apply {
            latitude = stopLat
            longitude = stopLon
        }
        val distance = userLocation.distanceTo(stopLocation)
        walkingTime = (distance / 1.4).toInt() / 60
    }

    private fun fetchArrivals(stopId: String) {
        lifecycleScope.launch {
            try {
                val arrivalsResponse = busStopRepository.getStopArrivals(stopId)
                if (arrivalsResponse.rawData != null) {
                    title = arrivalsResponse.rawData.stopName
                    allServices = arrivalsResponse.rawData.services.items
                    applyFilter()
                } else {
                    Toast.makeText(this@BusArrivalsActivity, "No arrival information available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BusArrivalsActivity, "Failed to load arrivals", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFilter() {
        val favoriteServices = preferencesManager.getFavoriteServices()
        val servicesToShow = if (isFilterEnabled && favoriteServices.isNotEmpty()) {
            allServices.filter { favoriteServices.contains(it.serviceName) }
        } else {
            allServices
        }
        busArrivalsAdapter.updateServices(servicesToShow, walkingTime)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.arrivals_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                isFilterEnabled = !isFilterEnabled
                item.setIcon(
                    if (isFilterEnabled) R.drawable.ic_filter else R.drawable.ic_filter_list_off
                )
                applyFilter()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_STOP_ID = "EXTRA_STOP_ID"
        const val EXTRA_STOP_LAT = "EXTRA_STOP_LAT"
        const val EXTRA_STOP_LON = "EXTRA_STOP_LON"
        const val EXTRA_USER_LAT = "EXTRA_USER_LAT"
        const val EXTRA_USER_LON = "EXTRA_USER_LON"
    }
}