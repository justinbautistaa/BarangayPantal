package com.barangay.pantal.ui.activities.staff

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityStaffViewResidentBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.google.android.material.chip.Chip
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class StaffViewResidentActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffViewResidentBinding
    private var residentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffViewResidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        residentId = intent.getStringExtra("residentId")

        if (residentId != null) {
            loadResidentData()
        }
    }

    private fun loadResidentData() {
        lifecycleScope.launch {
            try {
                val resident = SupabaseClient.client.postgrest["residents"]
                    .select(Columns.ALL) {
                        filter {
                            eq("id", residentId!!)
                        }
                    }
                    .decodeSingleOrNull<Resident>()

                if (resident != null) {
                    binding.nameTextView.text = resident.name
                    binding.detailsTextView.text =
                        "${resident.age ?: 0} years • ${resident.gender ?: "N/A"}"
                    binding.addressTextView.text = resident.address ?: "N/A"
                    binding.occupationTextView.text = resident.occupation ?: "N/A"

                    binding.tagsChipGroup.removeAllViews()
                    if (resident.isVoter) {
                        binding.tagsChipGroup.addView(createTagChip("Voter"))
                    }
                    if (resident.isSenior) {
                        binding.tagsChipGroup.addView(createTagChip("Senior"))
                    }
                    if (resident.isPwd) {
                        binding.tagsChipGroup.addView(createTagChip("PWD"))
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@StaffViewResidentActivity,
                    "Error loading resident: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createTagChip(tag: String): Chip {
        return Chip(this).apply {
            text = tag
        }
    }
}
