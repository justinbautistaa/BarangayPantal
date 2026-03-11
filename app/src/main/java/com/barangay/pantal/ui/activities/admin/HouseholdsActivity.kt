package com.barangay.pantal.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityHouseholdsBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.HouseholdAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class HouseholdsActivity : BaseActivity() {

    private lateinit var binding: ActivityHouseholdsBinding
    private lateinit var adapter: HouseholdAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        adapter = HouseholdAdapter(emptyList())
        binding.householdsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.householdsRecyclerView.adapter = adapter

        binding.addHouseholdButton.setOnClickListener {
            startActivity(Intent(this, AddHouseholdActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, com.barangay.pantal.R.id.navigation_households)
        
        fetchHouseholds()
    }

    private fun fetchHouseholds() {
        // Checking if progressBar exists in the layout, otherwise using a simple visibility check or removing
        try {
            // Note: If progressBar is not in activity_households.xml, this will need a different approach
            // I'll assume it might be missing or named differently based on the build error
        } catch (e: Exception) {}

        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["households"]
                    .select()
                    .decodeList<Household>()
                
                adapter.updateData(result)
            } catch (e: Exception) {
                Toast.makeText(this@HouseholdsActivity, "Error fetching households: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
