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
import com.barangay.pantal.model.Report
import com.barangay.pantal.model.Request
import com.barangay.pantal.model.Resident
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

        binding.btnViewAllAnnouncements.setOnClickListener {
            startActivity(Intent(this, AnnouncementsActivity::class.java))
        }

        binding.cardRequestDocument.setOnClickListener {
            startActivity(Intent(this, CreateRequestActivity::class.java))
        }

        binding.cardMyRequests.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }

        binding.cardSubmitReport.setOnClickListener {
            startActivity(Intent(this, UserReportsActivity::class.java))
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

        binding.heroProfileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.rvAnnouncements.layoutManager = LinearLayoutManager(this)
        binding.rvAnnouncements.isNestedScrollingEnabled = false
        binding.rvAnnouncements.adapter = AnnouncementsAdapter(false, emptyList()) { }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)

        fetchDashboardData()
        fetchWeather()
    }

    override fun onResume() {
        super.onResume()
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        fetchUserProfile()
        fetchRequestSummary()
        fetchReportSummary()
        fetchLatestAnnouncements()
    }

    private fun fetchUserProfile() {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        lifecycleScope.launch {
            try {
                val userProfiles = SupabaseClient.client.postgrest["users"]
                    .select {
                        filter { eq("id", authUser.id) }
                    }
                    .decodeList<User>()

                val residentProfiles = SupabaseClient.client.postgrest["residents"]
                    .select {
                        filter { eq("id", authUser.id) }
                    }
                    .decodeList<Resident>()

                val resolvedProfile = mergeProfileData(
                    userProfiles.firstOrNull(),
                    residentProfiles.firstOrNull(),
                    authUser.email.orEmpty()
                ) ?: return@launch

                currentUserProfile = resolvedProfile
                updateProfileUi(resolvedProfile)
            } catch (e: Exception) {
                Log.e("UserDashboard", "Error fetching profile", e)
            }
        }
    }

    private fun updateProfileUi(profile: User) {
        binding.tvHeaderResidentName.text = profile.fullName
        binding.tvUserName.text = profile.fullName
        binding.welcomeMessage.text = "Welcome back, ${profile.fullName.split(" ").firstOrNull() ?: "Resident"}!"
        binding.tvUserAddress.text = profile.address?.ifBlank { "Barangay Pantal, Dagupan City" }
            ?: "Barangay Pantal, Dagupan City"

        val profileUrl = profile.profilePictureUrl
        if (!profileUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(profileUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(binding.profileImage)

            Picasso.get()
                .load(profileUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(binding.heroProfileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_profile_placeholder)
            binding.heroProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun mergeProfileData(
        userProfile: User?,
        residentProfile: Resident?,
        fallbackEmail: String
    ): User? {
        if (userProfile == null && residentProfile == null) return null

        val fullName = residentProfile?.name
            ?.takeIf { it.isNotBlank() }
            ?: userProfile?.fullName
            ?: "Resident"

        return User(
            id = userProfile?.id ?: residentProfile?.id,
            fullName = fullName,
            email = residentProfile?.email
                ?.takeIf { it.isNotBlank() }
                ?: userProfile?.email
                ?: fallbackEmail,
            role = userProfile?.role ?: "user",
            age = residentProfile?.age ?: userProfile?.age,
            gender = residentProfile?.gender ?: userProfile?.gender,
            address = residentProfile?.address ?: userProfile?.address,
            occupation = residentProfile?.occupation ?: userProfile?.occupation,
            phoneNumber = residentProfile?.phone ?: userProfile?.phoneNumber,
            profilePictureUrl = residentProfile?.profilePicture
                ?: userProfile?.profilePictureUrl
                ?: residentProfile?.imageUrl
        )
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
                    val status = it.status.lowercase()
                    status == "pending" || status == "processing"
                }.toString()
                binding.tvApprovedRequests.text = requests.count {
                    val status = it.status.lowercase()
                    status == "approved" || status == "completed"
                }.toString()
                binding.tvRejectedRequests.text = requests.count {
                    val status = it.status.lowercase()
                    status == "rejected" || status == "cancelled"
                }.toString()
            } catch (e: Exception) {
                Log.e("UserDashboard", "Error fetching request summary", e)
                binding.tvPendingRequests.text = "0"
                binding.tvApprovedRequests.text = "0"
                binding.tvRejectedRequests.text = "0"
            }
        }
    }

    private fun fetchReportSummary() {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        lifecycleScope.launch {
            try {
                val reports = SupabaseClient.client.postgrest["reports"]
                    .select(
                        Columns.list(
                            "id",
                            "reporter_id",
                            "reporter_name",
                            "type",
                            "details",
                            "status",
                            "timestamp"
                        )
                    ) {
                        filter { eq("reporter_id", authUser.id) }
                    }
                    .decodeList<Report>()

                binding.tvPendingReports.text = reports.count {
                    val status = it.status.orEmpty().lowercase()
                    status == "pending" || status == "in progress" || status == "investigating" || status == "scheduled"
                }.toString()
                binding.tvResolvedReports.text = reports.count {
                    val status = it.status.orEmpty().lowercase()
                    status == "resolved" || status == "completed"
                }.toString()
            } catch (e: Exception) {
                Log.e("UserDashboard", "Error fetching report summary", e)
                binding.tvPendingReports.text = "0"
                binding.tvResolvedReports.text = "0"
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
                    .take(2)

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
                        binding.tvWeatherTemp.text = "$temp C"
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
