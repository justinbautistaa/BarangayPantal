package com.barangay.pantal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.barangay.pantal.databinding.FragmentReportsBinding
import com.barangay.pantal.model.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitReportButton.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val details = binding.reportDetailsEditText.text.toString().trim()

            if (details.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance().getReference("reports")
                val reportId = database.push().key
                if (reportId != null) {
                    val report = Report(
                        reporterId = userId,
                        details = details,
                        timestamp = System.currentTimeMillis()
                    )
                    database.child(reportId).setValue(report)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                            binding.reportDetailsEditText.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to submit report", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Please enter the report details", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "You must be logged in to submit a report", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}