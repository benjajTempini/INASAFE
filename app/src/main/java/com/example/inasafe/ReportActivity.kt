package com.example.inasafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ReportActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid

        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etDetails = findViewById<EditText>(R.id.etDetails)
        val btnSendReport = findViewById<Button>(R.id.btnSendReport)
        val btnViewAll = findViewById<Button>(R.id.btnViewAll)

        // Configurar Spinner
        val incidentTypes = arrayOf("Robo", "Acoso", "Accidente", "Infraestructura", "Otro")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, incidentTypes)
        spinnerType.adapter = adapter

        btnSendReport.setOnClickListener {
            val type = spinnerType.selectedItem.toString()
            val description = etDescription.text.toString()
            val details = etDetails.text.toString()

            if (description.isNotEmpty()) {
                sendReport(type, description, details, userId)
            } else {
                Toast.makeText(this, "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show()
            }
        }

        btnViewAll.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }
    }

    private fun sendReport(type: String, description: String, details: String, userId: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Se necesita permiso de ubicación para enviar un reporte.", Toast.LENGTH_LONG).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val database = FirebaseDatabase.getInstance().getReference("Alerts")
                val alertId = database.push().key!!

                val alert = Alert(
                    title = "Reporte: $type", // Usar el tipo como parte del título
                    description = "$description. $details",
                    time = System.currentTimeMillis().toString(),
                    status = "Activa",
                    type = type, // Guardar el tipo específico
                    latitude = location.latitude,
                    longitude = location.longitude,
                    userId = userId
                )

                database.child(alertId).setValue(alert).addOnCompleteListener {
                    Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al enviar el reporte.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación. Inténtelo de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}