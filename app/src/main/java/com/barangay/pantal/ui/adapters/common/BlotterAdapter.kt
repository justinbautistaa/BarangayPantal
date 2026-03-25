package com.barangay.pantal.ui.adapters.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemReportBinding
import com.barangay.pantal.model.Blotter
import java.text.SimpleDateFormat
import java.util.Locale

class BlotterAdapter(
    private var blotters: List<Blotter>,
    private val onItemClick: (Blotter) -> Unit = {}
) : RecyclerView.Adapter<BlotterAdapter.BlotterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlotterViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlotterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlotterViewHolder, position: Int) {
        holder.bind(blotters[position])
    }

    override fun getItemCount(): Int = blotters.size

    fun updateData(newBlotters: List<Blotter>) {
        blotters = newBlotters
        notifyDataSetChanged()
    }

    inner class BlotterViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(blotter: Blotter) {
            val number = blotter.blotterNumber.ifBlank { blotter.id.take(8) }
            val status = blotter.status.replaceFirstChar { it.uppercase() }
            val subject = blotter.respondent.ifBlank { "General concern" }

            binding.reportTitle.text = "Report #$number • $status"
            binding.reportDescription.text =
                "Complainant: ${blotter.complainantName.ifBlank { "Resident" }}\nSubject: $subject\n${blotter.complaint}"

            binding.reportDate.text = blotter.createdAt
                ?: blotter.incidentDate
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

            itemView.setOnClickListener { onItemClick(blotter) }
        }
    }
}
