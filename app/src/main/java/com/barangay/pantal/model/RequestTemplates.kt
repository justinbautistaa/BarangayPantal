package com.barangay.pantal.model

object RequestTemplates {
    fun pdfUrlFor(requestType: String): String? {
        return when (requestType.trim()) {
            "Business Permit", "Business Permit (Barangay)" ->
                "https://pjejgvuhxlubtuurilqz.supabase.co/storage/v1/object/public/certificate/templates/business_permit_template.pdf"
            "Barangay Clearance" ->
                "https://pjejgvuhxlubtuurilqz.supabase.co/storage/v1/object/public/certificate/templates/clearance_template.pdf"
            "Certificate of Indigency", "Barangay Indigency" ->
                "https://pjejgvuhxlubtuurilqz.supabase.co/storage/v1/object/public/certificate/templates/indigency_template.pdf"
            "Certificate of Residency" ->
                "https://pjejgvuhxlubtuurilqz.supabase.co/storage/v1/object/public/certificate/templates/residency_template.pdf"
            else -> null
        }
    }
}
