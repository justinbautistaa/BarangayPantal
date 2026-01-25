package com.barangay.pantal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.databinding.ActivityReportsBinding

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmitReport.setOnClickListener {
            val title = binding.etReportTitle.text.toString().trim()
            val content = binding.etReportContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Implement report submission logic
                Toast.makeText(this, "Report submitted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}