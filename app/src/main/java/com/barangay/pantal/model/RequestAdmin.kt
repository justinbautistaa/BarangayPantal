package com.barangay.pantal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class RequestAdmin(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("name")
    val userName: String = "",
    @SerialName("type")
    val serviceName: String = "",
    @SerialName("date")
    val date: String? = null,
    @SerialName("purpose")
    val purpose: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("civil_status")
    val civilStatus: String? = null,
    @SerialName("monthly_income")
    val monthlyIncome: Double? = null,
    @SerialName("business_name")
    val businessName: String? = null,
    @SerialName("business_location")
    val businessLocation: String? = null,
    @SerialName("certificate_name")
    val certificateName: String? = null,
    @SerialName("certificate_age")
    val certificateAge: Int? = null,
    @SerialName("certificate_citizenship")
    val certificateCitizenship: String? = null,
    @SerialName("barangay_area")
    val barangayArea: String? = null,
    @SerialName("specimen_signature")
    val specimenSignature: String? = null,
    @SerialName("issue_date")
    val issueDate: String? = null,
    @SerialName("punong_barangay_name")
    val punongBarangayName: String? = null,
    val status: String = "",
    val timestamp: Long? = null,
    @SerialName("pdf_url")
    val pdfUrl: String? = null
) : JavaSerializable
