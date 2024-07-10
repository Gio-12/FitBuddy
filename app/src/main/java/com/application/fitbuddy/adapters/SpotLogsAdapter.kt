package com.application.fitbuddy.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.application.fitbuddy.R
import com.application.fitbuddy.models.SpotLog

class SpotLogsAdapter(private val spotLogs: List<SpotLog>) : RecyclerView.Adapter<SpotLogsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logText: TextView = view.findViewById(R.id.log_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_spot_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val spotLog = spotLogs[position]
        holder.logText.text = spotLog.toString() // Customize this to show relevant spot log information
    }

    override fun getItemCount(): Int {
        return spotLogs.size
    }
}
