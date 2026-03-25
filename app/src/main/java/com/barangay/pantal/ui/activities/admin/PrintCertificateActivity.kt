package com.barangay.pantal.ui.activities.admin

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import com.barangay.pantal.databinding.ActivityPrintCertificateBinding
import com.barangay.pantal.model.RequestAdmin
import com.barangay.pantal.model.RequestTemplates
import com.barangay.pantal.model.Resident
import com.barangay.pantal.model.User
import com.barangay.pantal.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrintCertificateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrintCertificateBinding
    private var currentRequest: RequestAdmin? = null
    private var currentResident: Resident? = null
    private var currentUserProfile: User? = null
    private var fillValues: TemplateFillValues? = null
    private var initialFillValues: TemplateFillValues? = null
    private var previewBitmap: Bitmap? = null
    private var isViewOnly: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrintCertificateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        currentRequest = intent.getSerializableExtra("request") as? RequestAdmin
        isViewOnly = intent.getBooleanExtra("view_only", false)
        if (currentRequest == null) {
            Toast.makeText(this, "No request data found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            loadSupportingData()
            fillValues = buildDefaultFillValues()
            initialFillValues = fillValues
            renderPreview()
        }

        if (isViewOnly) {
            binding.btnPrint.text = "Close"
            binding.btnPrint.isEnabled = true
            binding.btnPrint.setOnClickListener { finish() }
            binding.btnSavePdf.text = "Save as PDF"
            binding.btnSavePdf.setOnClickListener {
                lifecycleScope.launch {
                    saveCurrentCertificatePdf()
                }
            }
        } else {
            binding.btnPrint.text = "Edit Fields"
            binding.btnPrint.setOnClickListener {
                showTemplateFillDialog()
            }

            binding.btnSavePdf.text = "Approve & Save PDF"
            binding.btnSavePdf.setOnClickListener {
                showTemplateFillDialog(generateAfterEdit = true)
            }
        }

        binding.btnBack.setOnClickListener { handleBackNavigation() }
        onBackPressedDispatcher.addCallback(this) {
            handleBackNavigation()
        }
    }

    private suspend fun loadSupportingData() {
        val request = currentRequest ?: return
        currentUserProfile = fetchUserProfile(request.userId)
        currentResident = fetchResidentData(
            userId = request.userId,
            name = request.userName,
            email = currentUserProfile?.email
        )
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

    private fun handleBackNavigation() {
        if (fillValues != null && !isViewOnly && fillValues != initialFillValues) {
            AlertDialog.Builder(this)
                .setTitle("Discard certificate changes?")
                .setMessage("Your certificate edits are not approved yet. Going back will discard them.")
                .setNegativeButton("Stay", null)
                .setPositiveButton("Discard") { _, _ ->
                    setResult(RESULT_CANCELED)
                    finish()
                }
                .show()
            return
        }

        setResult(RESULT_CANCELED)
        finish()
    }

    private suspend fun generateFilledTemplateBitmap(
        request: RequestAdmin,
        values: TemplateFillValues
    ): Bitmap = withContext(Dispatchers.IO) {
        val bitmap = Bitmap.createBitmap(1240, 1754, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        drawDigitalCertificate(bitmap, request, values)
        bitmap
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

        val nameInput = addField("Full Name", values.displayName)
        val ageInput = addField("Age", values.age)
        val citizenshipInput = addField("Citizenship", values.citizenship)
        val addressInput = addField("Address", values.address)
        val barangayInput = addField("Barangay / Location", values.barangayArea)
        val purposeInput = addField("Purpose", values.purpose)
        val issueDateInput = addField("Issue Date (YYYY-MM-DD)", values.issueDate)
        val signatureInput = addField("Specimen Signature", values.specimenSignature)
        val captainInput = addField("Punong Barangay Name", values.punongBarangayName)
        val civilStatusInput = if (request.serviceName.contains("Indigency", true)) {
            addField("Civil Status", values.civilStatus)
        } else null
        val monthlyIncomeInput = if (request.serviceName.contains("Indigency", true)) {
            addField("Monthly Income", values.monthlyIncome)
        } else null

        val businessNameInput = if (request.serviceName.contains("Business", true)) {
            addField("Business Name", values.businessName)
        } else null
        val businessLocationInput = if (request.serviceName.contains("Business", true)) {
            addField("Business Location", values.businessLocation)
        } else null

        val dialog = AlertDialog.Builder(this)
            .setTitle("Certificate Fields")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (generateAfterEdit) "Generate PDF" else "Update Preview", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                fillValues = values.copy(
                    displayName = nameInput.text.toString().trim().ifBlank { values.displayName },
                    age = ageInput.text.toString().trim().ifBlank { values.age },
                    citizenship = citizenshipInput.text.toString().trim().ifBlank { values.citizenship },
                    address = addressInput.text.toString().trim().ifBlank { values.address },
                    barangayArea = barangayInput.text.toString().trim().ifBlank { values.barangayArea },
                    purpose = purposeInput.text.toString().trim().ifBlank { values.purpose },
                    issueDate = issueDateInput.text.toString().trim().ifBlank { values.issueDate },
                    specimenSignature = signatureInput.text.toString().trim().ifBlank { values.specimenSignature },
                    punongBarangayName = captainInput.text.toString().trim().ifBlank { values.punongBarangayName },
                    civilStatus = civilStatusInput?.text?.toString()?.trim().orEmpty().ifBlank { values.civilStatus },
                    monthlyIncome = monthlyIncomeInput?.text?.toString()?.trim().orEmpty().ifBlank { values.monthlyIncome },
                    businessName = businessNameInput?.text?.toString()?.trim().orEmpty().ifBlank { values.businessName },
                    businessLocation = businessLocationInput?.text?.toString()?.trim().orEmpty().ifBlank { values.businessLocation }
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
        val values = fillValues ?: buildDefaultFillValues()
        try {
            binding.btnSavePdf.isEnabled = false
            binding.btnPrint.isEnabled = false
            approveRequestRecord(request.id, values)

            saveCurrentCertificatePdf()
            Toast.makeText(this, "Certificate approved and saved to device.", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            Log.e("PrintCert", "Approval failed", e)
            Toast.makeText(this, "Failed to approve certificate: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            binding.btnSavePdf.isEnabled = true
            binding.btnPrint.isEnabled = true
        }
    }

    private suspend fun approveRequestRecord(requestId: String, values: TemplateFillValues) {
        try {
            SupabaseClient.client.postgrest["requests"].update({
                set("status", "Approved")
                set("pdf_url", null as String?)
                set("certificate_name", values.displayName)
                set("certificate_age", values.age.toIntOrNull())
                set("certificate_citizenship", values.citizenship)
                set("barangay_area", values.barangayArea)
                set("specimen_signature", values.specimenSignature)
                set("issue_date", values.issueDate)
                set("punong_barangay_name", values.punongBarangayName)
                set("civil_status", values.civilStatus)
                set("monthly_income", values.monthlyIncome.toDoubleOrNull())
                set("business_name", values.businessName.takeIf { it.isNotBlank() })
                set("business_location", values.businessLocation.takeIf { it.isNotBlank() })
            }) {
                filter { eq("id", requestId) }
            }
        } catch (schemaError: Exception) {
            Log.w("PrintCert", "Full request update failed, retrying with minimal fields", schemaError)
            SupabaseClient.client.postgrest["requests"].update({
                set("status", "Approved")
                set("pdf_url", null as String?)
                set("civil_status", values.civilStatus)
                set("monthly_income", values.monthlyIncome.toDoubleOrNull())
                set("business_name", values.businessName.takeIf { it.isNotBlank() })
                set("business_location", values.businessLocation.takeIf { it.isNotBlank() })
            }) {
                filter { eq("id", requestId) }
            }
        }
    }

    private suspend fun saveCurrentCertificatePdf() {
        val request = currentRequest ?: return
        val values = fillValues ?: buildDefaultFillValues()
        val bitmap = previewBitmap ?: generateFilledTemplateBitmap(request, values)
        val fileName = buildCertificateFileName(request, values)

        withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            try {
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)

                val successMessage: String
                val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    successMessage = "Certificate saved to Downloads folder"
                    uri?.let { contentResolver.openOutputStream(it) }
                } else {
                    val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
                    val outputFile = File(downloadsDir, fileName)
                    successMessage = "Certificate saved to ${outputFile.absolutePath}"
                    FileOutputStream(outputFile)
                }

                outputStream?.use { pdfDocument.writeTo(it) }
                    ?: throw IllegalStateException("Unable to open output stream for PDF")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PrintCertificateActivity, successMessage, Toast.LENGTH_LONG).show()
                }
            } finally {
                pdfDocument.close()
            }
        }
    }

    private fun buildCertificateFileName(request: RequestAdmin, values: TemplateFillValues): String {
        val safeType = request.serviceName.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
        val safeName = values.displayName.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
        return "${safeType.ifBlank { "Certificate" }}_${safeName.ifBlank { "Resident" }}.pdf"
    }

    private suspend fun fetchResidentData(userId: String? = null, name: String, email: String? = null): Resident? {
        return try {
            val byUserId = userId?.takeIf { it.isNotBlank() }?.let { linkedUserId ->
                SupabaseClient.client.postgrest["residents"]
                    .select {
                        filter { eq("user_id", linkedUserId) }
                    }
                    .decodeSingleOrNull<Resident>()
            }

            val byName = byUserId ?: SupabaseClient.client.postgrest["residents"]
                .select {
                    filter { eq("name", name) }
                }
                .decodeSingleOrNull<Resident>()

            byName ?: email?.takeIf { it.isNotBlank() }?.let { residentEmail ->
                SupabaseClient.client.postgrest["residents"]
                    .select {
                        filter { eq("email", residentEmail) }
                    }
                    .decodeSingleOrNull<Resident>()
            }
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
        val address = request.address?.takeIf { it.isNotBlank() }
            ?: currentResident?.address?.takeIf { it.isNotBlank() }
            ?: currentUserProfile?.address?.takeIf { it.isNotBlank() }
            ?: "Barangay Pantal, Dagupan City"
        val purpose = request.purpose?.takeIf { it.isNotBlank() } ?: "whatever legal purpose it may serve"
        val name = request.certificateName?.takeIf { it.isNotBlank() } ?: request.userName.ifBlank {
            currentResident?.name?.takeIf { it.isNotBlank() }
                ?: currentUserProfile?.fullName?.takeIf { it.isNotBlank() }
                ?: "the applicant"
        }
        return TemplateFillValues(
            displayName = name,
            age = request.certificateAge?.toString()
                ?: currentResident?.age?.toString()
                ?: currentUserProfile?.age?.toString()
                ?: "",
            citizenship = request.certificateCitizenship?.takeIf { it.isNotBlank() } ?: "Filipino",
            address = address,
            barangayArea = request.barangayArea?.takeIf { it.isNotBlank() } ?: deriveBarangayArea(address),
            civilStatus = request.civilStatus?.takeIf { it.isNotBlank() }
                ?: currentResident?.civilStatus?.takeIf { it.isNotBlank() }
                ?: "Single",
            purpose = purpose,
            issueDate = request.issueDate?.takeIf { it.isNotBlank() } ?: getTodayIsoDate(),
            specimenSignature = request.specimenSignature?.takeIf { it.isNotBlank() } ?: name,
            punongBarangayName = request.punongBarangayName?.takeIf { it.isNotBlank() }
                ?: "HON. ROMULO S. VERGARA",
            businessName = request.businessName?.takeIf { it.isNotBlank() } ?: purpose,
            businessLocation = request.businessLocation?.takeIf { it.isNotBlank() } ?: address,
            monthlyIncome = request.monthlyIncome?.toString() ?: ""
        )
    }

    private fun drawDigitalCertificate(bitmap: Bitmap, request: RequestAdmin, values: TemplateFillValues) {
        val canvas = Canvas(bitmap)
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val marginX = width * 0.10f
        val contentWidth = width - (marginX * 2)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.LEFT
            typeface = Typeface.SERIF
        }
        val subtlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(88, 96, 112)
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }

        var y = height * 0.08f
        titlePaint.textSize = width * 0.019f
        canvas.drawText("Republic of the Philippines", width / 2f, y, titlePaint)
        y += height * 0.028f
        titlePaint.textSize = width * 0.022f
        canvas.drawText("CITY OF DAGUPAN", width / 2f, y, titlePaint)
        y += height * 0.026f
        titlePaint.textSize = width * 0.019f
        canvas.drawText("Province of Pangasinan", width / 2f, y, titlePaint)
        y += height * 0.032f
        canvas.drawLine(marginX, y, width - marginX, y, Paint().apply {
            color = Color.rgb(196, 203, 214)
            strokeWidth = 2f
        })

        y += height * 0.060f
        titlePaint.textSize = width * 0.036f
        canvas.drawText("BARANGAY PANTAL", width / 2f, y, titlePaint)
        y += height * 0.030f
        bodyPaint.textSize = width * 0.016f
        bodyPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Office of the Punong Barangay", width / 2f, y, bodyPaint)
        y += height * 0.070f

        titlePaint.textSize = width * 0.027f
        canvas.drawText(resolveCertificateTitle(request.serviceName), width / 2f, y, titlePaint)
        y += height * 0.060f

        bodyPaint.textAlign = Paint.Align.LEFT
        bodyPaint.textSize = width * 0.018f
        canvas.drawText("To Whom It May Concern:", marginX, y, bodyPaint)
        y += height * 0.050f

        val paragraphs = buildCertificateParagraphs(request.serviceName, values)
        paragraphs.forEach { paragraph ->
            y = drawWrappedParagraph(
                canvas = canvas,
                text = paragraph,
                x = marginX,
                y = y,
                width = contentWidth,
                paint = bodyPaint,
                indent = width * 0.05f
            )
            y += height * 0.018f
        }

        subtlePaint.textSize = width * 0.012f
        canvas.drawText("Resident Specimen Signature", marginX, y, subtlePaint)
        y += height * 0.020f
        canvas.drawLine(marginX, y, marginX + (width * 0.28f), y, Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        })
        bodyPaint.textSize = width * 0.019f
        canvas.drawText(values.specimenSignature.ifBlank { values.displayName }, marginX + 10f, y - 10f, bodyPaint)
        y += height * 0.060f

        y = drawWrappedParagraph(
            canvas = canvas,
            text = "ISSUED this ${formatIssueDateLong(values.issueDate)} at Barangay Pantal, Dagupan City, Pangasinan.",
            x = marginX,
            y = y,
            width = contentWidth,
            paint = bodyPaint,
            indent = 0f
        )

        val footerY = maxOf(height * 0.83f, y + (height * 0.060f))
        subtlePaint.textSize = width * 0.011f
        canvas.drawText("Issue Date", marginX, footerY, subtlePaint)
        bodyPaint.textSize = width * 0.018f
        canvas.drawText(formatIssueDateLong(values.issueDate), marginX, footerY + (height * 0.022f), bodyPaint)

        val lineStartX = width - marginX - (width * 0.30f)
        val lineEndX = width - marginX
        val lineY = footerY + (height * 0.020f)
        canvas.drawLine(lineStartX, lineY, lineEndX, lineY, Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        })

        bodyPaint.textAlign = Paint.Align.CENTER
        bodyPaint.typeface = Typeface.DEFAULT_BOLD
        bodyPaint.textSize = width * 0.017f
        canvas.drawText(values.punongBarangayName, (lineStartX + lineEndX) / 2f, lineY - 8f, bodyPaint)
        subtlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Punong Barangay", (lineStartX + lineEndX) / 2f, lineY + (height * 0.026f), subtlePaint)
    }

    private fun resolveCertificateTitle(serviceName: String): String {
        return when {
            serviceName.contains("Indigency", true) -> "CERTIFICATE OF INDIGENCY"
            serviceName.contains("Residency", true) -> "CERTIFICATE OF RESIDENCY"
            serviceName.contains("Business", true) -> "BUSINESS CLEARANCE"
            else -> "BARANGAY CLEARANCE"
        }
    }

    private fun buildCertificateParagraphs(serviceName: String, values: TemplateFillValues): List<String> {
        return when {
            serviceName.contains("Residency", true) -> listOf(
                "This is to CERTIFY that ${values.displayName}, ${values.age.ifBlank { "legal" }} years of age, ${values.citizenship} citizen, whose SPECIMEN SIGNATURE appears below, is a PERMANENT RESIDENT of Barangay ${values.barangayArea}, Dagupan City, Pangasinan.",
                "This CERTIFICATION is being issued upon the request of the above-named person for whatever legal purpose it may serve."
            )
            serviceName.contains("Indigency", true) -> {
                val incomeValue = values.monthlyIncome.toDoubleOrNull()
                val incomeLine = if (incomeValue != null) {
                    " with a declared monthly income of Php ${String.format(Locale.US, "%,.2f", incomeValue)}"
                } else {
                    ""
                }
                listOf(
                    "This is to CERTIFY that ${values.displayName}, ${values.age.ifBlank { "legal" }} years of age, ${values.civilStatus.lowercase(Locale.getDefault())}, ${values.citizenship} citizen, and a resident of Barangay ${values.barangayArea}, belongs to the indigent families of this barangay$incomeLine.",
                    "This CERTIFICATION is being issued upon the request of the above-named person for whatever legal purpose it may serve."
                )
            }
            serviceName.contains("Business", true) -> listOf(
                "This is to CERTIFY that ${values.displayName}, ${values.citizenship} citizen, is applying for a business clearance for ${values.businessName.ifBlank { "the declared business" }} located at ${values.businessLocation}.",
                "This CLEARANCE is being issued upon the request of the above-named person for whatever legal purpose it may serve."
            )
            else -> listOf(
                "This is to CERTIFY that ${values.displayName}, ${values.age.ifBlank { "legal" }} years of age, ${values.citizenship} citizen, and a resident of Barangay ${values.barangayArea}, has no pending case nor derogatory record in this barangay.",
                "This CERTIFICATION is being issued upon the request of the above-named person for whatever legal purpose it may serve."
            )
        }
    }

    private fun drawWrappedParagraph(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Float,
        paint: Paint,
        indent: Float
    ): Float {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val lines = mutableListOf<String>()
        var currentLine = ""
        words.forEach { word ->
            val candidate = if (currentLine.isBlank()) word else "$currentLine $word"
            val lineWidth = paint.measureText(candidate)
            val allowedWidth = if (lines.isEmpty()) width - indent else width
            if (lineWidth <= allowedWidth) {
                currentLine = candidate
            } else {
                if (currentLine.isNotBlank()) lines += currentLine
                currentLine = word
            }
        }
        if (currentLine.isNotBlank()) lines += currentLine

        var drawY = y
        val lineHeight = paint.textSize * 1.55f
        lines.forEachIndexed { index, line ->
            val drawX = if (index == 0) x + indent else x
            canvas.drawText(line, drawX, drawY, paint)
            drawY += lineHeight
        }
        return drawY
    }

    private fun copyTemplateAssetToCache(assetPath: String, requestId: String): File {
        val target = File(cacheDir, "template_${requestId}_${assetPath.hashCode()}.pdf")
        assets.open(assetPath).use { input ->
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
        if (address.isBlank()) return "Pantal"

        val barangayMatch = Regex("barangay\\s+([^,]+)", RegexOption.IGNORE_CASE).find(address)
        if (barangayMatch != null) {
            return barangayMatch.groupValues[1].trim().ifBlank { "Pantal" }
        }

        val parts = address.split(",").map { it.trim() }.filter { it.isNotBlank() }
        return parts.lastOrNull().orEmpty().ifBlank { "Pantal" }
    }

    private fun honorific(gender: String?): String {
        return when (gender?.trim()?.lowercase(Locale.getDefault())) {
            "male" -> "Mr."
            "female" -> "Ms."
            else -> "Mr./Ms."
        }
    }

    private fun getTodayIsoDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun formatIssueDateLong(value: String): String {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fallback = Date()
        val parsed = runCatching { parser.parse(value) }.getOrNull() ?: fallback
        return SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(parsed)
    }

    private fun getCurrentDay(): String = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
    private fun getCurrentMonth(): String = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
    private fun getCurrentYear(): String = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

    private data class TemplateFillValues(
        val displayName: String,
        val age: String,
        val citizenship: String,
        val address: String,
        val barangayArea: String,
        val civilStatus: String,
        val purpose: String,
        val issueDate: String,
        val specimenSignature: String,
        val punongBarangayName: String,
        val businessName: String,
        val businessLocation: String,
        val monthlyIncome: String
    )
}
