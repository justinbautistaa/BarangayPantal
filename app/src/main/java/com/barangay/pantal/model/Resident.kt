package com.barangay.pantal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Resident(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val address: String = "",
    val occupation: String = "",
    @SerialName("is_voter")
    val isVoter: Boolean = false,
    @SerialName("is_senior")
    val isSenior: Boolean = false,
    @SerialName("is_pwd")
    val isPwd: Boolean = false,
    @SerialName("image_url")
    val imageUrl: String? = null
)
