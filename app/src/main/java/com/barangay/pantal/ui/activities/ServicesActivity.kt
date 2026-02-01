package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityServicesBinding
import com.barangay.pantal.model.Service
import com.barangay.pantal.ui.adapters.ServiceAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ServicesActivity : BaseActivity() {

    private lateinit var binding: ActivityServicesBinding
    private lateinit var serviceAdapter: ServiceAdapter
    private val services = mutableListOf<Service>()
    private val filteredServices = mutableListOf<Service>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        serviceAdapter = ServiceAdapter(filteredServices) { service ->
            val intent = Intent(this, RequestServiceActivity::class.java)
            intent.putExtra("serviceName", service.name)
            startActivity(intent)
        }
        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(this@ServicesActivity)
            adapter = serviceAdapter
        }

        // Load services from Firebase
        loadServicesFromFirebase()

        // Add search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterServices(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadServicesFromFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("services")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                services.clear()
                for (dataSnapshot in snapshot.children) {
                    val service = dataSnapshot.getValue(Service::class.java)
                    if (service != null) {
                        services.add(service)
                    }
                }
                filterServices("")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun filterServices(query: String) {
        filteredServices.clear()
        for (service in services) {
            if (service.name.contains(query, ignoreCase = true) || service.description.contains(query, ignoreCase = true)) {
                filteredServices.add(service)
            }
        }
        serviceAdapter.notifyDataSetChanged()
    }
}