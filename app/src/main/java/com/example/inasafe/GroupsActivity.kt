package com.example.inasafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.inasafe.data.network.NearbyBusStop
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class GroupsActivity : AppCompatActivity() {

    private lateinit var busStopRepository: BusStopRepository
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        busStopRepository = BusStopRepository()

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            loadNearbyBusStops()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                loadNearbyBusStops()
            } else {
                Toast.makeText(this, "Se necesita permiso de ubicación para encontrar grupos cercanos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadNearbyBusStops() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lifecycleScope.launch {
                    try {
                        val stops = busStopRepository.getNearbyStops(location.latitude, location.longitude, 10)
                        setupListView(stops)
                    } catch (e: Exception) {
                        Toast.makeText(this@GroupsActivity, "Error al cargar los paraderos.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListView(stops: List<NearbyBusStop>) {
        val groupsListView = findViewById<ListView>(R.id.groupsListView)
        val adapter = GroupsAdapter(this, stops)
        groupsListView.adapter = adapter

        groupsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedStop = stops[position]
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("groupName", selectedStop.id) // The group is identified by the stop ID
            startActivity(intent)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}