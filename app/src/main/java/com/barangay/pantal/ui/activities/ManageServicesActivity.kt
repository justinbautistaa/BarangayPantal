package com.barangay.pantal.ui.activities

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityManageServicesBinding
import com.barangay.pantal.model.Service
import com.barangay.pantal.ui.adapters.ServicesAdminAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ManageServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageServicesBinding
    private lateinit var adapter: ServicesAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ServicesAdminAdapter { service ->
            deleteService(service)
        }
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.servicesRecyclerView.adapter = adapter

        binding.addServiceButton.setOnClickListener {
            showAddServiceDialog()
        }

        fetchServices()
    }

    private fun fetchServices() {
        val database = FirebaseDatabase.getInstance().getReference("services")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val services = mutableListOf<Service>()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(Service::class.java)
                    if (service != null) {
                        services.add(service)
                    }
                }
                adapter.submitList(services)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showAddServiceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_service, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.serviceNameEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.serviceDescriptionEditText)

        AlertDialog.Builder(this)
            .setTitle("Add Service")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                if (name.isNotEmpty() && description.isNotEmpty()) {
                    addService(name, description)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addService(name: String, description: String) {
        val database = FirebaseDatabase.getInstance().getReference("services")
        val serviceId = database.push().key
        if (serviceId != null) {
            val service = Service(name, description)
            database.child(serviceId).setValue(service)
        }
    }

    private fun deleteService(service: Service) {
        val database = FirebaseDatabase.getInstance().getReference("services")
        database.orderByChild("name").equalTo(service.name).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (serviceSnapshot in snapshot.children) {
                    serviceSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}