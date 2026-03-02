package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityProfileBinding
import com.barangay.pantal.ui.activities.BaseActivity

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_more)
    }
}
