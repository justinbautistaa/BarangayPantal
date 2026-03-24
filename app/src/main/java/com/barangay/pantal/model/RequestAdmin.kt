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
    val status: String = "",
    val timestamp: Long? = null,
    @SerialName("pdf_url")
    val pdfUrl: String? = null
) : JavaSerializable
