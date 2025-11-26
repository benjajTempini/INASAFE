package com.example.inasafe

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class MapActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val pinsContainer = findViewById<FrameLayout>(R.id.pinsContainer)
        val tvAlertCount = findViewById<TextView>(R.id.tvAlertCount)

        database = FirebaseDatabase.getInstance().getReference("Alerts")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pinsContainer.removeAllViews() // Clear old pins
                val alerts = mutableListOf<Alert>()
                for (alertSnapshot in snapshot.children) {
                    val alert = alertSnapshot.getValue(Alert::class.java)
                    alert?.let { alerts.add(it) }
                }

                val activeAlerts = alerts.filter { it.status == "Activa" }
                val count = activeAlerts.size

                // Draw pins
                pinsContainer.post {
                    val width = pinsContainer.width
                    val height = pinsContainer.height

                    for (alert in activeAlerts) {
                        if (alert.x != null && alert.y != null) {
                            val pin = ImageView(this@MapActivity)
                            pin.setImageResource(R.drawable.ic_warning)
                            
                            val params = FrameLayout.LayoutParams(60, 60) // Size of pin
                            params.leftMargin = (alert.x * width).toInt()
                            params.topMargin = (alert.y * height).toInt()
                            
                            pin.layoutParams = params
                            pinsContainer.addView(pin)
                        }
                    }
                }

                // Update bottom panel text
                if (count > 3) {
                    tvAlertCount.text = "¡PELIGRO! Alertas activas en la zona: $count"
                    tvAlertCount.setTextColor(Color.RED)
                    showDangerDialog()
                } else {
                    tvAlertCount.text = "Alertas activas en la zona: $count"
                    tvAlertCount.setTextColor(Color.BLACK)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
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
}