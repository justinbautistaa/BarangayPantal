package com.barangay.pantal.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityManageReportsBinding
import com.barangay.pantal.model.Report
import com.barangay.pantal.ui.adapters.ReportsAdminAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageReportsBinding
    private lateinit var adapter: ReportsAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ReportsAdminAdapter()
        binding.reportsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reportsRecyclerView.adapter = adapter

        fetchReports()
    }

    private fun fetchReports() {
        val database = FirebaseDatabase.getInstance().getReference("reports")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = mutableListOf<Report>()
                for (reportSnapshot in snapshot.children) {
                    val report = reportSnapshot.getValue(Report::class.java)
                    if (report != null) {
                        reports.add(report)
                    }
                }
                adapter.submitList(reports.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}