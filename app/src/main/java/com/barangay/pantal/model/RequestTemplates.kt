package com.barangay.pantal.model

object RequestTemplates {
    fun assetPathFor(requestType: String): String? {
        return when (requestType.trim()) {
            "Business Permit", "Business Permit (Barangay)", "Business Clearance" ->
                "templates/business-clearance.pdf"
            "Barangay Clearance" ->
                "templates/barangay-clearance.pdf"
            "Certificate of Indigency", "Barangay Indigency" ->
                "templates/certificate-of-indigency.pdf"
            "Certificate of Residency" ->
                "templates/certificate-of-residency.pdf"
            else -> null
        }
    }
}
