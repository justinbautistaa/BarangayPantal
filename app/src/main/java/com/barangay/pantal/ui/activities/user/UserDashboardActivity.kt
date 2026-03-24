package com.barangay.pantal.ui.activities.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.BuildConfig
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityUserDashboardBinding
import com.barangay.pantal.model.Announcement
import com.barangay.pantal.model.Request
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.network.WeatherClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.auth.LoginActivity
import com.barangay.pantal.ui.adapters.common.AnnouncementsAdapter
import com.squareup.picasso.Picasso
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class UserDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private var currentUserProfile: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardViewAllNews.setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        binding.cardRequestDocument.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            lifecycleScope.launch {
                SupabaseClient.client.auth.signOut()
                val intent = Intent(this@UserDashboardActivity, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }

        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.rvAnnouncements.layoutManager = LinearLayoutManager(this)
        binding.rvAnnouncements.isNestedScrollingEnabled = false
        binding.rvAnnouncements.adapter = AnnouncementsAdapter(false, emptyList()) { }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)

        fetchUserProfile()
        fetchRequestSummary()
        fetchLatestAnnouncements()
        fetchWeather()
    }

    private fun fetchUserProfile() {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        lifecycleScope.launch {
            try {
                val userProfile = SupabaseClient.client.postgrest["users"]
                    .select {
                        filter {
                            eq("id", authUser.id)
                        }
                    }
                    .decodeSingle<User>()

                currentUserProfile = userProfile
                binding.tvUserName.text = userProfile.fullName
                binding.welcomeMessage.text = "Hello, ${userProfile.fullName.split(" ").firstOrNull()}!"
                binding.tvUserAddress.text = userProfile.address?.ifBlank { "Barangay Pantal, Dagupan City" }
                    ?: "Barangay Pantal, Dagupan City"

                if (!userProfile.profilePictureUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(userProfile.profilePictureUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.profileImage)
                }
            } catch (e: Exception) {
                Log.e("UserDashboard", "Error fetching profile", e)
            }
        }
    }

    private fun fetchRequestSummary() {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        lifecycleScope.launch {
            try {
                val requests = SupabaseClient.client.postgrest["requests"]
                    .select(Columns.list("id", "status", "user_id")) {
                        filter { eq("user_id", authUser.id) }
                    }
                    .decodeList<Request>()

                binding.tvPendingRequests.text = requests.count {
                    it.status.equals("Pending", ignoreCase = true) ||
                        it.status.equals("Processing", ignoreCase = true)
                }.toString()
                binding.tvCompletedRequests.text = requests.count {
                    it.status.equals("Approved", ignoreCase = true) ||
                        it.status.equals("Completed", ignoreCase = true)
                }.toString()
            } catch (e: Exception) {
                Log.e("UserDashboard", "Error fetching request summary", e)
                binding.tvPendingRequests.text = "0"
                binding.tvCompletedRequests.text = "0"
            }
        }
    }

    private fun fetchLatestAnnouncements() {
        lifecycleScope.launch {
            try {
                val announcements = SupabaseClient.client.postgrest["announcements"]
                    .select()
                    .decodeList<Announcement>()
                    .sortedByDescending { it.timestamp }
                    .take(3)

                (binding.rvAnnouncements.adapter as? AnnouncementsAdapter)?.updateData(announcements)
            } catch (e: Exception) {
                Log.e("UserDashboard", "Error fetching announcements", e)
            }
        }
    }

    private fun fetchWeather() {
        val apiKey = BuildConfig.OPENWEATHER_API_KEY
        val city = getString(R.string.weather_city_default)

        lifecycleScope.launch {
            try {
                val response = WeatherClient.service.getCurrentWeather(city, apiKey)
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        val temp = it.main.temp.toInt()
                        val description = it.weather[0].description.replaceFirstChar { ch -> ch.uppercase() }
                        binding.tvWeatherTemp.text = "${temp}°C"
                        binding.tvWeatherDesc.text = description
                        binding.tvWeather.text = getString(R.string.weather_format, temp, description)

                        val iconRes = when {
                            description.contains("cloud", true) -> R.drawable.ic_cloudy
                            description.contains("rain", true) -> R.drawable.ic_rainy
                            description.contains("clear", true) -> R.drawable.ic_sunny
                            else -> R.drawable.ic_weather_placeholder
                        }
                        binding.ivWeatherIcon.setImageResource(iconRes)
                    }
                } else {
                    Log.e("UserDashboard", "Weather API Error: ${response.code()}")
                    binding.tvWeatherDesc.text = getString(R.string.weather_unavailable)
                }
            } catch (e: Exception) {
                Log.e("UserDashboard", "Network Error", e)
                binding.tvWeatherDesc.text = getString(R.string.weather_error)
            }
        }
    }
}
