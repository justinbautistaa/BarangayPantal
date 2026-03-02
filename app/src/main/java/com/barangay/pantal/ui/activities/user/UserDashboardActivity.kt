package com.barangay.pantal.ui.activities.user

import android.content.Intent
import android.os.Bundle
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityUserDashboardBinding
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.user.AnnouncementsActivity
import com.barangay.pantal.ui.activities.user.RequestsActivity
import com.google.firebase.auth.FirebaseAuth

class UserDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.cardViewAllNews.setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        binding.cardRequestDocument.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }

        // It seems these views are not in the layout, so I'm commenting them out.
        // binding.cardServices.setOnClickListener {
        //     startActivity(Intent(this, ServicesActivity::class.java))
        // }
        //
        // binding.cardOfficials.setOnClickListener {
        //     startActivity(Intent(this, OfficialsActivity::class.java))
        // }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)
    }
}
