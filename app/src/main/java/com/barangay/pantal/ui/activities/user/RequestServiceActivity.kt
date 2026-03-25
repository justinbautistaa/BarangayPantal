package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityRequestServiceBinding
import com.barangay.pantal.model.Request
import com.barangay.pantal.model.Service
import com.barangay.pantal.model.ServiceCatalog
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

class RequestServiceActivity : BaseActivity() {

    private lateinit var binding: ActivityRequestServiceBinding
    private var service: Service? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        service = loadServiceFromIntent()
        bindServiceDetails(service)

        binding.submitRequestButton.setOnClickListener {
            submitRequest(service?.name)
        }
    }

    private fun loadServiceFromIntent(): Service? {
        val serviceExtra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("service", Service::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("service") as? Service
        }

        if (serviceExtra != null) {
            return ServiceCatalog.enrich(serviceExtra)
        }

        val serviceName = intent.getStringExtra("service_name")
        return ServiceCatalog.findByName(serviceName) ?: serviceName?.let { Service(name = it) }
    }

    private fun bindServiceDetails(service: Service?) {
        binding.serviceNameTextView.text = service?.name ?: "Service"
        binding.serviceDescriptionTextView.text = service?.description ?: "No description available."
        binding.serviceCategoryChip.text = service?.category ?: "General"
        binding.scheduleValueTextView.text = service?.schedule ?: "Please ask the barangay office for the latest schedule."
        binding.venueValueTextView.text = service?.venue ?: "Barangay Hall"
        binding.requirementsValueTextView.text = service?.requirements
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n") { "\u2022 $it" }
            ?: "Requirements will be confirmed during assessment."
        binding.howToAvailValueTextView.text = service?.howToAvail
            ?: "Visit the barangay office and coordinate with the assigned staff for assistance."
    }

    private fun submitRequest(serviceName: String?) {
        val purpose = ValidationUtils.cleanText(binding.purposeEditText.text.toString(), 500)
        val user = SupabaseClient.client.auth.currentUserOrNull()

        if (purpose.isEmpty()) {
            Toast.makeText(this, "Please enter the purpose of your request", Toast.LENGTH_SHORT).show()
            return
        } else if (purpose.length < 5) {
            Toast.makeText(this, "Purpose must be at least 5 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (user != null && serviceName != null) {
            lifecycleScope.launch {
                try {
                    val fullName = user.userMetadata?.get("full_name")?.toString() ?: user.email ?: "Unknown User"
                    
                    val request = Request(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        name = fullName,
                        type = serviceName,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        status = "Pending",
                        purpose = purpose,
                        timestamp = System.currentTimeMillis()
                    )

                    Log.d("RequestService", "Submitting request: $request")
                    val response = SupabaseClient.client.postgrest["requests"].insert(request)
                    Log.d("RequestService", "Response: ${response.data}")
                    
                    Toast.makeText(this@RequestServiceActivity, "Service requested successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Log.e("RequestService", "Error submitting request", e)
                    Toast.makeText(this@RequestServiceActivity, "Failed to submit request: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "You must be logged in to make a request", Toast.LENGTH_SHORT).show()
        }
    }
}
