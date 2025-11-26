package com.example.inasafe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AlertsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvAlerts: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        rvAlerts = findViewById(R.id.rvAlerts)
        rvAlerts.layoutManager = LinearLayoutManager(this)

        database = FirebaseDatabase.getInstance().getReference("Alerts")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = mutableListOf<Alert>()
                for (alertSnapshot in snapshot.children) {
                    val alert = alertSnapshot.getValue(Alert::class.java)
                    alert?.let { alerts.add(it) }
                }
                rvAlerts.adapter = AlertAdapter(alerts.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }
}