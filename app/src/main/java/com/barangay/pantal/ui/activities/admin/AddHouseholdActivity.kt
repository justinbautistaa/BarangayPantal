package com.barangay.pantal.ui.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityAddHouseholdBinding
import com.barangay.pantal.model.Household
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.UUID

class AddHouseholdActivity : BaseActivity() {

    private lateinit var binding: ActivityAddHouseholdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHouseholdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveHouseholdButton.setOnClickListener {
            saveHousehold()
        }
    }

    private fun saveHousehold() {
        val householdName = binding.householdNameEditText.text.toString().trim()
        val householdAddress = binding.householdAddressEditText.text.toString().trim()

        if (householdName.isEmpty() || householdAddress.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val newHousehold = Household(
            id = UUID.randomUUID().toString(),
            name = householdName,
            address = householdAddress,
            members = emptyList()
        )

        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["households"].insert(newHousehold)
                Toast.makeText(this@AddHouseholdActivity, "Household added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddHouseholdActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
