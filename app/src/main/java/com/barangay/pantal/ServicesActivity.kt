package com.barangay.pantal

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.barangay.pantal.databinding.ActivityServicesBinding

class ServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadServices()
    }

    private fun loadServices() {
        val services = listOf(
            Service("Barangay Clearance", "Certificate of residency required...", "Valid ID, Proof of Residency", "1-2 days"),
            Service("Barangay Indigency", "Certificate for financial assistance...", "Valid ID, Proof of Residency", "1-2 days"),
            Service("Certificate of Residency", "Proof of residence...", "Valid ID, 2 Valid IDs", "Same day"),
            Service("Business Permit", "Permit to operate business...", "DTI Registration, Valid ID", "3-5 days")
        )

        val layout = findViewById<LinearLayout>(R.id.layoutServices)
        services.forEach { service ->
            layout.addView(createServiceCard(service))
        }
    }

    private fun createServiceCard(service: Service): CardView {
        return CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            radius = 12f
            cardElevation = 2f

            val content = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)

                // Name
                addView(TextView(context).apply {
                    text = service.name
                    textSize = 18f
                    setTextColor(resources.getColor(R.color.gray_dark))
                })

                // Description
                addView(TextView(context).apply {
                    text = service.description
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.gray_text))
                    setPadding(0, 4, 0, 8)
                })

                // Requirements
                addView(TextView(context).apply {
                    text = "Requirements: ${service.requirements}"
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.gray_dark))
                    setPadding(0, 0, 0, 4)
                })

                // Processing Time
                addView(TextView(context).apply {
                    text = "Processing: ${service.processingTime}"
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.primary_blue))
                })
            }

            addView(content)
        }
    }

    data class Service(
        val name: String,
        val description: String,
        val requirements: String,
        val processingTime: String
    )
}