package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityServicesBinding
import com.barangay.pantal.model.Service
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.user.ServiceAdapter

class ServicesActivity : BaseActivity() {

    private lateinit var binding: ActivityServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val services = listOf(
            Service("Barangay Clearance", "Certificate of residency required for various purposes"),
            Service("Barangay Indigency", "Certificate for those who need financial assistance"),
            Service("Certificate of Residency", "Proof that you are a resident of Pantal, Dagupan City"),
            Service("Business Permit (Barangay)", "Permit required to operate a business in the barangay")
        )

        val adapter = ServiceAdapter(services)
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.servicesRecyclerView.adapter = adapter

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_services)
    }
}
