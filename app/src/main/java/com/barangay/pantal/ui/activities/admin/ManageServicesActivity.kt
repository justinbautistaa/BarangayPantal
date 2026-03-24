package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ServiceAdapter(emptyList(), isAdmin = true, 
            onEditClick = { service -> showEditServiceDialog(service) },
            onDeleteClick = { service -> showDeleteConfirmation(service) }
        )
        
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.servicesRecyclerView.adapter = adapter

        binding.addServiceButton.setOnClickListener {
            showAddServiceDialog()
        }

        fetchServices()
    }

    override fun onResume() {
        super.onResume()
        fetchServices()
    }

    private fun fetchServices() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["services"].select().decodeList<Service>()
                adapter.updateData(result.sortedBy { it.name })
            } catch (e: Exception) {
                Toast.makeText(this@ManageServicesActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddServiceDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 10)

        val nameEditText = EditText(this)
        nameEditText.hint = "Service Name"
        layout.addView(nameEditText)

        val descEditText = EditText(this)
        descEditText.hint = "Description"
        layout.addView(descEditText)

        AlertDialog.Builder(this)
            .setTitle("Add New Service")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString()
                val desc = descEditText.text.toString()
                if (name.isNotEmpty()) {
                    addService(name, desc)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditServiceDialog(service: Service) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 10)

        val nameEditText = EditText(this)
        nameEditText.setText(service.name)
        nameEditText.hint = "Service Name"
        layout.addView(nameEditText)

        val descEditText = EditText(this)
        descEditText.setText(service.description)
        descEditText.hint = "Description"
        layout.addView(descEditText)

        AlertDialog.Builder(this)
            .setTitle("Edit Service")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val name = nameEditText.text.toString()
                val desc = descEditText.text.toString()
                if (name.isNotEmpty()) {
                    updateService(service.id, name, desc)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(service: Service) {
        AlertDialog.Builder(this)
            .setTitle("Delete Service")
            .setMessage("Are you sure you want to delete ${service.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteService(service.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addService(name: String, description: String) {
        lifecycleScope.launch {
            try {
                val newService = Service(id = UUID.randomUUID().toString(), name = name, description = description)
                SupabaseClient.client.postgrest["services"].insert(newService)
                Toast.makeText(this@ManageServicesActivity, "Service added", Toast.LENGTH_SHORT).show()
                fetchServices()
            } catch (e: Exception) {
                Toast.makeText(this@ManageServicesActivity, "Add failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateService(id: String, name: String, description: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["services"].update({
                    set("name", name)
                    set("description", description)
                }) {
                    filter { eq("id", id) }
                }
                Toast.makeText(this@ManageServicesActivity, "Service updated", Toast.LENGTH_SHORT).show()
                fetchServices()
            } catch (e: Exception) {
                Toast.makeText(this@ManageServicesActivity, "Update failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteService(id: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["services"].delete {
                    filter { eq("id", id) }
                }
                Toast.makeText(this@ManageServicesActivity, "Service deleted", Toast.LENGTH_SHORT).show()
                fetchServices()
            } catch (e: Exception) {
                Toast.makeText(this@ManageServicesActivity, "Delete failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
