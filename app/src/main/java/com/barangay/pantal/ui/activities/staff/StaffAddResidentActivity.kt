package com.barangay.pantal.ui.activities.staff

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityStaffAddResidentBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.picasso.Picasso
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class StaffAddResidentActivity : BaseActivity() {

    private lateinit var binding: ActivityStaffAddResidentBinding
    private var residentId: String? = null
    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String? = null
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data
            if (selectedImageUri != null) {
                binding.residentImage.setImageURI(selectedImageUri)
            }
        }
    }

    private val scanIdLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                processImageForOCR(imageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffAddResidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        residentId = intent.getStringExtra("residentId")

        if (residentId != null) {
            loadResidentData()
        }

        binding.saveButton.setOnClickListener {
            saveResident()
        }

        binding.btnChangePhoto.setOnClickListener {
            openGallery(selectImageLauncher)
        }

        binding.btnScanId.setOnClickListener {
            openGallery(scanIdLauncher)
        }
    }

    private fun openGallery(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launcher.launch(intent)
    }

    private fun processImageForOCR(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    if (fullText.isNotEmpty()) {
                        val lines = fullText.split("\n")
                        if (lines.isNotEmpty()) binding.nameEditText.setText(lines[0])
                        if (lines.size > 1) binding.addressEditText.setText(lines.subList(1, lines.size).joinToString(" "))
                        Toast.makeText(this, "ID Scanned Successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to scan ID: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("StaffAddResident", "OCR error", e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadResidentData() {
        lifecycleScope.launch {
            try {
                val resident = SupabaseClient.client.postgrest["residents"]
                    .select(Columns.ALL) {
                        filter {
                            eq("id", residentId!!)
                        }
                    }
                    .decodeSingle<Resident>()
                
                existingImageUrl = resident.imageUrl
                binding.nameEditText.setText(resident.name)
                binding.ageEditText.setText(resident.age.toString())
                binding.genderEditText.setText(resident.gender)
                binding.addressEditText.setText(resident.address)
                binding.occupationEditText.setText(resident.occupation)
                binding.voterSwitch.isChecked = resident.isVoter
                binding.seniorSwitch.isChecked = resident.isSenior
                binding.pwdSwitch.isChecked = resident.isPwd

                if (!resident.imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(resident.imageUrl).placeholder(com.barangay.pantal.R.drawable.ic_profile_placeholder).into(binding.residentImage)
                }
            } catch (e: Exception) {
                Log.e("StaffAddResident", "Load error", e)
                Toast.makeText(this@StaffAddResidentActivity, "Error loading resident: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveResident() {
        val name = binding.nameEditText.text.toString().trim()
        val ageString = binding.ageEditText.text.toString().trim()
        val gender = binding.genderEditText.text.toString().trim()
        val address = binding.addressEditText.text.toString().trim()
        val occupation = binding.occupationEditText.text.toString().trim()
        val isVoter = binding.voterSwitch.isChecked
        val isSenior = binding.seniorSwitch.isChecked
        val isPwd = binding.pwdSwitch.isChecked

        if (name.isEmpty() || ageString.isEmpty() || gender.isEmpty() || address.isEmpty() || occupation.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageString.toIntOrNull() ?: 0

        lifecycleScope.launch {
            try {
                binding.saveButton.isEnabled = false
                
                var finalImageUrl = existingImageUrl

                if (selectedImageUri != null) {
                    try {
                        finalImageUrl = uploadImageToSupabase(selectedImageUri!!)
                    } catch (e: Exception) {
                        Log.e("StaffAddResident", "Image upload failed", e)
                        Toast.makeText(this@StaffAddResidentActivity, "Image upload failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        binding.saveButton.isEnabled = true
                        return@launch
                    }
                }

                val currentResidentId = residentId ?: UUID.randomUUID().toString()
                val resident = Resident(currentResidentId, name, age, gender, address, occupation, isVoter, isSenior, isPwd, finalImageUrl)
                
                SupabaseClient.client.postgrest["residents"].upsert(resident)
                
                Toast.makeText(this@StaffAddResidentActivity, "Resident saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("StaffAddResident", "Save error", e)
                Toast.makeText(this@StaffAddResidentActivity, "Failed to save resident: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                binding.saveButton.isEnabled = true
            }
        }
    }

    private suspend fun uploadImageToSupabase(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val fileName = "resident_${UUID.randomUUID()}.jpg"
            val bucket = SupabaseClient.client.storage["profiles"]
            
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: throw Exception("Could not read image bytes")
            inputStream.close()

            bucket.upload(fileName, bytes)
            bucket.publicUrl(fileName)
        }
    }
}
