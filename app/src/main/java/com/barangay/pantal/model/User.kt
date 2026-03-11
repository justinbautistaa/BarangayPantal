package com.barangay.pantal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    @SerialName("full_name")
    val fullName: String = "",
    val email: String = "",
    val role: String = "user"
)
