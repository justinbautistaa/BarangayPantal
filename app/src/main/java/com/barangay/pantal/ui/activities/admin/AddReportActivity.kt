package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityAddReportBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.util.ValidationUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class AddReportActivity : BaseActivity() {

    private lateinit var binding: ActivityAddReportBinding
    private val priorityOptions = listOf("normal", "high", "low")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            priorityOptions.map { it.replaceFirstChar { ch -> ch.uppercase() } }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        binding.btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val respondent = ValidationUtils.cleanText(binding.etRespondent.text.toString(), 120)
        val incidentDate = ValidationUtils.cleanText(binding.etIncidentDate.text.toString(), 20)
        val incidentLocation = ValidationUtils.cleanText(binding.etIncidentLocation.text.toString(), 160)
        val witnesses = ValidationUtils.cleanText(binding.etWitnesses.text.toString(), 500)
        val complaint = ValidationUtils.cleanText(binding.etReportDetails.text.toString(), 2000)
        val user = SupabaseClient.client.auth.currentUserOrNull()

        binding.etRespondent.error = null
        binding.etIncidentDate.error = null
        binding.etIncidentLocation.error = null
        binding.etWitnesses.error = null
        binding.etReportDetails.error = null

        if (incidentDate.isBlank()) {
            binding.etIncidentDate.error = "Incident date is required"
            return
        }

        if (runCatching { LocalDate.parse(incidentDate) }.getOrNull() == null) {
            binding.etIncidentDate.error = "Use YYYY-MM-DD format"
            return
        }

        if (incidentLocation.length < 5) {
            binding.etIncidentLocation.error = "Incident location must be at least 5 characters"
            return
        }

        if (complaint.length < 10) {
            binding.etReportDetails.error = "Complaint details must be at least 10 characters"
            return
        }

        if (respondent.isNotBlank() && respondent.length < 3) {
            binding.etRespondent.error = "Subject or person involved must be at least 3 characters"
            return
        }

        if (respondent.isNotBlank() && !ValidationUtils.isLettersOnlyNameList(respondent)) {
            binding.etRespondent.error = "Subject or person involved must contain letters only"
            return
        }

        if (witnesses.isNotBlank() && witnesses.length < 3) {
            binding.etWitnesses.error = "Witnesses must be at least 3 characters"
            return
        }

        if (witnesses.isNotBlank() && !ValidationUtils.isLettersOnlyNameList(witnesses)) {
            binding.etWitnesses.error = "Witnesses must contain letters only"
            return
        }

        if (user == null) {
            Toast.makeText(this, "You must be logged in to submit a report", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val complainantName = user.userMetadata?.get("full_name")?.toString()
                    ?: user.userMetadata?.get("name")?.toString()
                    ?: user.email?.substringBefore("@")
                    ?: "Resident"

                val extraDetails = buildString {
                    appendLine("Incident Date: $incidentDate")
                    appendLine("Incident Location: $incidentLocation")
                    if (witnesses.isNotBlank()) {
                        appendLine("Witnesses: $witnesses")
                    }
                    appendLine("Priority: ${priorityOptions[binding.spinnerPriority.selectedItemPosition]}")
                    appendLine()
                    append(complaint)
                }

                val report = Report(
                    id = UUID.randomUUID().toString(),
                    reporterId = user.id,
                    reporterName = complainantName,
                    type = respondent.ifBlank { "Resident Report" },
                    details = extraDetails,
                    status = "Pending",
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
