package com.example.inasafe

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class GroupsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        val groupsListView = findViewById<ListView>(R.id.groupsListView)
        val groups = arrayOf("PB1559", "PB422", "PB2025", "PB1563")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, groups)
        groupsListView.adapter = adapter

        groupsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedGroup = groups[position]
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("groupName", selectedGroup)
            startActivity(intent)
        }
    }
}