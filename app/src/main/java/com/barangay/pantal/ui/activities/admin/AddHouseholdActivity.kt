package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityAddHouseholdBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.util.ValidationUtils
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.UUID

class AddHouseholdActivity : BaseActivity() {

    private lateinit var binding: ActivityAddHouseholdBinding
    private var householdId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHouseholdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        householdId = intent.getStringExtra("household_id")

        if (householdId != null) {
            binding.toolbar.title = "Edit Household"
            binding.saveHouseholdButton.text = "Update Household"
            loadHouseholdData()
        }

        binding.saveHouseholdButton.setOnClickListener {
            saveHousehold()
        }
    }

    private fun loadHouseholdData() {
        lifecycleScope.launch {
            try {
                val household = SupabaseClient.client.postgrest["households"].select {
                    filter {
                        eq("id", householdId!!)
                    }
                }.decodeSingleOrNull<Household>()

                household?.let {
                    binding.householdNameEditText.setText(it.name)
                    binding.householdAddressEditText.setText(it.address)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AddHouseholdActivity, "Error loading: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveHousehold() {
        val householdName = ValidationUtils.cleanText(binding.householdNameEditText.text.toString(), 120)
        val householdAddress = ValidationUtils.cleanText(binding.householdAddressEditText.text.toString(), 255)

        if (householdName.length < 3 || householdAddress.length < 8) {
            Toast.makeText(this, "Enter a valid household name and address", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (householdId == null) {
                    SupabaseClient.client.postgrest["households"].insert(
                        mapOf(
                            "id" to UUID.randomUUID().toString(),
                            "name" to householdName,
                            "address" to householdAddress
                        )
                    )
                    Toast.makeText(this@AddHouseholdActivity, "Household added successfully", Toast.LENGTH_SHORT).show()
                } else {
                    SupabaseClient.client.postgrest["households"].update({
                        set("name", householdName)
                        set("address", householdAddress)
                    }) {
                        filter {
                            eq("id", householdId!!)
                        }
                    }
                    Toast.makeText(this@AddHouseholdActivity, "Household updated successfully", Toast.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddHouseholdActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
