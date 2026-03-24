package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityCreateRequestBinding
import com.barangay.pantal.model.Request
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateRequestActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateRequestBinding

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

        binding.submitRequestButton.setOnClickListener {
            submitRequest()
        }
    }

    private fun submitRequest() {
        val requestType = binding.requestTypeAutoComplete.text.toString()
        val purpose = binding.purposeEditText.text.toString().trim()

        if (requestType.isEmpty()) {
            binding.requestTypeAutoComplete.error = "Request type is required"
            binding.requestTypeAutoComplete.requestFocus()
            return
        }

        if (purpose.isEmpty()) {
            binding.purposeEditText.error = "Purpose is required"
            binding.purposeEditText.requestFocus()
            return
        }

        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user != null) {
            lifecycleScope.launch {
                try {
                    val fullName = user.userMetadata?.get("full_name")?.toString() ?: user.email ?: "Unknown User"
                    
                    val request = Request(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        name = fullName,
                        type = requestType,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        purpose = purpose,
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
}
