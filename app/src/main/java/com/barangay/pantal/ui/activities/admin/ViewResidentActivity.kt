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
import com.barangay.pantal.databinding.ActivityViewResidentBinding
import com.barangay.pantal.model.Resident
import com.barangay.pantal.ui.activities.BaseActivity
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
        val query = FirebaseDatabase.getInstance().reference.child("residents").child(residentId!!)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentResident = snapshot.getValue(Resident::class.java)
                currentResident?.let { resident ->
                    binding.nameTextView.text = resident.name
                    binding.detailsTextView.text = "Age: ${resident.age} • Gender: ${resident.gender}"
                    binding.addressTextView.text = "Address: ${resident.address}"
                    binding.occupationTextView.text = "Occupation: ${resident.occupation}"

                    binding.tagsChipGroup.removeAllViews()
                    if (resident.isVoter) binding.tagsChipGroup.addView(createTagChip("Voter"))
                    if (resident.isSenior) binding.tagsChipGroup.addView(createTagChip("Senior"))
                    if (resident.isPwd) binding.tagsChipGroup.addView(createTagChip("PWD"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewResidentActivity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateClearancePdf(resident: Resident) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Header
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("REPUBLIC OF THE PHILIPPINES", 297f, 50f, paint)
        canvas.drawText("PROVINCE OF PANGASINAN", 297f, 65f, paint)
        canvas.drawText("CITY OF DAGUPAN", 297f, 80f, paint)
        paint.textSize = 16f
        canvas.drawText("BARANGAY PANTAL", 297f, 105f, paint)

        // Title
        paint.textSize = 24f
        paint.color = Color.BLACK
        canvas.drawText("BARANGAY CLEARANCE", 297f, 180f, paint)

        // Body Content
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f
        paint.isFakeBoldText = false
        val startX = 50f
        var startY = 250f

        canvas.drawText("TO WHOM IT MAY CONCERN:", startX, startY, paint)
        startY += 40f
        
        val content = "This is to certify that ${resident.name}, ${resident.age} years old, " +
                "is a bona fide resident of Barangay Pantal, Dagupan City."
        
        // Simple text wrap (Manual)
        canvas.drawText("This is to certify that ${resident.name}, ${resident.age} years old,", startX, startY, paint)
        startY += 20f
        canvas.drawText("is a bona fide resident of Barangay Pantal, Dagupan City.", startX, startY, paint)
        
        startY += 40f
        canvas.drawText("This certification is issued upon the request of the above-mentioned", startX, startY, paint)
        startY += 20f
        canvas.drawText("person for whatever legal purpose it may serve.", startX, startY, paint)

        // Footer
        startY += 100f
        paint.isFakeBoldText = true
        canvas.drawText("HON. BARANGAY CAPTAIN", 400f, startY, paint)
        paint.isFakeBoldText = false
        canvas.drawText("Punong Barangay", 420f, startY + 20f, paint)

        pdfDocument.finishPage(page)

        // Save PDF using MediaStore (Modern Android Way)
        val fileName = "Clearance_${resident.name.replace(" ", "_")}.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        
        try {
            val outputStream: OutputStream? = uri?.let { contentResolver.openOutputStream(it) }
            outputStream?.let {
                pdfDocument.writeTo(it)
                it.close()
                Toast.makeText(this, "Clearance saved to Downloads folder", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }

    private fun createTagChip(tag: String): Chip {
        return Chip(this).apply { text = tag }
    }
}
