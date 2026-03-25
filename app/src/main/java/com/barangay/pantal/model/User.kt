package com.barangay.pantal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    @SerialName("full_name")
    val fullName: String = "",
    val email: String = "",
    val role: String = "user",
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val occupation: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("image_url")
    val profilePictureUrl: String? = null
)
