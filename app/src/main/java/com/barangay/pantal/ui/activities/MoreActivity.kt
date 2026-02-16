package com.barangay.pantal.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.databinding.ActivityMoreBinding

class MoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        binding.cardServices.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }
    }
}
