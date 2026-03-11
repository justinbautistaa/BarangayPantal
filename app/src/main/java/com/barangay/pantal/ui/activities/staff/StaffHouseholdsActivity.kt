package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityStaffHouseholdsBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.staff.StaffHouseholdAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class StaffHouseholdsActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffHouseholdsBinding
    private lateinit var adapter: StaffHouseholdAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffHouseholdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        adapter = StaffHouseholdAdapter(emptyList())
        binding.householdsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.householdsRecyclerView.adapter = adapter

        binding.addHouseholdButton.setOnClickListener {
            startActivity(Intent(this, StaffAddHouseholdActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_households)

        fetchHouseholds()
    }

    private fun fetchHouseholds() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["households"].select().decodeList<Household>()
                adapter.updateData(result)
            } catch (e: Exception) {
                Toast.makeText(this@StaffHouseholdsActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
