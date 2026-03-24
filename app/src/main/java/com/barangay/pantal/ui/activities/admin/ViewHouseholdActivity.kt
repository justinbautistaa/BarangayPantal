package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityViewHouseholdBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.admin.HouseholdMemberAdapter
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class ViewHouseholdActivity : BaseActivity() {

    private lateinit var binding: ActivityViewHouseholdBinding
    private var householdId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewHouseholdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        householdId = intent.getStringExtra("householdId")

        binding.toolbar.setNavigationOnClickListener { finish() }

        if (householdId != null) {
            fetchHouseholdData()
        }
    }

    private fun fetchHouseholdData() {
        lifecycleScope.launch {
            try {
                val household = SupabaseClient.client.postgrest["households"]
                    .select(Columns.ALL) {
                        filter {
                            eq("id", householdId!!)
                        }
                    }
                    .decodeSingleOrNull<Household>()
                
                if (household != null) {
                    binding.householdName.text = household.name
                    binding.householdAddress.text = household.address
                    binding.membersRecyclerView.adapter = HouseholdMemberAdapter(household.members)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewHouseholdActivity, "Error fetching household: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}