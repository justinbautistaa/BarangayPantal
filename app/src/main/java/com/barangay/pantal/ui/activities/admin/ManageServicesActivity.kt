package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityManageServicesBinding
import com.barangay.pantal.databinding.DialogAddServiceBinding
import com.barangay.pantal.model.Service
import com.barangay.pantal.model.ServiceCatalog
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
                val servicesToShow = if (result.isEmpty()) {
                    ServiceCatalog.builtIns()
                } else {
                    ServiceCatalog.mergeWithBuiltIns(result)
                }
                adapter.updateData(servicesToShow.sortedBy { it.name })
            } catch (e: Exception) {
                Toast.makeText(this@ManageServicesActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddServiceDialog() {
        val dialogBinding = DialogAddServiceBinding.inflate(LayoutInflater.from(this))
        dialogBinding.serviceStatusEditText.setText("Active")
        dialogBinding.serviceIconEditText.setText("\u2764\uFE0F")

        AlertDialog.Builder(this)
            .setTitle("Add New Service")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val service = buildServiceFromDialog(dialogBinding)
                if (service != null) {
                    addService(service)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditServiceDialog(service: Service) {
        val dialogBinding = DialogAddServiceBinding.inflate(LayoutInflater.from(this))
        dialogBinding.serviceNameEditText.setText(service.name)
        dialogBinding.serviceDescriptionEditText.setText(service.description)
        dialogBinding.serviceCategoryEditText.setText(service.category.orEmpty())
        dialogBinding.serviceScheduleEditText.setText(service.schedule.orEmpty())
        dialogBinding.serviceContactEditText.setText(service.contactInfo.orEmpty())
        dialogBinding.serviceIconEditText.setText(service.icon.orEmpty())
        dialogBinding.serviceStatusEditText.setText(service.status.orEmpty())

        AlertDialog.Builder(this)
            .setTitle("Edit Service")
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { _, _ ->
                val updatedService = buildServiceFromDialog(dialogBinding, service.id, service.createdAt)
                if (updatedService != null) {
                    updateService(service.id, updatedService)
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
    
    private fun buildServiceFromDialog(
        dialogBinding: DialogAddServiceBinding,
        id: String = UUID.randomUUID().toString(),
        createdAt: String? = null
    ): Service? {
        val name = dialogBinding.serviceNameEditText.text.toString().trim()
        val description = dialogBinding.serviceDescriptionEditText.text.toString().trim()
        if (name.isBlank()) {
            Toast.makeText(this, "Service name is required", Toast.LENGTH_SHORT).show()
            return null
        }
        val now = java.time.Instant.now().toString()
        return Service(
            id = id,
            name = name,
            description = description,
            category = dialogBinding.serviceCategoryEditText.text.toString().trim().ifBlank { null },
            schedule = dialogBinding.serviceScheduleEditText.text.toString().trim().ifBlank { null },
            contactInfo = dialogBinding.serviceContactEditText.text.toString().trim().ifBlank { null },
            icon = dialogBinding.serviceIconEditText.text.toString().trim().ifBlank { null },
            status = dialogBinding.serviceStatusEditText.text.toString().trim().ifBlank { "Active" },
            createdAt = createdAt ?: now,
            updatedAt = now
        )
    }

    private fun addService(service: Service) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["services"].insert(service)
                Toast.makeText(this@ManageServicesActivity, "Service added", Toast.LENGTH_SHORT).show()
                fetchServices()
            } catch (e: Exception) {
                Toast.makeText(this@ManageServicesActivity, "Add failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateService(id: String, service: Service) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["services"].update({
                    set("name", service.name)
                    set("description", service.description)
                    set("category", service.category)
                    set("schedule", service.schedule)
                    set("contact_info", service.contactInfo)
                    set("icon", service.icon)
                    set("status", service.status)
                    set("updated_at", service.updatedAt)
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
