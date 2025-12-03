package com.example.inasafe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class BusStopsActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var busStopRepository: BusStopRepository
    private lateinit var busStopsAdapter: BusStopsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_stops)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        busStopRepository = BusStopRepository()

        val rvBusStops = findViewById<RecyclerView>(R.id.rvBusStops)
        rvBusStops.layoutManager = LinearLayoutManager(this)
        busStopsAdapter = BusStopsAdapter(emptyList())
        rvBusStops.adapter = busStopsAdapter

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getNearbyStops()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getNearbyStops()
            } else {
                Toast.makeText(this, "Location permission is required to find nearby bus stops", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getNearbyStops() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    lifecycleScope.launch {
                        try {
                            val stops = busStopRepository.getNearbyStops(location.latitude, location.longitude, 10)
                            busStopsAdapter.updateStops(stops)
                        } catch (e: Exception) {
                            Toast.makeText(this@BusStopsActivity, "Failed to load bus stops", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}