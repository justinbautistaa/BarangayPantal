package com.barangay.pantal.model
import java.util.UUID

data class HouseholdMember(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val age: Int = 0,
    val occupation: String = "",
    val role: String = ""
)