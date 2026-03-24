package com.barangay.pantal.ui.adapters.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ItemReportBinding
import com.barangay.pantal.model.Report
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter(
    private var reports: List<Report>,
    private val onStatusClick: (Report) -> Unit = {}
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount() = reports.size

    fun updateData(newReports: List<Report>) {
        this.reports = newReports
        notifyDataSetChanged()
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            binding.reportTitle.text = report.type.ifEmpty { "General Report" }
            binding.reportDescription.text = report.details
            binding.reportDate.text = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(report.timestamp))
            
            // Display reporter name if available
            val reporter = if (report.reporterName.isNotEmpty()) "By: ${report.reporterName}" else "ID: ${report.reporterId.take(8)}"
            // Using reportTitle as a container for both for now, or you can add a dedicated field
            binding.reportTitle.text = "${report.type.ifEmpty { "General" }} ($reporter)"

            // Handle status display (Assuming you have a status TextView in item_report or we repurpose one)
            // For now, let's just make the card clickable for status updates
            itemView.setOnClickListener { onStatusClick(report) }
        }
    }
}
