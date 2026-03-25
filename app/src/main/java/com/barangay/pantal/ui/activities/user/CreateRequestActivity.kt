package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityCreateRequestBinding
import com.barangay.pantal.model.Request
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.util.ValidationUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateRequestActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateRequestBinding
    private var selectedRequestType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar with a back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val requestTypes = arrayOf(
            "Certificate of Residency", 
            "Barangay Clearance", 
            "Business Permit", 
            "Certificate of Indigency"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, requestTypes)
        binding.requestTypeAutoComplete.setAdapter(adapter)
        binding.requestTypeAutoComplete.setOnItemClickListener { _, _, position, _ ->
            selectedRequestType = requestTypes[position]
            updateConditionalFields(selectedRequestType)
        }

        binding.submitRequestButton.setOnClickListener {
            submitRequest()
        }
    }

    private fun submitRequest() {
        val requestType = ValidationUtils.cleanText(binding.requestTypeAutoComplete.text.toString(), 120)
        val purpose = ValidationUtils.cleanText(binding.purposeEditText.text.toString(), 500)
        val monthlyIncomeText = ValidationUtils.cleanText(binding.monthlyIncomeEditText.text.toString(), 40)
        val businessName = ValidationUtils.cleanText(binding.businessNameEditText.text.toString(), 160)
        val businessLocation = ValidationUtils.cleanText(binding.businessLocationEditText.text.toString(), 200)
        val requiresIncome = requestType.equals("Certificate of Indigency", ignoreCase = true)
        val requiresBusinessDetails = requestType.contains("Business", ignoreCase = true)

        if (requestType.isEmpty()) {
            binding.requestTypeAutoComplete.error = "Request type is required"
            binding.requestTypeAutoComplete.requestFocus()
            return
        }

        if (purpose.isEmpty()) {
            binding.purposeEditText.error = "Purpose is required"
            binding.purposeEditText.requestFocus()
            return
        } else if (purpose.length < 5) {
            binding.purposeEditText.error = "Purpose must be at least 5 characters"
            binding.purposeEditText.requestFocus()
            return
        }

        val monthlyIncome = if (monthlyIncomeText.isNotEmpty()) monthlyIncomeText.toDoubleOrNull() else null
        if (requiresIncome && monthlyIncome == null) {
            binding.monthlyIncomeEditText.error = "Monthly income is required"
            binding.monthlyIncomeEditText.requestFocus()
            return
        }
        if (monthlyIncome != null && monthlyIncome < 0) {
            binding.monthlyIncomeEditText.error = "Monthly income must be zero or higher"
            binding.monthlyIncomeEditText.requestFocus()
            return
        }
        if (requiresBusinessDetails && businessName.isEmpty()) {
            binding.businessNameEditText.error = "Business name is required"
            binding.businessNameEditText.requestFocus()
            return
        }
        if (requiresBusinessDetails && businessLocation.isEmpty()) {
            binding.businessLocationEditText.error = "Business location is required"
            binding.businessLocationEditText.requestFocus()
            return
        }

        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user != null) {
            lifecycleScope.launch {
                try {
                    val fullName = user.userMetadata?.get("full_name")?.toString() ?: user.email ?: "Unknown User"
                    val address = user.userMetadata?.get("address")?.toString()?.trim('"').orEmpty()
                    val civilStatus = user.userMetadata?.get("civil_status")?.toString()?.trim('"').orEmpty()

                    if (fullName.isBlank()) {
                        Toast.makeText(this@CreateRequestActivity, "Profile name is required before requesting a certificate", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    if (address.isBlank()) {
                        Toast.makeText(this@CreateRequestActivity, "Profile address is required before requesting a certificate", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    if (requiresIncome && civilStatus.isBlank()) {
                        Toast.makeText(this@CreateRequestActivity, "Profile civil status is required for indigency certificates", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    
                    val request = Request(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        name = fullName,
                        type = requestType,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        purpose = purpose,
                        address = address,
                        civilStatus = civilStatus.ifBlank { null },
                        monthlyIncome = monthlyIncome,
                        businessName = businessName.ifBlank { null },
                        businessLocation = businessLocation.ifBlank { null },
                        status = "Pending",
                        timestamp = System.currentTimeMillis()
                    )

                    Log.d("CreateRequest", "Submitting request: $request")
                    val response = SupabaseClient.client.postgrest["requests"].insert(request)
                    Log.d("CreateRequest", "Response: ${response.data}")
                    
                    Toast.makeText(this@CreateRequestActivity, "Request submitted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Log.e("CreateRequest", "Error submitting request", e)
                    Toast.makeText(this@CreateRequestActivity, "Failed to submit request: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "You must be logged in to make a request", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateConditionalFields(requestType: String) {
        val requiresIncome = requestType.equals("Certificate of Indigency", ignoreCase = true)
        val requiresBusinessDetails = requestType.contains("Business", ignoreCase = true)
        binding.monthlyIncomeLayout.visibility = if (requiresIncome) View.VISIBLE else View.GONE
        binding.businessNameLayout.visibility = if (requiresBusinessDetails) View.VISIBLE else View.GONE
        binding.businessLocationLayout.visibility = if (requiresBusinessDetails) View.VISIBLE else View.GONE

        if (!requiresIncome) {
            binding.monthlyIncomeEditText.text = null
        }
        if (!requiresBusinessDetails) {
            binding.businessNameEditText.text = null
            binding.businessLocationEditText.text = null
        }
    }
}
