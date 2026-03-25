package com.barangay.pantal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.FragmentReportsBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.util.ValidationUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

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
        val user = SupabaseClient.client.auth.currentSessionOrNull()?.user
        if (user != null) {
            val userId = user.id
            val details = ValidationUtils.cleanText(binding.reportDetailsEditText.text.toString(), 2000)

            if (details.isNotEmpty()) {
                if (details.length < 10) {
                    Toast.makeText(context, "Report details must be at least 10 characters", Toast.LENGTH_SHORT).show()
                    return
                }
                lifecycleScope.launch {
                    try {
                        val report = Report(
                            reporterId = userId,
                            details = details,
                            timestamp = System.currentTimeMillis()
                        )
                        SupabaseClient.client.postgrest["reports"].insert(report)
                        Toast.makeText(context, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                        binding.reportDetailsEditText.text.clear()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to submit report: ${e.message}", Toast.LENGTH_SHORT).show()
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
