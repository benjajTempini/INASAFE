package com.example.inasafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid

        // Main buttons
        val btnPanic = findViewById<Button>(R.id.btnPanic)
        val btnReport = findViewById<Button>(R.id.btnReport)

        // Bottom navigation items
        val btnNavMap = findViewById<LinearLayout>(R.id.btnNavMap)
        val btnNavList = findViewById<LinearLayout>(R.id.btnNavList)
        val btnNavBus = findViewById<LinearLayout>(R.id.btnNavBus)

        btnPanic.setOnClickListener {
            val database = FirebaseDatabase.getInstance().getReference("Alerts")
            val alertId = database.push().key!!

            val alert = Alert(
                title = "ALERTA DE PÁNICO",
                description = "Botón de pánico activado.",
                time = System.currentTimeMillis().toString(),
                status = "Activa",
                type = "Pánico",
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                userId = userId
            )

            database.child(alertId).setValue(alert).addOnCompleteListener {
                Toast.makeText(this, "¡ALERTA ENVIADA!", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Error al enviar la alerta.", Toast.LENGTH_SHORT).show()
            }
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
}