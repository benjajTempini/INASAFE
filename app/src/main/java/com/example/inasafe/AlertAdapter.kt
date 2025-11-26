package com.example.inasafe

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlertAdapter(private val alerts: List<Alert>) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.tvTitle.text = alert.title
        holder.tvDescription.text = alert.description
        holder.tvTime.text = alert.time
        holder.tvStatus.text = alert.status

        if (alert.status == "Activa") {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_active)
            holder.tvStatus.setTextColor(Color.WHITE)
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_processed)
            holder.tvStatus.setTextColor(Color.parseColor("#B71C12"))
        }
    }

    override fun getItemCount() = alerts.size
}