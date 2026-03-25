package com.barangay.pantal.util

import android.util.Patterns
import java.util.Locale

object ValidationUtils {
    fun cleanText(value: String?, maxLength: Int = Int.MAX_VALUE): String {
        val cleaned = value.orEmpty()
            .replace(Regex("[\\u0000-\\u001F\\u007F]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (cleaned.length > maxLength) cleaned.take(maxLength) else cleaned
    }

    fun isValidEmail(value: String?): Boolean {
        val cleaned = cleanText(value, 160).lowercase(Locale.getDefault())
        return cleaned.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(cleaned).matches()
    }

    fun isValidGmail(value: String?): Boolean {
        val cleaned = cleanText(value, 160).lowercase(Locale.getDefault())
        return isValidEmail(cleaned) && cleaned.endsWith("@gmail.com")
    }

    fun isStrongPassword(value: String?): Boolean = value.orEmpty().trim().length >= 8

    fun isValidPhone(value: String?): Boolean {
        val cleaned = cleanText(value, 20).replace(Regex("[^\\d+]"), "")
        return cleaned.isNotEmpty() && Regex("^\\+?\\d{10,15}$").matches(cleaned)
    }

    fun isLettersOnlyNameList(value: String?): Boolean {
        val cleaned = cleanText(value, 500)
        if (cleaned.isEmpty()) return true
        return Regex("^[A-Za-z.\\-,' ]+$").matches(cleaned)
    }
}
