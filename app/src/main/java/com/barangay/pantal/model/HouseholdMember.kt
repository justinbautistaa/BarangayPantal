package com.barangay.pantal.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class HouseholdMember(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val age: Int = 0,
    val occupation: String = "",
    val role: String = ""
)
