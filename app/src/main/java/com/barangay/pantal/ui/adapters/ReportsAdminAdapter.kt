package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.model.Report

class ReportsAdminAdapter : ListAdapter<Report, ReportsAdminAdapter.ReportAdminViewHolder>(ReportAdminDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportAdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report_admin, parent, false)
        return ReportAdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportAdminViewHolder, position: Int) {
        val report = getItem(position)
        holder.bind(report)
    }

    class ReportAdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reporterIdTextView: TextView = itemView.findViewById(R.id.reporterIdTextView)
        private val reportDetailsTextView: TextView = itemView.findViewById(R.id.reportDetailsTextView)

        fun bind(report: Report) {
            reporterIdTextView.text = report.reporterId
            reportDetailsTextView.text = report.details
        }
    }
}

class ReportAdminDiffCallback : DiffUtil.ItemCallback<Report>() {
    override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
        return oldItem == newItem
    }
}
