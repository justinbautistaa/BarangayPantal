package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityServicesBinding
import com.barangay.pantal.databinding.DialogProgramDetailsBinding
import com.barangay.pantal.model.ProgramService
import com.barangay.pantal.model.Resident
import com.barangay.pantal.model.Service
import com.barangay.pantal.model.ServiceCatalog
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.barangay.pantal.ui.adapters.user.ProgramServiceAdapter
import com.squareup.picasso.Picasso
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ServicesActivity : BaseActivity() {

    private lateinit var binding: ActivityServicesBinding
    private lateinit var adapter: ProgramServiceAdapter
    private var allPrograms: List<ProgramService> = emptyList()
    private var selectedCategory = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ProgramServiceAdapter(emptyList()) { program ->
            showProgramDetails(program)
        }

        binding.servicesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.servicesRecyclerView.adapter = adapter

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_services)
        setupFilters()
        fetchServices()
        fetchResidentProfile()
    }

    override fun onResume() {
        super.onResume()
        fetchServices()
        fetchResidentProfile()
    }

    private fun setupFilters() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCategory = when (checkedIds.firstOrNull()) {
                R.id.chipEmployment -> "Employment"
                R.id.chipHealth -> "Health"
                R.id.chipYouth -> "Youth"
                R.id.chipEducation -> "Education"
                R.id.chipLivelihood -> "Livelihood"
                R.id.chipSocial -> "Social"
                else -> "all"
            }
            applyFilters()
        }

        binding.etSearchServices.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                applyFilters()
            }
        })

        binding.tvClearFilters.setOnClickListener { clearFilters() }
        binding.btnClearEmptyState.setOnClickListener { clearFilters() }
    }

    private fun clearFilters() {
        binding.etSearchServices.setText("")
        binding.chipAll.isChecked = true
        selectedCategory = "all"
        applyFilters()
    }

    private fun fetchServices() {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.postgrest["services"].select().decodeList<Service>()
                val servicesToShow = if (result.isEmpty()) {
                    ServiceCatalog.builtIns()
                } else {
                    ServiceCatalog.mergeWithBuiltIns(result)
                }
                allPrograms = servicesToShow.map(::mapServiceToProgram)
                applyFilters()
            } catch (_: Exception) {
                allPrograms = ServiceCatalog.builtIns().map(::mapServiceToProgram)
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val query = binding.etSearchServices.text?.toString()?.trim().orEmpty()
        val filteredPrograms = allPrograms.filter { program ->
            val matchesSearch = query.isBlank() ||
                program.title.contains(query, ignoreCase = true) ||
                program.description.contains(query, ignoreCase = true) ||
                program.category.contains(query, ignoreCase = true)
            val matchesCategory = selectedCategory == "all" || program.category == selectedCategory
            matchesSearch && matchesCategory
        }

        adapter.updateData(filteredPrograms)
        binding.tvResultsInfo.text = "Showing ${filteredPrograms.size} of ${allPrograms.size} programs"
        val hasFilters = query.isNotBlank() || selectedCategory != "all"
        binding.tvClearFilters.visibility = if (hasFilters) View.VISIBLE else View.GONE
        binding.layoutEmptyState.visibility = if (filteredPrograms.isEmpty()) View.VISIBLE else View.GONE
        binding.servicesRecyclerView.visibility = if (filteredPrograms.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun fetchResidentProfile() {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        lifecycleScope.launch {
            try {
                val users = SupabaseClient.client.postgrest["users"]
                    .select {
                        filter { eq("id", authUser.id) }
                    }
                    .decodeList<User>()

                val residents = SupabaseClient.client.postgrest["residents"]
                    .select {
                        filter { eq("id", authUser.id) }
                    }
                    .decodeList<Resident>()

                val mergedProfile = mergeProfileData(
                    users.firstOrNull(),
                    residents.firstOrNull(),
                    authUser.email.orEmpty()
                )
                updateResidentHeader(mergedProfile)
            } catch (_: Exception) {
                updateResidentHeader(null)
            }
        }
    }

    private fun mergeProfileData(
        userProfile: User?,
        residentProfile: Resident?,
        fallbackEmail: String
    ): User? {
        if (userProfile == null && residentProfile == null) return null

        val fullName = residentProfile?.name?.takeIf { it.isNotBlank() }
            ?: userProfile?.fullName
            ?: "Resident"

        return User(
            id = userProfile?.id ?: residentProfile?.id,
            fullName = fullName,
            email = residentProfile?.email?.takeIf { it.isNotBlank() } ?: userProfile?.email ?: fallbackEmail,
            role = userProfile?.role ?: "user",
            age = residentProfile?.age ?: userProfile?.age,
            gender = residentProfile?.gender ?: userProfile?.gender,
            address = residentProfile?.address ?: userProfile?.address,
            occupation = residentProfile?.occupation ?: userProfile?.occupation,
            phoneNumber = residentProfile?.phone ?: userProfile?.phoneNumber,
            profilePictureUrl = residentProfile?.profilePicture ?: userProfile?.profilePictureUrl ?: residentProfile?.imageUrl
        )
    }

    private fun updateResidentHeader(profile: User?) {
        val residentName = profile?.fullName?.takeIf { it.isNotBlank() } ?: "Resident"
        binding.tvWelcomeTitle.text = "Welcome back, $residentName!"

        if (!profile?.profilePictureUrl.isNullOrBlank()) {
            Picasso.get()
                .load(profile.profilePictureUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(binding.ivResidentAvatar)
        } else {
            binding.ivResidentAvatar.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun mapServiceToProgram(service: Service): ProgramService {
        return ProgramService(
            id = service.id,
            category = service.category ?: "General",
            title = service.name,
            description = service.description,
            requirements = service.requirements,
            schedule = service.schedule ?: "Please ask the barangay office for the latest schedule.",
            venue = service.venue ?: "Barangay Hall",
            contact = service.contactInfo ?: "Barangay Hall - (075) 123-4567",
            duration = service.status ?: "Active",
            benefits = if (service.howToAvail.isNullOrBlank()) emptyList() else listOf(service.howToAvail)
        )
    }

    private fun showProgramDetails(program: ProgramService) {
        val dialogBinding = DialogProgramDetailsBinding.inflate(layoutInflater)
        dialogBinding.tvDialogCategory.text = program.category
        dialogBinding.tvDialogTitle.text = program.title
        dialogBinding.tvDialogDescription.text = program.description
        dialogBinding.tvDialogSchedule.text = program.schedule
        dialogBinding.tvDialogDuration.text = "Status: ${program.duration}"
        dialogBinding.tvDialogVenue.text = program.venue
        dialogBinding.tvDialogRequirements.text = if (program.requirements.isEmpty()) {
            "Requirements will be confirmed during assessment."
        } else {
            toBullets(program.requirements)
        }
        dialogBinding.tvDialogBenefits.text = if (program.benefits.isEmpty()) {
            "Please coordinate with the barangay office for complete program benefits."
        } else {
            toBullets(program.benefits)
        }
        dialogBinding.tvDialogContact.text = program.contact
        dialogBinding.tvDialogHowToAvail.text =
            "Pumunta sa ${program.venue} at makipag-ugnayan sa Barangay Secretary o sa program coordinator. Para sa karagdagang impormasyon, maaaring tumawag sa ${program.contact}."

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun toBullets(items: List<String>): String {
        return items.joinToString("\n") { "\u2022 $it" }
    }
}
