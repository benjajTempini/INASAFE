package com.example.inasafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class ReportActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid

        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etDetails = findViewById<EditText>(R.id.etDetails)
        val btnSendReport = findViewById<Button>(R.id.btnSendReport)
        val btnViewAll = findViewById<Button>(R.id.btnViewAll)

        btnSendReport.setOnClickListener {
            val description = etDescription.text.toString()
            val details = etDetails.text.toString()

            if (description.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance().getReference("Alerts")
                val alertId = database.push().key!!

                val alert = Alert(
                    title = "Reporte de Usuario",
                    description = "$description. $details",
                    time = System.currentTimeMillis().toString(),
                    status = "Activa",
                    type = "Reporte",
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    userId = userId
                )

                database.child(alertId).setValue(alert).addOnCompleteListener {
                    Toast.makeText(this, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al enviar el reporte.", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Por favor ingrese una descripción", Toast.LENGTH_SHORT).show()
            }
        }

        btnViewAll.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }
    }
}