package com.barangay.pantal.model

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable
import java.util.UUID

@Serializable
data class Service(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val category: String? = null,
    val schedule: String? = null,
    @kotlinx.serialization.SerialName("contact_info")
    val contactInfo: String? = null,
    val icon: String? = null,
    val status: String? = null,
    val venue: String? = null,
    val requirements: List<String> = emptyList(),
    val howToAvail: String? = null,
    @kotlinx.serialization.SerialName("created_at")
    val createdAt: String? = null,
    @kotlinx.serialization.SerialName("updated_at")
    val updatedAt: String? = null
) : JavaSerializable
