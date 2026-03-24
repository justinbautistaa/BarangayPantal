package com.barangay.pantal.ui.activities.admin

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
import com.barangay.pantal.R
import com.barangay.pantal.databinding.ActivityAddResidentBinding
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

class AddResidentActivity : BaseActivity() {

    private lateinit var binding: ActivityAddResidentBinding
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
        binding = ActivityAddResidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        residentId = intent.getStringExtra("residentId")

        if (residentId != null) {
            binding.toolbar.title = getString(R.string.title_edit_resident)
            binding.saveButton.text = getString(R.string.button_update_resident)
            loadResidentData()
        } else {
            binding.toolbar.title = getString(R.string.title_add_resident)
            binding.saveButton.text = getString(R.string.button_save_resident)
        }

        binding.btnChangePhoto.setOnClickListener {
            openGallery(selectImageLauncher)
        }

        binding.btnScanId.setOnClickListener {
            openGallery(scanIdLauncher)
        }

        binding.saveButton.setOnClickListener {
            saveResident()
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
                        Toast.makeText(this, getString(R.string.id_scanned_success), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.error_id_scan_failed, e.message), Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("AddResident", "OCR error", e)
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
                        filter { eq("id", residentId!!) }
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
                    Picasso.get().load(resident.imageUrl).placeholder(R.drawable.ic_profile_placeholder).into(binding.residentImage)
                }
            } catch (e: Exception) {
                Log.e("AddResident", "Load error", e)
                Toast.makeText(this@AddResidentActivity, getString(R.string.error_load_data, e.localizedMessage), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageString.toIntOrNull() ?: 0

        lifecycleScope.launch {
            try {
                binding.saveButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
                
                var finalImageUrl = existingImageUrl

                // 1. Upload image if selected
                if (selectedImageUri != null) {
                    try {
                        finalImageUrl = uploadImageToSupabase(selectedImageUri!!)
                    } catch (e: Exception) {
                        Log.e("AddResident", "Image upload failed", e)
                        Toast.makeText(this@AddResidentActivity, getString(R.string.error_image_upload_failed, e.localizedMessage), Toast.LENGTH_SHORT).show()
                        binding.saveButton.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        return@launch
                    }
                }

                // 2. Prepare resident object
                val currentResidentId = residentId ?: UUID.randomUUID().toString()
                val resident = Resident(currentResidentId, name, age, gender, address, occupation, isVoter, isSenior, isPwd, finalImageUrl)
                
                // Align with the working staff flow to support both create and edit cleanly.
                SupabaseClient.client.postgrest["residents"].upsert(resident)
                val messageRes = if (residentId == null) {
                    R.string.resident_added_success
                } else {
                    R.string.resident_updated_success
                }
                Toast.makeText(this@AddResidentActivity, getString(messageRes), Toast.LENGTH_SHORT).show()
                
                finish()
            } catch (e: Exception) {
                Log.e("AddResident", "Save error", e)
                Toast.makeText(this@AddResidentActivity, getString(R.string.error_save_failed, e.localizedMessage), Toast.LENGTH_LONG).show()
                binding.saveButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
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
