package com.barangay.pantal.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAddAnnouncementBinding

class AddAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAnnouncementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
