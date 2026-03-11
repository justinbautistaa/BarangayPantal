package com.barangay.pantal.ui.adapters.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemReportBinding
import com.barangay.pantal.model.Report
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter(private var reports: List<Report>) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

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
            binding.reportTitle.text = "Report from: User ${report.reporterId.take(8)}"
            binding.reportDescription.text = report.details
            binding.reportDate.text = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(report.timestamp))
        }
    }
}
