package com.example.inasafe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AdminActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val btnLogout = findViewById<ImageView>(R.id.btnLogout)
        val tvTotalReports = findViewById<TextView>(R.id.tvTotalReports)
        val tvPanicAlerts = findViewById<TextView>(R.id.tvPanicAlerts)
        val rvAdminReports = findViewById<RecyclerView>(R.id.rvAdminReports)

        rvAdminReports.layoutManager = LinearLayoutManager(this)

        database = FirebaseDatabase.getInstance().getReference("Alerts")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alerts = mutableListOf<Alert>()
                for (alertSnapshot in snapshot.children) {
                    val alert = alertSnapshot.getValue(Alert::class.java)
                    alert?.let { alerts.add(it) }
                }

                // Calculate Stats
                val total = alerts.size
                val panics = alerts.count { it.title.orEmpty().contains("PÁNICO", ignoreCase = true) }

                tvTotalReports.text = total.toString()
                tvPanicAlerts.text = panics.toString()

                // Update List
                rvAdminReports.adapter = AlertAdapter(alerts.reversed())
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

        btnLogout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}