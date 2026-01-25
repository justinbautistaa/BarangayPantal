package com.barangay.pantal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HouseholdsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_households)

        // Find views
        val textViewBack = findViewById<TextView>(R.id.textViewBack)
        val editTextSearch = findViewById<EditText>(R.id.editTextSearch)
        val buttonAdd = findViewById<Button>(R.id.buttonAdd)

        // Back button
        textViewBack.setOnClickListener {
            finish()
        }

        // Add button
        buttonAdd.setOnClickListener {
            Toast.makeText(this, "Add household feature coming soon", Toast.LENGTH_SHORT).show()
        }

        // Search
        editTextSearch.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val query = editTextSearch.text.toString()
                if (query.isNotEmpty()) {
                    Toast.makeText(this, "Searching: $query", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Load households
        loadHouseholds()
    }

    private fun loadHouseholds() {
        val layoutHouseholdsList = findViewById<LinearLayout>(R.id.layoutHouseholdsList)
        val textViewHouseholdsCount = findViewById<TextView>(R.id.textViewHouseholdsCount)

        // Clear existing views
        layoutHouseholdsList.removeAllViews()

        // Sample households data
        val households = listOf(
            Household("H1", "Justin Bautista", "Block 1, Lot 5, Pantal, Dagupan City", 2),
            Household("H2", "Aira Agustin", "Block 2, Lot 12, Pantal, Dagupan City", 1),
            Household("H3", "Cyril Sarenas", "Block 3, Lot 8, Pantal, Dagupan City", 1),
            Household("H4", "Joshua Basa", "Block 1, Lot 15, Pantal, Dagupan City", 2)
        )

        // Update count
        textViewHouseholdsCount.text = "${households.size} households"

        // Add each household
        households.forEach { household ->
            val householdView = createHouseholdView(household)
            layoutHouseholdsList.addView(householdView)
        }
    }

    private fun createHouseholdView(household: Household): LinearLayout {
        // Create container
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(20, 20, 20, 20)
        container.setBackgroundColor(resources.getColor(R.color.gray_light))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 20)
        container.layoutParams = params

        // Head of Family
        val tvHead = TextView(this)
        tvHead.text = household.headOfFamily
        tvHead.textSize = 18f
        tvHead.setTypeface(null, android.graphics.Typeface.BOLD)
        container.addView(tvHead)

        // Address
        val tvAddress = TextView(this)
        tvAddress.text = "📍 ${household.address}"
        tvAddress.textSize = 14f
        tvAddress.setTextColor(resources.getColor(R.color.gray_dark))
        tvAddress.setPadding(0, 5, 0, 5)
        container.addView(tvAddress)

        // Household ID and Members
        val tvDetails = LinearLayout(this)
        tvDetails.orientation = LinearLayout.HORIZONTAL

        // ID
        val tvId = TextView(this)
        tvId.text = "ID: ${household.householdId}"
        tvId.textSize = 14f
        tvId.setPadding(0, 5, 20, 5)
        tvDetails.addView(tvId)

        // Members
        val tvMembers = TextView(this)
        tvMembers.text = "👨‍👩‍👧‍👦 ${household.totalMembers} members"
        tvMembers.textSize = 14f
        tvMembers.setPadding(0, 5, 0, 5)
        tvDetails.addView(tvMembers)

        container.addView(tvDetails)

        return container
    }

    // Data class for household
    data class Household(
        val householdId: String,
        val headOfFamily: String,
        val address: String,
        val totalMembers: Int
    )
}