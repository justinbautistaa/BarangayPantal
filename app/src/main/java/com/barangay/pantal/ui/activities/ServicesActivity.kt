package com.barangay.pantal.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityServicesBinding
import com.barangay.pantal.model.Service
import com.barangay.pantal.ui.adapters.ServiceAdapter

class ServicesActivity : BaseActivity() {

    private lateinit var binding: ActivityServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val services = listOf(
            Service("Barangay Clearance", "Request for a barangay clearance certificate."),
            Service("Business Permit", "Application for a new business permit."),
            Service("Community Tax Certificate", "Request for a Cedula.")
        )

        val adapter = ServiceAdapter(services)
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.servicesRecyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
