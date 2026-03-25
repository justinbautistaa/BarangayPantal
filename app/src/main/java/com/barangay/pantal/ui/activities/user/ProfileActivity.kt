package com.barangay.pantal.ui.activities.user

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityProfileBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.squareup.picasso.Picasso
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.UUID

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var currentUser: User? = null
    private var currentResident: Resident? = null
    private var pendingProfilePictureUrl: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
        imageUri?.let {
            binding.ivProfilePicture.setImageURI(it)
            uploadImageToSupabase(it)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let { uploadBitmapToSupabase(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupBottomNavigation(binding.bottomNavigation, R.id.navigation_profile)
        
        fetchUserProfile()

        binding.ivProfilePicture.setOnClickListener {
            showImagePickerOptions()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfile()
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Gallery", "Camera")
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun uploadImageToSupabase(uri: Uri) {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream.useOrThrow()
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val extension = when (mimeType.lowercase()) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> "jpg"
                }
                val fileName = "profile-pictures/${authUser.id}/${UUID.randomUUID()}.$extension"
                val bucket = SupabaseClient.client.storage["profiles"]
                
                bucket.upload(fileName, bytes, upsert = true)
                val url = bucket.publicUrl(fileName)
                
                updateProfilePictureUrl(url)
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("ProfileActivity", "Gallery upload failed", e)
                Toast.makeText(this@ProfileActivity, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadBitmapToSupabase(bitmap: Bitmap) {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val bytes = stream.toByteArray()
                
                val fileName = "profile-pictures/${authUser.id}/${UUID.randomUUID()}.jpg"
                val bucket = SupabaseClient.client.storage["profiles"]
                
                bucket.upload(fileName, bytes, upsert = true)
                val url = bucket.publicUrl(fileName)
                
                updateProfilePictureUrl(url)
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e("ProfileActivity", "Camera upload failed", e)
                Toast.makeText(this@ProfileActivity, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun updateProfilePictureUrl(url: String) {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        try {
            ensureUserProfileRow(authUser.id)
            ensureResidentProfileRow(authUser.id)
            SupabaseClient.client.postgrest["users"].update({
                set("image_url", url)
            }) {
                filter {
                    eq("id", authUser.id)
                }
            }
            SupabaseClient.client.postgrest["residents"].update({
                set("profile_picture", url)
            }) {
                filter {
                    eq("id", authUser.id)
                }
            }
            
            withContext(Dispatchers.Main) {
                pendingProfilePictureUrl = url
                currentUser = (currentUser ?: User(
                    id = authUser.id,
                    fullName = metadataValue(authUser.userMetadata, "full_name").orEmpty(),
                    email = authUser.email.orEmpty(),
                    address = metadataValue(authUser.userMetadata, "address"),
                    phoneNumber = metadataValue(authUser.userMetadata, "phone_number")
                )).copy(profilePictureUrl = url)
                currentResident = (currentResident ?: Resident(
                    id = authUser.id,
                    name = currentUser?.fullName.orEmpty(),
                    email = authUser.email.orEmpty()
                )).copy(profilePicture = url)
                
                Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(binding.ivProfilePicture)
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ProfileActivity, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                fetchUserProfile()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                Log.e("ProfileActivity", "Error updating URL", e)
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to save profile picture: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun ensureUserProfileRow(userId: String) {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        val existingUsers = SupabaseClient.client.postgrest["users"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeList<User>()

        if (existingUsers.isNotEmpty()) return

        val metadata = authUser.userMetadata
        val fallbackName = metadataValue(metadata, "full_name").orEmpty()
        val fallbackAddress = metadataValue(metadata, "address").orEmpty()
        val fallbackPhone = metadataValue(metadata, "phone_number").orEmpty()

        val userProfile = User(
            id = userId,
            fullName = fallbackName.ifBlank {
                authUser.email
                    ?.substringBefore("@")
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    ?: "Resident"
            },
            email = authUser.email.orEmpty(),
            role = currentUser?.role ?: "user",
            address = fallbackAddress.ifBlank { currentUser?.address },
            phoneNumber = fallbackPhone.ifBlank { currentUser?.phoneNumber },
            profilePictureUrl = currentUser?.profilePictureUrl
        )

        SupabaseClient.client.postgrest["users"].insert(userProfile)
        currentUser = userProfile
    }

    private suspend fun ensureResidentProfileRow(userId: String) {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        val existingResidents = SupabaseClient.client.postgrest["residents"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeList<Resident>()

        if (existingResidents.isNotEmpty()) {
            currentResident = existingResidents.first()
            return
        }

        val residentProfile = Resident(
            id = userId,
            name = currentUser?.fullName.orEmpty().ifBlank {
                authUser.email
                    ?.substringBefore("@")
                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    ?: "Resident"
            },
            age = currentUser?.age,
            gender = currentUser?.gender,
            address = currentUser?.address,
            occupation = currentUser?.occupation,
            phone = currentUser?.phoneNumber,
            email = authUser.email.orEmpty(),
            profilePicture = currentUser?.profilePictureUrl
        )

        SupabaseClient.client.postgrest["residents"].insert(residentProfile)
        currentResident = residentProfile
    }

    private fun fetchUserProfile() {
        val authUser = SupabaseClient.client.auth.currentUserOrNull()
        if (authUser != null) {
            lifecycleScope.launch {
                try {
                    binding.progressBar.visibility = View.VISIBLE
                    val refreshedAuthUser = SupabaseClient.client.auth.retrieveUserForCurrentSession()
                    val userProfiles = SupabaseClient.client.postgrest["users"]
                        .select {
                            filter {
                                eq("id", authUser.id)
                        }
                    }
                        .decodeList<User>()
                    val residentProfiles = SupabaseClient.client.postgrest["residents"]
                        .select {
                            filter {
                                eq("id", authUser.id)
                            }
                        }
                        .decodeList<Resident>()

                    val userProfile = userProfiles.firstOrNull() ?: run {
                        ensureUserProfileRow(authUser.id)
                        SupabaseClient.client.postgrest["users"]
                            .select {
                                filter {
                                    eq("id", authUser.id)
                                }
                            }
                            .decodeList<User>()
                            .firstOrNull()
                    }
                    val residentProfile = residentProfiles.firstOrNull() ?: run {
                        ensureResidentProfileRow(authUser.id)
                        SupabaseClient.client.postgrest["residents"]
                            .select {
                                filter {
                                    eq("id", authUser.id)
                                }
                            }
                            .decodeList<Resident>()
                            .firstOrNull()
                    }
                    
                    val profilePictureUrl = pendingProfilePictureUrl
                        ?: residentProfile?.profilePicture
                        ?: userProfile?.profilePictureUrl
                        ?: metadataValue(refreshedAuthUser.userMetadata, "image_url")
                    val resolvedProfile = mergeProfileData(
                        userProfile = userProfile,
                        residentProfile = residentProfile,
                        fallbackEmail = authUser.email.orEmpty(),
                        fallbackImageUrl = profilePictureUrl
                    )

                    if (resolvedProfile != null) {
                        currentUser = resolvedProfile
                        currentResident = residentProfile
                        updateUI(resolvedProfile)
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                    binding.progressBar.visibility = View.GONE
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    Log.e("ProfileActivity", "Fetch error: ${e.message}")
                    Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mergeProfileData(
        userProfile: User?,
        residentProfile: Resident?,
        fallbackEmail: String,
        fallbackImageUrl: String?
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
                ?: fallbackImageUrl
        )
    }

    private fun updateUI(user: User) {
        binding.tvDisplayFullName.text = user.fullName
        binding.tvDisplayEmail.text = user.email
        
        binding.etFullName.setText(user.fullName)
        binding.etAge.setText(user.age?.toString() ?: "")
        binding.etGender.setText(user.gender ?: "")
        binding.etOccupation.setText(user.occupation ?: "")
        binding.etPhone.setText(user.phoneNumber ?: "")
        binding.etAddress.setText(user.address ?: "")

        if (!user.profilePictureUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(user.profilePictureUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(binding.ivProfilePicture)
        } else {
            binding.ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun saveProfileChanges() {
        val authUser = SupabaseClient.client.auth.currentUserOrNull() ?: return
        val currentProfile = currentUser ?: return

        val newName = binding.etFullName.text.toString().trim()
        val newAgeString = binding.etAge.text.toString().trim()
        val newGender = binding.etGender.text.toString().trim()
        val newOccupation = binding.etOccupation.text.toString().trim()
        val newPhone = binding.etPhone.text.toString().trim()
        val newAddress = binding.etAddress.text.toString().trim()

        if (newName.isEmpty()) {
            binding.etFullName.error = "Name is required"
            return
        }

        val newAge = newAgeString.toIntOrNull()

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSaveProfile.isEnabled = false
                val latestProfilePictureUrl = pendingProfilePictureUrl ?: currentProfile.profilePictureUrl

                ensureUserProfileRow(authUser.id)
                ensureResidentProfileRow(authUser.id)

                val userUpdates = User(
                    id = authUser.id,
                    fullName = newName,
                    email = authUser.email ?: currentProfile.email,
                    role = currentProfile.role, 
                    age = newAge,
                    gender = newGender,
                    occupation = newOccupation,
                    phoneNumber = newPhone,
                    address = newAddress,
                    profilePictureUrl = latestProfilePictureUrl
                )

                SupabaseClient.client.postgrest["users"].update({
                    set("full_name", userUpdates.fullName)
                    set("email", userUpdates.email)
                    set("role", userUpdates.role)
                    set("age", userUpdates.age)
                    set("gender", userUpdates.gender)
                    set("occupation", userUpdates.occupation)
                    set("phone_number", userUpdates.phoneNumber)
                    set("address", userUpdates.address)
                    set("image_url", userUpdates.profilePictureUrl)
                }) {
                    filter {
                        eq("id", authUser.id)
                    }
                }
                SupabaseClient.client.postgrest["residents"].update({
                    set("name", newName)
                    set("age", newAge)
                    set("gender", newGender.ifBlank { null })
                    set("occupation", newOccupation.ifBlank { null })
                    set("phone", newPhone.ifBlank { null })
                    set("address", newAddress.ifBlank { null })
                    set("birthdate", currentResident?.birthdate)
                    set("email", authUser.email ?: currentProfile.email)
                    set("profile_picture", latestProfilePictureUrl)
                }) {
                    filter {
                        eq("id", authUser.id)
                    }
                }

                Toast.makeText(this@ProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                currentUser = userUpdates
                currentResident = (currentResident ?: Resident(id = authUser.id)).copy(
                    name = newName,
                    age = newAge,
                    gender = newGender.ifBlank { null },
                    occupation = newOccupation.ifBlank { null },
                    phone = newPhone.ifBlank { null },
                    address = newAddress.ifBlank { null },
                    birthdate = currentResident?.birthdate,
                    email = authUser.email ?: currentProfile.email,
                    profilePicture = latestProfilePictureUrl
                )
                pendingProfilePictureUrl = latestProfilePictureUrl
                fetchUserProfile()
                
                binding.btnSaveProfile.isEnabled = true
                binding.progressBar.visibility = View.GONE

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true
                Log.e("ProfileActivity", "Update error: ${e.message}")
                Toast.makeText(this@ProfileActivity, "Update failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun InputStream?.useOrThrow(): ByteArray {
        val stream = this ?: throw IllegalStateException("Unable to open selected image")
        return stream.use { it.readBytes() }
    }

    private fun metadataValue(metadata: kotlinx.serialization.json.JsonObject?, key: String): String? {
        return metadata?.get(key)?.toString()?.trim('"')?.takeIf { it.isNotBlank() }
    }
}
