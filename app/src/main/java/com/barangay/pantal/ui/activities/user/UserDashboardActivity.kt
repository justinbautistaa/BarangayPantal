package com.barangay.pantal.ui.activities.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityUserDashboardBinding
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.network.WeatherService
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.activities.auth.LoginActivity
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserDashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityUserDashboardBinding

    private val weatherService: WeatherService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }

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

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_dashboard)
        
        fetchWeather()
    }

    private fun fetchWeather() {
        val apiKey = "d96a227c56697296f2344e7e5e36f1f1"
        val city = "Dagupan,PH"

        lifecycleScope.launch {
            try {
                val response = weatherService.getCurrentWeather(city, apiKey)
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    weatherData?.let {
                        val temp = it.main.temp.toInt()
                        val description = it.weather[0].description.replaceFirstChar { it.uppercase() }
                        binding.tvWeather.text = "$temp°C | $description"
                    }
                } else {
                    Log.e("UserDashboard", "Weather API Error: ${response.code()}")
                    binding.tvWeather.text = "Weather unavailable"
                }
            } catch (e: Exception) {
                Log.e("UserDashboard", "Network Error", e)
                binding.tvWeather.text = "Error loading weather"
            }
        }
    }
}
