package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityOfficialsBinding
import com.barangay.pantal.models.Official
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.user.OfficialAdapter
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class OfficialsActivity : BaseActivity() {

    private lateinit var binding: ActivityOfficialsBinding
    private lateinit var adapter: OfficialAdapter
    private val officialsList = mutableListOf<Official>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfficialsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.officialsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OfficialAdapter(officialsList)
        binding.officialsRecyclerView.adapter = adapter

        fetchOfficials()
    }

    private fun fetchOfficials() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["officials"]
                    .select()
                    .decodeList<Official>()
                
                officialsList.clear()
                officialsList.addAll(result)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@OfficialsActivity, "Error fetching officials: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}