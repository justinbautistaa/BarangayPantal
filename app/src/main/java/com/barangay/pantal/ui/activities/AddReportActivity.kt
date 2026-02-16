package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAddReportBinding
import com.barangay.pantal.model.Priority
import com.barangay.pantal.model.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReportBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        auth = FirebaseAuth.getInstance()

        val priorities = listOf(Priority.Low.toString(), Priority.Medium.toString(), Priority.High.toString())
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        binding.btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_services -> {
                startActivity(Intent(this, ServicesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun submitReport() {
        val title = binding.etReportTitle.text.toString().trim()
        val details = binding.etReportDetails.text.toString().trim()
        val priority = binding.spinnerPriority.selectedItem.toString()

        if (title.isEmpty() || details.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "You must be logged in to submit a report", Toast.LENGTH_SHORT).show()
            return
        }

        val report = Report(
            reporterId = user.uid,
            title = title,
            details = details,
            priority = priority,
            timestamp = System.currentTimeMillis()
        )

        val database = FirebaseDatabase.getInstance().getReference("reports")
        val reportId = database.push().key
        if (reportId != null) {
            database.child(reportId).setValue(report).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to submit report: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Failed to generate a unique ID for the report", Toast.LENGTH_SHORT).show()
        }
    }
}