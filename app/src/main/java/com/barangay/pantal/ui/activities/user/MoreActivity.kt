package com.barangay.pantal.ui.activities.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.barangay.pantal.databinding.ActivityMoreBinding
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.admin.AddReportActivity

class MoreActivity : BaseActivity() {

    private lateinit var binding: ActivityMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "More"

        binding.cardReports.setOnClickListener {
            startActivity(Intent(this, AddReportActivity::class.java))
        }

        binding.cardServices.setOnClickListener {
            startActivity(Intent(this, ServicesActivity::class.java))
        }

        setupBottomNavigation(binding.bottomNavigation, View.NO_ID)
    }
}
