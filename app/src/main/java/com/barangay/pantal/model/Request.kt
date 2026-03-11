package com.barangay.pantal.model

import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = "",
    val date: String = "",
    val purpose: String? = null,
    val status: String = "Pending",
    val timestamp: Long? = null
)
