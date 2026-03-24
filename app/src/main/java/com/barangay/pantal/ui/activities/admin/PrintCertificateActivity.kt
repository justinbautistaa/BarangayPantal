package com.barangay.pantal.ui.activities.admin

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityPrintCertificateBinding
import com.barangay.pantal.model.RequestAdmin
import com.barangay.pantal.model.RequestTemplates
import com.barangay.pantal.model.Resident
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrintCertificateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrintCertificateBinding
    private var currentRequest: RequestAdmin? = null
    private var currentResident: Resident? = null
    private var currentUserProfile: User? = null
    private var fillValues: TemplateFillValues? = null
    private var previewBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrintCertificateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        currentRequest = intent.getSerializableExtra("request") as? RequestAdmin
        if (currentRequest == null) {
            Toast.makeText(this, "No request data found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            loadSupportingData()
            fillValues = buildDefaultFillValues()
            renderPreview()
        }

        binding.btnPrint.text = "Edit Fields"
        binding.btnPrint.setOnClickListener {
            showTemplateFillDialog()
        }

        binding.btnSavePdf.setOnClickListener {
            showTemplateFillDialog(generateAfterEdit = true)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private suspend fun loadSupportingData() {
        val request = currentRequest ?: return
        currentUserProfile = fetchUserProfile(request.userId)
        currentResident = fetchResidentData(request.userName)
    }

    private suspend fun renderPreview() {
        val request = currentRequest ?: return
        val values = fillValues ?: return
        try {
            val bitmap = generateFilledTemplateBitmap(request, values)
            previewBitmap = bitmap
            showPreviewBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("PrintCert", "Preview generation failed", e)
            Toast.makeText(this, "Failed to load template preview: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPreviewBitmap(bitmap: Bitmap) {
        binding.certificateContainer.removeAllViews()
        val imageView = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageBitmap(bitmap)
        }
        binding.certificateContainer.addView(imageView)
    }

    private suspend fun generateFilledTemplateBitmap(
        request: RequestAdmin,
        values: TemplateFillValues
    ): Bitmap = withContext(Dispatchers.IO) {
        val templateUrl = RequestTemplates.pdfUrlFor(request.serviceName)
            ?: error("No template URL found for ${request.serviceName}")
        val pdfFile = downloadTemplateToCache(templateUrl, request.id)
        val baseBitmap = renderFirstPage(pdfFile)
        overlayTemplateFields(baseBitmap, request, values)
        baseBitmap
    }

    private fun showTemplateFillDialog(generateAfterEdit: Boolean = false) {
        val request = currentRequest ?: return
        val values = fillValues ?: buildDefaultFillValues()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        fun addField(label: String, value: String): EditText {
            return EditText(this).apply {
                hint = label
                setText(value)
                container.addView(this)
            }
        }

        val nameInput = addField("Name", values.displayName)
        val addressInput = addField("Address", values.address)
        val barangayInput = addField("Barangay / Location", values.barangayArea)
        val purposeInput = addField("Purpose", values.purpose)
        val dayInput = addField("Issue Day", values.issueDay)
        val monthInput = addField("Issue Month", values.issueMonth)
        val yearInput = addField("Issue Year", values.issueYear)
        val signatureInput = addField("Signature Name", values.signatureName)

        val businessNameInput = if (request.serviceName.contains("Business", true)) {
            addField("Business Name", values.businessName)
        } else null
        val businessLocationInput = if (request.serviceName.contains("Business", true)) {
            addField("Business Location", values.businessLocation)
        } else null
        val validYearInput = if (request.serviceName.contains("Business", true)) {
            addField("Valid Until Year", values.validUntilYear)
        } else null
        val idNumberInput = if (request.serviceName.contains("ID", true)) {
            addField("ID Number", values.idNumber)
        } else null
        val idAgeInput = if (request.serviceName.contains("ID", true)) {
            addField("ID Age Label", values.idAgeLabel)
        } else null

        val dialog = AlertDialog.Builder(this)
            .setTitle("Template Fields")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (generateAfterEdit) "Generate PDF" else "Update Preview", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                fillValues = values.copy(
                    displayName = nameInput.text.toString().trim().ifBlank { values.displayName },
                    address = addressInput.text.toString().trim().ifBlank { values.address },
                    barangayArea = barangayInput.text.toString().trim().ifBlank { values.barangayArea },
                    purpose = purposeInput.text.toString().trim().ifBlank { values.purpose },
                    issueDay = dayInput.text.toString().trim().ifBlank { values.issueDay },
                    issueMonth = monthInput.text.toString().trim().ifBlank { values.issueMonth },
                    issueYear = yearInput.text.toString().trim().ifBlank { values.issueYear },
                    signatureName = signatureInput.text.toString().trim().ifBlank { values.signatureName },
                    businessName = businessNameInput?.text?.toString()?.trim().orEmpty().ifBlank { values.businessName },
                    businessLocation = businessLocationInput?.text?.toString()?.trim().orEmpty().ifBlank { values.businessLocation },
                    validUntilYear = validYearInput?.text?.toString()?.trim().orEmpty().ifBlank { values.validUntilYear },
                    idNumber = idNumberInput?.text?.toString()?.trim().orEmpty().ifBlank { values.idNumber },
                    idAgeLabel = idAgeInput?.text?.toString()?.trim().orEmpty().ifBlank { values.idAgeLabel }
                )

                lifecycleScope.launch {
                    renderPreview()
                    dialog.dismiss()
                    if (generateAfterEdit) {
                        uploadCurrentPreviewAsPdf()
                    }
                }
            }
        }
        dialog.show()
    }

    private suspend fun uploadCurrentPreviewAsPdf() {
        val request = currentRequest ?: return
        val bitmap = previewBitmap ?: return
        try {
            binding.btnSavePdf.isEnabled = false
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = document.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            document.finishPage(page)

            val output = ByteArrayOutputStream()
            document.writeTo(output)
            document.close()

            val safeType = request.serviceName.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
            val safeName = fillValues?.displayName?.replace(Regex("[^A-Za-z0-9]+"), "_")?.trim('_')
                ?: request.userName.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
            val fileName = "generated/${request.id}/${safeType}_${safeName}.pdf"
            val bucket = SupabaseClient.client.storage["certificate"]
            bucket.upload(fileName, output.toByteArray(), upsert = true)
            val pdfUrl = bucket.publicUrl(fileName)

            SupabaseClient.client.postgrest["requests"].update({
                set("status", "Approved")
                set("pdf_url", pdfUrl)
            }) {
                filter {
                    eq("id", request.id)
                }
            }

            Toast.makeText(this, "Certificate approved and uploaded successfully.", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            Log.e("PrintCert", "Upload failed", e)
            Toast.makeText(this, "Failed to generate certificate: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            binding.btnSavePdf.isEnabled = true
        }
    }

    private suspend fun fetchResidentData(name: String): Resident? {
        return try {
            SupabaseClient.client.postgrest["residents"]
                .select {
                    filter { eq("name", name) }
                }
                .decodeSingleOrNull<Resident>()
        } catch (e: Exception) {
            Log.e("PrintCert", "Error fetching resident", e)
            null
        }
    }

    private suspend fun fetchUserProfile(userId: String): User? {
        return try {
            SupabaseClient.client.postgrest["users"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<User>()
        } catch (e: Exception) {
            Log.e("PrintCert", "Error fetching user profile", e)
            null
        }
    }

    private fun buildDefaultFillValues(): TemplateFillValues {
        val request = currentRequest ?: error("Missing request")
        val address = currentResident?.address ?: currentUserProfile?.address ?: "Barangay Pantal, Dagupan City"
        val purpose = request.purpose ?: "whatever legal purpose it may serve"
        val name = request.userName.ifBlank { "the applicant" }
        return TemplateFillValues(
            displayName = name,
            address = address,
            barangayArea = deriveBarangayArea(address),
            purpose = purpose,
            issueDay = getCurrentDay(),
            issueMonth = getCurrentMonth(),
            issueYear = getCurrentYear(),
            businessName = purpose,
            businessLocation = address,
            validUntilYear = getCurrentYear(),
            idNumber = "2024-${request.id.take(4).uppercase()}",
            idAgeLabel = currentResident?.age?.let { "AGE: $it" }
                ?: currentUserProfile?.age?.let { "AGE: $it" }
                ?: "AGE: N/A",
            signatureName = name
        )
    }

    private fun overlayTemplateFields(bitmap: Bitmap, request: RequestAdmin, values: TemplateFillValues) {
        val canvas = Canvas(bitmap)
        val requestType = request.serviceName.trim()
        when {
            requestType.contains("Indigency", true) -> overlayIndigency(canvas, bitmap, values)
            requestType.contains("Residency", true) -> overlayResidency(canvas, bitmap, values)
            requestType.contains("Business", true) -> overlayBusiness(canvas, bitmap, values)
            else -> overlayClearance(canvas, bitmap, values)
        }
    }

    private fun overlayIndigency(canvas: Canvas, bitmap: Bitmap, values: TemplateFillValues) {
        drawFittedText(
            canvas, bitmap,
            "${honorific(currentResident?.gender ?: currentUserProfile?.gender)} ${values.displayName}",
            0.67f, 0.445f, 0.020f, 0.28f, true
        )
        drawFittedText(
            canvas, bitmap,
            values.barangayArea,
            0.62f, 0.502f, 0.017f, 0.18f, false
        )
        drawFittedText(canvas, bitmap, values.issueDay, 0.28f, 0.755f, 0.017f, 0.08f, false)
        drawFittedText(canvas, bitmap, values.issueMonth, 0.43f, 0.755f, 0.017f, 0.14f, false)
        drawFittedText(canvas, bitmap, values.issueYear, 0.59f, 0.755f, 0.017f, 0.10f, false)
        drawFittedText(canvas, bitmap, values.signatureName, 0.19f, 0.925f, 0.017f, 0.22f, false)
    }

    private fun overlayResidency(canvas: Canvas, bitmap: Bitmap, values: TemplateFillValues) {
        drawFittedText(
            canvas, bitmap,
            "${honorific(currentResident?.gender ?: currentUserProfile?.gender)} ${values.displayName}",
            0.53f, 0.424f, 0.019f, 0.30f, true
        )
        drawFittedText(canvas, bitmap, values.issueDay, 0.24f, 0.708f, 0.017f, 0.08f, false)
        drawFittedText(canvas, bitmap, values.issueMonth, 0.39f, 0.708f, 0.017f, 0.14f, false)
        drawFittedText(canvas, bitmap, values.issueYear, 0.58f, 0.708f, 0.017f, 0.10f, false)
        drawFittedText(canvas, bitmap, values.signatureName, 0.19f, 0.89f, 0.017f, 0.22f, false)
    }

    private fun overlayClearance(canvas: Canvas, bitmap: Bitmap, values: TemplateFillValues) {
        drawFittedText(
            canvas, bitmap,
            "${honorific(currentResident?.gender ?: currentUserProfile?.gender)} ${values.displayName}",
            0.54f, 0.425f, 0.019f, 0.31f, true
        )
        drawFittedText(canvas, bitmap, values.issueDay, 0.24f, 0.708f, 0.017f, 0.08f, false)
        drawFittedText(canvas, bitmap, values.issueMonth, 0.39f, 0.708f, 0.017f, 0.14f, false)
        drawFittedText(canvas, bitmap, values.issueYear, 0.58f, 0.708f, 0.017f, 0.10f, false)
        drawFittedText(canvas, bitmap, values.signatureName, 0.19f, 0.89f, 0.017f, 0.22f, false)
    }

    private fun overlayBusiness(canvas: Canvas, bitmap: Bitmap, values: TemplateFillValues) {
        drawCenteredFittedText(canvas, bitmap, values.displayName, 0.50f, 0.386f, 0.026f, 0.42f, true)
        drawCenteredFittedText(canvas, bitmap, values.businessName, 0.50f, 0.498f, 0.024f, 0.42f, true)
        drawCenteredFittedText(canvas, bitmap, values.businessLocation, 0.50f, 0.607f, 0.021f, 0.45f, false)
        drawFittedText(canvas, bitmap, values.validUntilYear, 0.64f, 0.681f, 0.017f, 0.09f, false)
        drawFittedText(canvas, bitmap, values.issueDay, 0.26f, 0.744f, 0.017f, 0.08f, false)
        drawFittedText(canvas, bitmap, values.issueMonth, 0.43f, 0.744f, 0.017f, 0.14f, false)
        drawFittedText(canvas, bitmap, values.issueYear, 0.60f, 0.744f, 0.017f, 0.10f, false)
    }

    private fun drawText(
        canvas: Canvas,
        bitmap: Bitmap,
        text: String,
        xRatio: Float,
        yRatio: Float,
        sizeRatio: Float,
        bold: Boolean = false
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = bitmap.width * sizeRatio
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        canvas.drawText(text, bitmap.width * xRatio, bitmap.height * yRatio, paint)
    }

    private fun drawCenteredText(
        canvas: Canvas,
        bitmap: Bitmap,
        text: String,
        centerXRatio: Float,
        yRatio: Float,
        sizeRatio: Float,
        bold: Boolean
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = bitmap.width * sizeRatio
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(text, bitmap.width * centerXRatio, bitmap.height * yRatio, paint)
    }

    private fun drawFittedText(
        canvas: Canvas,
        bitmap: Bitmap,
        text: String,
        xRatio: Float,
        yRatio: Float,
        sizeRatio: Float,
        maxWidthRatio: Float,
        bold: Boolean
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = bitmap.width * sizeRatio
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val maxWidth = bitmap.width * maxWidthRatio
        while (paint.measureText(text) > maxWidth && paint.textSize > bitmap.width * 0.010f) {
            paint.textSize *= 0.95f
        }
        canvas.drawText(text, bitmap.width * xRatio, bitmap.height * yRatio, paint)
    }

    private fun drawCenteredFittedText(
        canvas: Canvas,
        bitmap: Bitmap,
        text: String,
        centerXRatio: Float,
        yRatio: Float,
        sizeRatio: Float,
        maxWidthRatio: Float,
        bold: Boolean
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = bitmap.width * sizeRatio
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }
        val maxWidth = bitmap.width * maxWidthRatio
        while (paint.measureText(text) > maxWidth && paint.textSize > bitmap.width * 0.010f) {
            paint.textSize *= 0.95f
        }
        canvas.drawText(text, bitmap.width * centerXRatio, bitmap.height * yRatio, paint)
    }

    private fun downloadTemplateToCache(url: String, requestId: String): File {
        val target = File(cacheDir, "template_${requestId}_${url.hashCode()}.pdf")
        URL(url).openStream().use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }
        return target
    }

    private fun renderFirstPage(file: File): Bitmap {
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val page = renderer.openPage(0)
        val scale = 2
        val bitmap = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        descriptor.close()
        return bitmap
    }

    private fun deriveBarangayArea(address: String): String {
        if (address.isBlank()) return "Barangay Pantal"
        val parts = address.split(",").map { it.trim() }.filter { it.isNotBlank() }
        return parts.take(2).joinToString(", ").ifBlank { address }
    }

    private fun honorific(gender: String?): String {
        return when (gender?.trim()?.lowercase(Locale.getDefault())) {
            "male" -> "Mr."
            "female" -> "Ms."
            else -> "Mr./Ms."
        }
    }

    private fun getCurrentDay(): String = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
    private fun getCurrentMonth(): String = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
    private fun getCurrentYear(): String = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

    private data class TemplateFillValues(
        val displayName: String,
        val address: String,
        val barangayArea: String,
        val purpose: String,
        val issueDay: String,
        val issueMonth: String,
        val issueYear: String,
        val businessName: String,
        val businessLocation: String,
        val validUntilYear: String,
        val idNumber: String,
        val idAgeLabel: String,
        val signatureName: String
    )
}
