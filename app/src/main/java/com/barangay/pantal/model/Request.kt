package com.barangay.pantal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("type")
    val type: String = "",
    @SerialName("date")
    val date: String? = null,
    @SerialName("purpose")
    val purpose: String? = null,
    @SerialName("status")
    val status: String = "Pending",
    @SerialName("timestamp")
    val timestamp: Long? = null,
    @SerialName("pdf_url")
    val pdfUrl: String? = null
)
