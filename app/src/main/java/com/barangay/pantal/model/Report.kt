package com.barangay.pantal.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Report(
    val id: String = UUID.randomUUID().toString(),
    val reporterId: String,
    val details: String,
    val timestamp: Long
)
