package com.barangay.pantal.ui.activities.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = HouseholdAdapter(emptyList()) { household ->
            deleteHousehold(household)
        }
        binding.householdsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.householdsRecyclerView.adapter = adapter

        binding.addHouseholdButton.setOnClickListener {
            startActivity(Intent(this, AddHouseholdActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, View.NO_ID)
    }

    override fun onResume() {
        super.onResume()
        fetchHouseholds()
    }

    private fun fetchHouseholds() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["households"]
                    .select()
                    .decodeList<Household>()
                
                adapter.updateData(result.sortedBy { it.name })
            } catch (e: Exception) {
                Toast.makeText(this@HouseholdsActivity, getString(R.string.error_fetching_households, e.localizedMessage), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteHousehold(household: Household) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["households"].delete {
                    filter {
                        eq("id", household.id)
                    }
                }
                Toast.makeText(this@HouseholdsActivity, getString(R.string.household_deleted), Toast.LENGTH_SHORT).show()
                fetchHouseholds()
            } catch (e: Exception) {
                Toast.makeText(this@HouseholdsActivity, getString(R.string.error_delete_household, e.localizedMessage), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
