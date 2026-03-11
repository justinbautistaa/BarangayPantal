package com.barangay.pantal.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Service(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = ""
)
