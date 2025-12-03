package com.example.inasafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Set up the toolbar
        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(topAppBar)

        // Main buttons
        val btnPanic = findViewById<Button>(R.id.btnPanic)
        val btnReport = findViewById<Button>(R.id.btnReport)

        // Bottom navigation items
        val btnNavMap = findViewById<LinearLayout>(R.id.btnNavMap)
        val btnNavList = findViewById<LinearLayout>(R.id.btnNavList)
        val btnNavBus = findViewById<LinearLayout>(R.id.btnNavBus)

        btnPanic.setOnClickListener {
            sendPanicAlert()
        }

        btnReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }

        btnNavMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        btnNavList.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        btnNavBus.setOnClickListener {
            startActivity(Intent(this, BusStopsActivity::class.java))
        }
    }

    private fun sendPanicAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Se necesita permiso de ubicación para enviar una alerta de pánico.", Toast.LENGTH_LONG).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userId = auth.currentUser!!.uid
                val database = FirebaseDatabase.getInstance().getReference("Alerts")
                val alertId = database.push().key!!

                val alert = Alert(
                    title = "ALERTA DE PÁNICO",
                    description = "Botón de pánico activado.",
                    time = System.currentTimeMillis().toString(),
                    status = "Activa",
                    type = "Pánico",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    userId = userId
                )

                database.child(alertId).setValue(alert).addOnCompleteListener {
                    Toast.makeText(this, "¡ALERTA ENVIADA!", Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al enviar la alerta.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación. Inténtelo de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}