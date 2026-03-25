package com.barangay.pantal.ui.activities.admin

import android.content.ContentValues
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityViewResidentBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.network.SupabaseClient
import com.barangay.pantal.ui.activities.BaseActivity
import com.google.android.material.chip.Chip
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ViewResidentActivity : BaseActivity() {

    private lateinit var binding: ActivityViewResidentBinding
    private var residentId: String? = null
    private var currentResident: Resident? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewResidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        residentId = intent.getStringExtra("residentId")

        if (residentId != null) {
            loadResidentData()
        }

        binding.btnGenerateClearance.setOnClickListener {
            currentResident?.let { generateClearancePdf(it) } ?: run {
                Toast.makeText(this, "Resident data not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
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
                    .decodeSingleOrNull<Resident>()

                currentResident = resident
                currentResident?.let { res ->
                    binding.nameTextView.text = res.name
                    binding.detailsTextView.text = "Age: ${res.age ?: 0} | Gender: ${res.gender ?: "N/A"}"
                    binding.addressTextView.text = "Address: ${res.address ?: "N/A"}"
                    binding.occupationTextView.text = "Occupation: ${res.occupation ?: "N/A"}"

                    binding.tagsChipGroup.removeAllViews()
                    if (res.isVoter) binding.tagsChipGroup.addView(createTagChip("Voter"))
                    if (res.isSenior) binding.tagsChipGroup.addView(createTagChip("Senior"))
                    if (res.isPwd) binding.tagsChipGroup.addView(createTagChip("PWD"))
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewResidentActivity, "Error loading data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateClearancePdf(resident: Resident) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("REPUBLIC OF THE PHILIPPINES", 297f, 50f, paint)
        canvas.drawText("PROVINCE OF PANGASINAN", 297f, 65f, paint)
        canvas.drawText("CITY OF DAGUPAN", 297f, 80f, paint)
        paint.textSize = 16f
        canvas.drawText("BARANGAY PANTAL", 297f, 105f, paint)

        paint.textSize = 24f
        paint.color = Color.BLACK
        canvas.drawText("BARANGAY CLEARANCE", 297f, 180f, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f
        paint.isFakeBoldText = false
        val startX = 50f
        var startY = 250f

        canvas.drawText("TO WHOM IT MAY CONCERN:", startX, startY, paint)
        startY += 40f

        canvas.drawText(
            "This is to certify that ${resident.name}, ${resident.age ?: 0} years old,",
            startX,
            startY,
            paint
        )
        startY += 20f
        canvas.drawText("is a bona fide resident of Barangay Pantal, Dagupan City.", startX, startY, paint)

        startY += 40f
        canvas.drawText("This certification is issued upon the request of the above-mentioned", startX, startY, paint)
        startY += 20f
        canvas.drawText("person for whatever legal purpose it may serve.", startX, startY, paint)

        startY += 100f
        paint.isFakeBoldText = true
        canvas.drawText("HON. BARANGAY CAPTAIN", 400f, startY, paint)
        paint.isFakeBoldText = false
        canvas.drawText("Punong Barangay", 420f, startY + 20f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Clearance_${resident.name.replace(" ", "_")}.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        try {
            val outputStream: OutputStream?
            val successMessage: String

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { contentResolver.openOutputStream(it) }
                successMessage = "Clearance saved to Downloads folder"
            } else {
                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
                val outputFile = File(downloadsDir, fileName)
                outputStream = FileOutputStream(outputFile)
                successMessage = "Clearance saved to ${outputFile.absolutePath}"
            }

            outputStream?.use {
                pdfDocument.writeTo(it)
            } ?: throw IllegalStateException("Unable to open output stream for PDF")

            Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun createTagChip(tag: String): Chip {
        return Chip(this).apply { text = tag }
    }
}
