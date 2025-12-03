package com.example.inasafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.inasafe.data.network.NearbyBusStop
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var busStopRepository: BusStopRepository
    private lateinit var alertsRef: DatabaseReference
    private val alertMarkers = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        busStopRepository = BusStopRepository()
        alertsRef = FirebaseDatabase.getInstance().getReference("Alerts")

        // Initialize the map
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // Set up location overlay
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        map.overlays.add(locationOverlay)

        val mapController = map.controller
        mapController.setZoom(18.0)

        checkLocationPermission()
        listenForAlerts()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            setupMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                setupMap()
            } else {
                Toast.makeText(this, "Location permission is required to show the map", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupMap() {
        locationOverlay.enableMyLocation()
        locationOverlay.runOnFirstFix {
            runOnUiThread {
                val myLocation = locationOverlay.myLocation
                if (myLocation != null) {
                    map.controller.animateTo(myLocation)
                    loadNearbyBusStops(myLocation)
                }
            }
        }
    }

    private fun loadNearbyBusStops(location: GeoPoint) {
        lifecycleScope.launch {
            try {
                val stops = busStopRepository.getNearbyStops(location.latitude, location.longitude, 20)
                addBusStopMarkers(stops)
            } catch (e: Exception) {
                Toast.makeText(this@MapActivity, "Failed to load bus stops", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addBusStopMarkers(stops: List<NearbyBusStop>) {
        for (stop in stops) {
            val stopMarker = Marker(map)
            stopMarker.position = GeoPoint(stop.lat, stop.lon)
            stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            stopMarker.title = stop.name
            stopMarker.subDescription = stop.id
            // Using the newly created bus stop icon
            stopMarker.icon = ContextCompat.getDrawable(this, R.drawable.ic_bus_stop)
            
            stopMarker.setOnMarkerClickListener { marker, _ ->
                // Show info window
                marker.showInfoWindow()
                
                // Open BusArrivalsActivity
                val intent = Intent(this, BusArrivalsActivity::class.java).apply {
                    putExtra(BusArrivalsActivity.EXTRA_STOP_ID, stop.id)
                }
                startActivity(intent)
                true
            }
            map.overlays.add(stopMarker)
        }
        map.invalidate() // Refresh the map
    }

    private fun listenForAlerts() {
        alertsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear old alert markers
                alertMarkers.forEach { map.overlays.remove(it) }
                alertMarkers.clear()

                val activeAlerts = snapshot.children.mapNotNull { it.getValue(Alert::class.java) }
                    .filter { it.status == "Activa" }

                for (alert in activeAlerts) {
                    if (alert.latitude != null && alert.longitude != null) {
                        val alertMarker = Marker(map)
                        alertMarker.position = GeoPoint(alert.latitude, alert.longitude)
                        alertMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        alertMarker.title = alert.title
                        alertMarker.subDescription = alert.description
                        alertMarker.icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.ic_warning)
                        alertMarkers.add(alertMarker)
                        map.overlays.add(alertMarker)
                    }
                }
                map.invalidate() // Refresh the map

                if (activeAlerts.size > 3) {
                    showDangerDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapActivity, "Failed to load alerts", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDangerDialog() {
        AlertDialog.Builder(this)
            .setTitle("¡ALERTA DE SEGURIDAD!")
            .setMessage("Se han detectado múltiples incidentes en la zona.\n\nCONSEJOS DE SEGURIDAD:\n- Evite caminar solo.\n- Guarde su celular y objetos de valor.\n- Manténgase en zonas iluminadas.\n- Contacte a seguridad si ve algo sospechoso.")
            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .setIcon(R.drawable.ic_warning)
            .show()
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationOverlay.enableMyLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        locationOverlay.disableMyLocation()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}