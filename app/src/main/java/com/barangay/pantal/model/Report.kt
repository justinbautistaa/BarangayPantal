package com.barangay.pantal.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Report(
    val id: String = UUID.randomUUID().toString(),
    val reporterId: String = "",
    val reporterName: String = "",
    val type: String = "",
    val details: String = "",
    val status: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)
