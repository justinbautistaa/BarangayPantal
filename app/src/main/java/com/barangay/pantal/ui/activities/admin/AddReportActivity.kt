package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAddReportBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.UUID

class AddReportActivity : BaseActivity() {

    private lateinit var binding: ActivityAddReportBinding
    private val reportTypes = listOf("General Concern", "Complaint", "Incident", "Suggestion")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        binding.btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val details = binding.etReportDetails.text.toString().trim()
        val typeInput = binding.etReportTitle.text.toString().trim()
        val user = SupabaseClient.client.auth.currentUserOrNull()

        if (details.isEmpty()) {
            Toast.makeText(this, "Please enter the report details", Toast.LENGTH_SHORT).show()
            return
        }

        if (user == null) {
            Toast.makeText(this, "You must be logged in to submit a report", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val reportType = typeInput.ifBlank {
                    binding.spinnerPriority.selectedItem?.toString().orEmpty().ifBlank { "General Concern" }
                }
                val report = Report(
                    id = UUID.randomUUID().toString(),
                    reporterId = user.id,
                    type = reportType,
                    details = details,
                    timestamp = System.currentTimeMillis()
                )
                SupabaseClient.client.postgrest["reports"].insert(report)
                Toast.makeText(this@AddReportActivity, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddReportActivity, "Failed to submit report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
