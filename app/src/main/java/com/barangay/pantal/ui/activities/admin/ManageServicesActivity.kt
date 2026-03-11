package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityManageServicesBinding
import com.barangay.pantal.model.Service
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.user.ServiceAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.UUID

class ManageServicesActivity : BaseActivity() {

    private lateinit var binding: ActivityManageServicesBinding
    private lateinit var adapter: ServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.root.findViewById(com.barangay.pantal.R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = ServiceAdapter(emptyList())
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.servicesRecyclerView.adapter = adapter

        binding.addServiceButton.setOnClickListener {
            showAddServiceDialog()
        }

        fetchServices()
    }

    private fun fetchServices() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["services"].select().decodeList<Service>()
                (binding.servicesRecyclerView.adapter as ServiceAdapter).updateData(result)
            } catch (e: Exception) {
                Toast.makeText(this@ManageServicesActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddServiceDialog() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Add New Service")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val serviceName = editText.text.toString()
                if (serviceName.isNotEmpty()) {
                    addService(serviceName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addService(name: String) {
        lifecycleScope.launch {
            try {
                val newService = Service(id = UUID.randomUUID().toString(), name = name, description = "")
                SupabaseClient.client.postgrest["services"].insert(newService)
                fetchServices()
            } catch (e: Exception) {
                Toast.makeText(this@ManageServicesActivity, "Add failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // NOTE: Edit and Delete dialogs would be added here in a similar fashion, 
    // calling Supabase update and delete functions.
}
