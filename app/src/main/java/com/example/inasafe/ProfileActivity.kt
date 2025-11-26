package com.example.inasafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val btnAddContact = findViewById<Button>(R.id.btnAddContact)
        
        btnAddContact.setOnClickListener {
            showAddContactDialog()
        }

        setupBottomNavigation()
    }

    private fun showAddContactDialog() {
        val options = arrayOf("Llamada", "Mensaje", "WhatsApp")
        var selectedOption = 0

        AlertDialog.Builder(this)
            .setTitle("Añadir método de contacto")
            .setSingleChoiceItems(options, 0) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("Guardar") { dialog, _ ->
                Toast.makeText(this, "Preferencia guardada: ${options[selectedOption]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupBottomNavigation() {
        val btnNavMap = findViewById<LinearLayout>(R.id.btnNavMap)
        val btnNavList = findViewById<LinearLayout>(R.id.btnNavList)
        val btnNavBus = findViewById<LinearLayout>(R.id.btnNavBus)
        val btnNavProfile = findViewById<LinearLayout>(R.id.btnNavProfile)

        btnNavMap.setOnClickListener { startActivity(Intent(this, MapActivity::class.java)) }
        btnNavList.setOnClickListener { startActivity(Intent(this, AlertsActivity::class.java)) }
        btnNavBus.setOnClickListener { startActivity(Intent(this, BusStopsActivity::class.java)) }
        // Already here
    }
}