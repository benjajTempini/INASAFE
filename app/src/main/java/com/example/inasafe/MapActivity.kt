package com.example.inasafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.inasafe.data.network.NearbyBusStop
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var busStopRepository: BusStopRepository
    private lateinit var alertsRef: DatabaseReference
    private lateinit var infoCard: CardView
    private lateinit var tvInfoTitle: TextView
    private lateinit var tvInfoDesc: TextView
    private lateinit var btnCloseInfo: View
    private lateinit var btnNavigate: Button
    
    // UI Buttons
    private lateinit var btnShareLocation: FloatingActionButton
    private lateinit var btnMetroStatus: FloatingActionButton

    // Marker Lists for Filtering
    private val busStopMarkers = mutableListOf<Marker>()
    private val alertMarkers = mutableListOf<Marker>()
    private val safeSpotMarkers = mutableListOf<Marker>()
    private val metroLineOverlays = mutableListOf<Polyline>() // For Metro Lines
    private val metroStationMarkers = mutableListOf<Marker>() // For Metro Stations
    
    // Filter State
    private val checkedItems = booleanArrayOf(true, true, true, true) // Paraderos, Alertas, Zonas Seguras, Red Metro

    private val httpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        busStopRepository = BusStopRepository()
        alertsRef = FirebaseDatabase.getInstance().getReference("Alerts")

        // Initialize UI elements by ID
        infoCard = findViewById<CardView>(R.id.infoCard)
        tvInfoTitle = findViewById(R.id.tvInfoTitle)
        tvInfoDesc = findViewById(R.id.tvInfoDesc)
        btnCloseInfo = findViewById(R.id.btnCloseInfo)
        btnNavigate = findViewById(R.id.btnNavigate)
        
        btnCloseInfo.setOnClickListener {
            infoCard.visibility = View.GONE
        }

        val btnCenterMap = findViewById<FloatingActionButton>(R.id.btnCenterMap)
        btnCenterMap.setOnClickListener {
            val myLocation = locationOverlay.myLocation
            if (myLocation != null) {
                map.controller.animateTo(myLocation)
                map.controller.setZoom(18.0)
            }
        }

        val btnFilters = findViewById<FloatingActionButton>(R.id.btnFilters)
        btnFilters.setOnClickListener {
            showFilterDialog()
        }

        btnShareLocation = findViewById(R.id.btnShareLocation)
        btnShareLocation.setOnClickListener {
            shareLocation()
        }
        
        btnMetroStatus = findViewById(R.id.btnMetroStatus)
        btnMetroStatus.setOnClickListener {
            fetchMetroStatus()
        }

        // Initialize the map
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // Enable Rotation
        val rotationGestureOverlay = RotationGestureOverlay(map)
        rotationGestureOverlay.isEnabled = true
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)

        // Add Compass
        val compassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        // Add Scale Bar
        val scaleBarOverlay = ScaleBarOverlay(map)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(resources.displayMetrics.widthPixels / 2, 10)
        map.overlays.add(scaleBarOverlay)

        // Set up location overlay
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        map.overlays.add(locationOverlay)
        
        // Custom Location Icon
        val personIcon = ContextCompat.getDrawable(this, R.drawable.ic_map_character)
        val personBitmap = (personIcon as? android.graphics.drawable.BitmapDrawable)?.bitmap
        if (personBitmap != null) {
             locationOverlay.setPersonIcon(personBitmap)
        } else {
             val bitmap = android.graphics.Bitmap.createBitmap(
                 personIcon!!.intrinsicWidth, 
                 personIcon.intrinsicHeight, 
                 android.graphics.Bitmap.Config.ARGB_8888
             )
             val canvas = android.graphics.Canvas(bitmap)
             personIcon.setBounds(0, 0, canvas.width, canvas.height)
             personIcon.draw(canvas)
             locationOverlay.setPersonIcon(bitmap)
        }

        val mapController = map.controller
        mapController.setZoom(18.0)

        checkLocationPermission()
        listenForAlerts()
        addSafeSpots()
        fetchAndDrawMetroLines()
    }
    
    private fun fetchAndDrawMetroLines() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // MOCK COORDINATES FOR ALL METRO LINES
                val mockLines = listOf(
                    // Line 1 (Red)
                    Triple("L1", "#E53935", listOf(
                        GeoPoint(-33.444, -70.740), // San Pablo
                        GeoPoint(-33.455, -70.700),
                        GeoPoint(-33.445, -70.660),
                        GeoPoint(-33.440, -70.650), // La Moneda
                        GeoPoint(-33.435, -70.640), // Baquedano
                        GeoPoint(-33.420, -70.600), // Tobalaba
                        GeoPoint(-33.410, -70.570), // Escuela Militar
                        GeoPoint(-33.400, -70.540)  // Los Dominicos
                    )),
                    // Line 2 (Yellow)
                    Triple("L2", "#FFEB3B", listOf(
                        GeoPoint(-33.370, -70.640), // Vespucio Norte
                        GeoPoint(-33.400, -70.650),
                        GeoPoint(-33.430, -70.655), // Cal y Canto
                        GeoPoint(-33.450, -70.660), // Los Heroes
                        GeoPoint(-33.480, -70.650), // Franklin
                        GeoPoint(-33.520, -70.660), // La Cisterna
                        GeoPoint(-33.550, -70.660)  // Hospital El Pino
                    )),
                    // Line 3 (Brown)
                    Triple("L3", "#8D6E63", listOf(
                        GeoPoint(-33.360, -70.700), // Los Libertadores
                        GeoPoint(-33.400, -70.670),
                        GeoPoint(-33.430, -70.655), // Cal y Canto
                        GeoPoint(-33.437, -70.650), // Plaza de Armas
                        GeoPoint(-33.445, -70.640), // U de Chile
                        GeoPoint(-33.450, -70.620), // Matta
                        GeoPoint(-33.460, -70.620), // Irarrazaval
                        GeoPoint(-33.470, -70.590), // Plaza Egaña
                        GeoPoint(-33.460, -70.550)  // F. Castillo Velasco
                    )),
                    // Line 4 (Blue)
                    Triple("L4", "#1E88E5", listOf(
                        GeoPoint(-33.420, -70.600), // Tobalaba
                        GeoPoint(-33.430, -70.580), // Bilbao
                        GeoPoint(-33.470, -70.590), // Plaza Egaña
                        GeoPoint(-33.500, -70.590), // Macul
                        GeoPoint(-33.530, -70.595), // Vicuña Mackenna
                        GeoPoint(-33.570, -70.580), // Vicente Valdes
                        GeoPoint(-33.590, -70.570), // Elisa Correa
                        GeoPoint(-33.610, -70.560)  // Plaza Puente Alto
                    )),
                    // Line 4A (Blue/Light Blue)
                    Triple("L4A", "#42A5F5", listOf(
                        GeoPoint(-33.530, -70.595), // Vicuña Mackenna
                        GeoPoint(-33.525, -70.620), // Santa Julia
                        GeoPoint(-33.520, -70.660)  // La Cisterna
                    )),
                    // Line 5 (Green)
                    Triple("L5", "#43A047", listOf(
                        GeoPoint(-33.510, -70.750), // Plaza Maipu
                        GeoPoint(-33.470, -70.700),
                        GeoPoint(-33.440, -70.680), // Quinta Normal
                        GeoPoint(-33.437, -70.650), // Plaza de Armas
                        GeoPoint(-33.435, -70.630), // Baquedano
                        GeoPoint(-33.460, -70.620), // Irarrázaval
                        GeoPoint(-33.500, -70.600),
                        GeoPoint(-33.570, -70.580)  // Vicente Valdés
                    )),
                    // Line 6 (Purple)
                    Triple("L6", "#9C27B0", listOf(
                        GeoPoint(-33.500, -70.720), // Cerrillos
                        GeoPoint(-33.480, -70.650), // Franklin
                        GeoPoint(-33.460, -70.630), // Ñuble
                        GeoPoint(-33.430, -70.610), // Estadio Nacional
                        GeoPoint(-33.420, -70.605), // Ñuñoa
                        GeoPoint(-33.415, -70.600)  // Los Leones
                    ))
                )
                
                // MOCK KEY STATIONS (Terminals & Combinations)
                val mockStations = listOf(
                    // Terminals
                    Triple("Los Dominicos", "L1", GeoPoint(-33.400, -70.540)),
                    Triple("San Pablo", "L1/L5", GeoPoint(-33.444, -70.740)),
                    Triple("Vespucio Norte", "L2", GeoPoint(-33.370, -70.640)),
                    Triple("Hospital El Pino", "L2", GeoPoint(-33.550, -70.660)), // Approx
                    Triple("Los Libertadores", "L3", GeoPoint(-33.360, -70.700)),
                    Triple("F. Castillo Velasco", "L3", GeoPoint(-33.460, -70.550)),
                    Triple("Plaza Puente Alto", "L4", GeoPoint(-33.610, -70.560)),
                    Triple("Plaza de Maipú", "L5", GeoPoint(-33.510, -70.750)),
                    Triple("Cerrillos", "L6", GeoPoint(-33.500, -70.720)),
                    
                    // Combinations (Major)
                    Triple("Baquedano", "L1/L5", GeoPoint(-33.435, -70.635)),
                    Triple("Los Héroes", "L1/L2", GeoPoint(-33.445, -70.660)),
                    Triple("Tobalaba", "L1/L4", GeoPoint(-33.417, -70.600)),
                    Triple("Vicente Valdés", "L4/L5", GeoPoint(-33.570, -70.580)),
                    Triple("La Cisterna", "L2/L4A", GeoPoint(-33.520, -70.660)),
                    Triple("Franklin", "L2/L6", GeoPoint(-33.480, -70.650)),
                    Triple("Ñuble", "L5/L6", GeoPoint(-33.460, -70.630)),
                    Triple("Plaza Egaña", "L3/L4", GeoPoint(-33.470, -70.590)),
                    Triple("U. de Chile", "L1/L3", GeoPoint(-33.445, -70.650)),
                    Triple("Santa Ana", "L2/L5", GeoPoint(-33.438, -70.660)), // Approx
                    Triple("Cal y Canto", "L2/L3", GeoPoint(-33.430, -70.655))
                )

                withContext(Dispatchers.Main) {
                    metroLineOverlays.forEach { map.overlays.remove(it) }
                    metroStationMarkers.forEach { map.overlays.remove(it) }
                    metroLineOverlays.clear()
                    metroStationMarkers.clear()

                    // Draw Lines
                    for ((name, color, points) in mockLines) {
                        val polyline = Polyline()
                        polyline.color = Color.parseColor(color)
                        polyline.width = 15f 
                        polyline.setPoints(points)
                        polyline.title = "Metro $name"
                        
                        metroLineOverlays.add(polyline)
                        map.overlays.add(polyline)
                    }
                    
                    // Draw Stations
                    for ((name, line, point) in mockStations) {
                        val marker = Marker(map)
                        marker.position = point
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        marker.icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.ic_subway) // Reuse subway icon
                        // Or make it smaller? For now standard size.
                        marker.title = name
                        marker.subDescription = "Estación $line"
                        
                        marker.setOnMarkerClickListener { _, _ ->
                            showInfoCard(name, "Estación de Metro - $line", false, null, point)
                            true
                        }
                        
                        metroStationMarkers.add(marker)
                        map.overlays.add(marker)
                    }
                    
                    applyFilters()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun fetchMetroStatus() {
        Toast.makeText(this, "Consultando estado del Metro...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.xor.cl/red/metro-network")
                    .build()
                val response = httpClient.newCall(request).execute()
                val responseData = response.body?.string()

                if (response.isSuccessful && responseData != null) {
                    val json = Json { ignoreUnknownKeys = true }
                    val jsonObject = json.parseToJsonElement(responseData).jsonObject
                    
                    val statusSummary = StringBuilder()
                    var hasIssues = false
                    val lines = listOf("l1", "l2", "l3", "l4", "l4a", "l5", "l6")
                    
                    for (lineId in lines) {
                        val lineData = jsonObject[lineId]?.jsonObject
                        if (lineData != null) {
                            val status = lineData["status"]?.jsonPrimitive?.content
                            if (status != "0") { 
                                hasIssues = true
                                statusSummary.append("• Línea ${lineId.uppercase()}: Problemas reportados\n")
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (!hasIssues) {
                            showInfoCard("Metro de Santiago", "Todas las líneas operando normalmente.", false, null)
                        } else {
                            showInfoCard("Estado del Metro", "Se reportan problemas:\n$statusSummary", false, null)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MapActivity, "Error al obtener estado del metro", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun shareLocation() {
        val myLocation = locationOverlay.myLocation
        if (myLocation != null) {
            val lat = myLocation.latitude
            val lon = myLocation.longitude
            val mapLink = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
            val message = "¡Hola! Estoy aquí sigue mi ubicacion en Google Maps: $mapLink (vía INA SAFE)"
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir ubicación vía"))
        } else {
            Toast.makeText(this, "Obteniendo ubicación, intenta de nuevo...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFilterDialog() {
        val layers = arrayOf("Paraderos", "Alertas de Seguridad", "Zonas Seguras", "Red de Metro")

        AlertDialog.Builder(this)
            .setTitle("Capas del Mapa")
            .setMultiChoiceItems(layers, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Aplicar") { dialog, _ ->
                applyFilters()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun applyFilters() {
        busStopMarkers.forEach { it.isEnabled = checkedItems[0] }
        alertMarkers.forEach { it.isEnabled = checkedItems[1] }
        safeSpotMarkers.forEach { it.isEnabled = checkedItems[2] }
        metroLineOverlays.forEach { it.isEnabled = checkedItems[3] }
        metroStationMarkers.forEach { it.isEnabled = checkedItems[3] } // Toggle stations with lines
        map.invalidate() 
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
        busStopMarkers.forEach { map.overlays.remove(it) } 
        busStopMarkers.clear()
        for (stop in stops) {
            val stopMarker = Marker(map)
            stopMarker.position = GeoPoint(stop.lat, stop.lon)
            stopMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            stopMarker.title = stop.name
            stopMarker.subDescription = stop.id
            stopMarker.icon = ContextCompat.getDrawable(this, R.drawable.ic_bus_stop)
            
            stopMarker.setOnMarkerClickListener { _, _ ->
                showInfoCard(stop.name, "Paradero: ${stop.id}\nToca para ver llegadas", true, {
                    val myLocation = locationOverlay.myLocation
                    if (myLocation != null) {
                        val intent = Intent(this, BusArrivalsActivity::class.java).apply {
                            putExtra(BusArrivalsActivity.EXTRA_STOP_ID, stop.id)
                            putExtra(BusArrivalsActivity.EXTRA_STOP_LAT, stop.lat)
                            putExtra(BusArrivalsActivity.EXTRA_STOP_LON, stop.lon)
                            putExtra(BusArrivalsActivity.EXTRA_USER_LAT, myLocation.latitude)
                            putExtra(BusArrivalsActivity.EXTRA_USER_LON, myLocation.longitude)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No se pudo obtener tu ubicación actual.", Toast.LENGTH_SHORT).show()
                    }
                }, null)
                true
            }
            busStopMarkers.add(stopMarker)
            map.overlays.add(stopMarker)
        }
        applyFilters() 
    }
    
    private fun showInfoCard(title: String, desc: String, isClickable: Boolean = false, onClick: (() -> Unit)? = null, navGeoPoint: GeoPoint? = null) {
        tvInfoTitle.text = title
        tvInfoDesc.text = desc
        infoCard.visibility = View.VISIBLE
        
        if (isClickable && onClick != null) {
            infoCard.setOnClickListener { onClick() }
        } else {
            infoCard.setOnClickListener(null)
        }

        if (navGeoPoint != null) {
            btnNavigate.visibility = View.VISIBLE
            btnNavigate.setOnClickListener {
                val gmmIntentUri = Uri.parse("google.navigation:q=${navGeoPoint.latitude},${navGeoPoint.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${navGeoPoint.latitude},${navGeoPoint.longitude}"))
                    startActivity(browserIntent)
                }
            }
        } else {
            btnNavigate.visibility = View.GONE
        }
    }

    private fun listenForAlerts() {
        alertsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                alertMarkers.forEach { map.overlays.remove(it) }
                alertMarkers.clear()

                val activeAlerts = snapshot.children.mapNotNull { it.getValue(Alert::class.java) }
                    .filter { it.status == "Activa" }

                for (alert in activeAlerts) {
                    if (alert.latitude != null && alert.longitude != null) {
                        val alertMarker = Marker(map)
                        alertMarker.position = GeoPoint(alert.latitude, alert.longitude)
                        alertMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        
                        val title = alert.title ?: "Alerta"
                        val description = alert.description ?: "Sin descripción"
                        val type = alert.type ?: "General"

                        alertMarker.title = title
                        alertMarker.subDescription = description
                        alertMarker.icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.ic_warning)
                        
                        alertMarker.setOnMarkerClickListener { _, _ ->
                            showInfoCard(title, "$description\n$type", false, null, null)
                            true
                        }
                        
                        alertMarkers.add(alertMarker)
                        map.overlays.add(alertMarker)
                    }
                }
                applyFilters() 
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapActivity, "Failed to load alerts", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun addSafeSpots() {
        safeSpotMarkers.forEach { map.overlays.remove(it) } 
        safeSpotMarkers.clear()

        val policeStation = Marker(map)
        policeStation.position = GeoPoint(-33.4489, -70.6693)
        policeStation.title = "Comisaría Central"
        policeStation.icon = ContextCompat.getDrawable(this, R.drawable.ic_police)
        policeStation.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        policeStation.setOnMarkerClickListener { _, _ ->
             showInfoCard("Comisaría Central", "Zona Segura\nAbierto 24/7", false, null, policeStation.position)
             true
        }
        safeSpotMarkers.add(policeStation)
        map.overlays.add(policeStation)

        val hospital = Marker(map)
        hospital.position = GeoPoint(-33.4243, -70.6277)
        hospital.title = "Hospital Público"
        hospital.icon = ContextCompat.getDrawable(this, R.drawable.ic_hospital)
        hospital.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        hospital.setOnMarkerClickListener { _, _ ->
             showInfoCard("Hospital Público", "Zona Segura\nUrgencias", false, null, hospital.position)
             true
        }
        safeSpotMarkers.add(hospital)
        map.overlays.add(hospital)
        
        applyFilters() 
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