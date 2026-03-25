package com.barangay.pantal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Resident(
    val id: String = "",
    @SerialName("user_id")
    val userId: String? = null,
    val name: String = "",
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val occupation: String? = null,
    val birthdate: String? = null,
    @SerialName("civil_status")
    val civilStatus: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("is_voter")
    val isVoter: Boolean = false,
    @SerialName("is_senior")
    val isSenior: Boolean = false,
    @SerialName("is_pwd")
    val isPwd: Boolean = false,
    @SerialName("profile_picture")
    val profilePicture: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
