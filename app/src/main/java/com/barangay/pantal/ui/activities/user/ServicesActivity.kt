package com.barangay.pantal.ui.activities.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityServicesBinding
import com.barangay.pantal.model.Service
import com.barangay.pantal.model.ServiceCatalog
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.user.ServiceAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ServicesActivity : BaseActivity() {

    private lateinit var binding: ActivityServicesBinding
    private lateinit var adapter: ServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ServiceAdapter(
            services = emptyList(), 
            isAdmin = false, 
            onEditClick = { /* User cannot edit */ },
            onDeleteClick = { /* User cannot delete */ },
            onItemClick = { service: Service ->
                val intent = Intent(this, RequestServiceActivity::class.java)
                intent.putExtra("service", service)
                startActivity(intent)
            }
        )
        
        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.servicesRecyclerView.adapter = adapter

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_services)
        
        fetchServices()
    }

    private fun fetchServices() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val result = SupabaseClient.client.postgrest["services"]
                    .select()
                    .decodeList<Service>()

                val servicesToShow = if (result.isEmpty()) {
                    ServiceCatalog.builtIns()
                } else {
                    ServiceCatalog.mergeWithBuiltIns(result)
                }

                adapter.updateData(servicesToShow.sortedBy { it.name })
                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ServicesActivity, "Connection error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
