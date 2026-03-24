package com.barangay.pantal.ui.activities.user

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityRequestsBinding
import com.barangay.pantal.model.Request
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.admin.AdminRequestsActivity
import com.barangay.pantal.ui.adapters.common.RequestAdapter
import com.google.android.material.chip.Chip
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestsActivity : BaseActivity(), RequestAdapter.OnRequestInteractionListener {

    private lateinit var binding: ActivityRequestsBinding
    private lateinit var adapter: RequestAdapter
    private val requests = mutableListOf<Request>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("user_role", "user")

        if (userRole == "admin") {
            startActivity(Intent(this, AdminRequestsActivity::class.java))
            finish()
            return
        }

        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RequestAdapter(this, mutableListOf(), this, false)
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter

        binding.fabCreateRequest.setOnClickListener {
            startActivity(Intent(this, CreateRequestActivity::class.java))
        }

        binding.requestFilterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            if(chip != null) {
                filterRequests(chip.text.toString())
            } else {
                filterRequests("All")
            }
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_requests)

        fetchRequests()
        setupRealtimeSync()
    }

    private fun fetchRequests() {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user != null) {
            lifecycleScope.launch {
                try {
                    val result = SupabaseClient.client.postgrest["requests"]
                        .select(Columns.ALL) {
                            filter {
                                eq("user_id", user.id)
                            }
                        }
                        .decodeList<Request>()
                    
                    requests.clear()
                    requests.addAll(result.sortedByDescending { it.timestamp })
                    
                    binding.chipAll.isChecked = true
                    filterRequests("All")
                } catch (e: Exception) {
                    Toast.makeText(this@RequestsActivity, "Error fetching requests: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupRealtimeSync() {
        val user = SupabaseClient.client.auth.currentUserOrNull() ?: return
        val channel = SupabaseClient.client.realtime.channel("user_requests_${user.id}")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "requests"
            filter = "user_id=eq.${user.id}"
        }

        changeFlow.onEach { action ->
            Log.d("Realtime", "Request change detected: $action")
            fetchRequests()
            
            if (action is PostgresAction.Update) {
                val status = action.record["status"]?.toString()
                if (status == "Approved" || status == "Rejected") {
                    Toast.makeText(this@RequestsActivity, "Your request was $status!", Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            channel.subscribe()
        }
    }

    private fun filterRequests(status: String) {
        val filteredList = if (status.equals("All", ignoreCase = true)) {
            requests
        } else {
            requests.filter { it.status.equals(status, ignoreCase = true) }
        }
        adapter.updateList(filteredList)
    }

    override fun onViewRequest(request: Request) {
        val authUser = SupabaseClient.client.auth.currentUserOrNull()
        val profileName = authUser?.userMetadata?.get("full_name")?.toString()?.trim('"')
            ?.takeIf { it.isNotBlank() }
            ?: authUser?.email
            ?: "Unknown User"
        val savedDate = request.timestamp?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
        } ?: request.date ?: "No date"
        val hasNameMismatch = normalizeName(profileName) != normalizeName(request.name)

        val message = buildString {
            appendLine("Saved Request Name: ${request.name}")
            appendLine("Current Profile Name: $profileName")
            appendLine("Request Type: ${request.type}")
            appendLine("Status: ${request.status}")
            appendLine("Date: $savedDate")
            appendLine()
            appendLine("Purpose: ${request.purpose ?: "No purpose provided"}")

            if (hasNameMismatch) {
                appendLine()
                append("Warning: The saved request name does not match your current profile name.")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Request Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onApproveRequest(request: Request) {
        // Not used for regular users
    }

    override fun onRejectRequest(request: Request) {
        // Not used for regular users
    }

    override fun onDownloadPdf(request: Request) {
        if (!request.pdfUrl.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.pdfUrl))
            startActivity(intent)
        } else {
            Toast.makeText(this, "PDF not available yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalizeName(value: String?): String {
        return value.orEmpty().trim().lowercase(Locale.getDefault()).replace(Regex("\\s+"), " ")
    }
}
