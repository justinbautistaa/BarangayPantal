package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityStaffRequestsBinding
import com.barangay.pantal.model.RequestAdmin
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.admin.PrintCertificateActivity
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.RequestsAdminAdapter
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StaffRequestsActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffRequestsBinding
    private lateinit var adapter: RequestsAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        adapter = RequestsAdminAdapter(
            onApproveClick = { request -> approveRequest(request) },
            onRejectClick = { request -> updateRequestStatus(request, "Rejected") },
            onViewClick = { request -> openCertificatePreview(request) }
        )
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter

        fetchRequests()
        setupRealtimeSync()
    }

    private fun fetchRequests() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["requests"]
                    .select()
                    .decodeList<RequestAdmin>()
                adapter.submitList(result.sortedByDescending { it.timestamp })
            } catch (e: Exception) {
                Toast.makeText(this@StaffRequestsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRealtimeSync() {
        val channel = SupabaseClient.client.realtime.channel("requests_channel")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "requests"
        }

        changeFlow.onEach {
            Log.d("Realtime", "Change detected in requests table: $it")
            fetchRequests()
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            channel.subscribe()
        }
    }

    private fun updateRequestStatus(request: RequestAdmin, status: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["requests"].update({
                    set("status", status)
                    if (status == "Rejected") {
                        set("pdf_url", null as String?)
                    }
                }) {
                    filter {
                        eq("id", request.id) 
                    }
                }
                Toast.makeText(this@StaffRequestsActivity, "Status updated to $status", Toast.LENGTH_SHORT).show()
                fetchRequests()
            } catch (e: Exception) {
                Toast.makeText(this@StaffRequestsActivity, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun approveRequest(request: RequestAdmin) {
        startActivity(Intent(this, PrintCertificateActivity::class.java).putExtra("request", request))
    }

    private fun openCertificatePreview(request: RequestAdmin) {
        startActivity(Intent(this, PrintCertificateActivity::class.java).putExtra("request", request))
    }
}
