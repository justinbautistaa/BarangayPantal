package com.barangay.pantal.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ActivityLog(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val details: String = "",
    val user: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
