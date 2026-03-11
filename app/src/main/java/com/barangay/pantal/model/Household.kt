package com.barangay.pantal.model

import kotlinx.serialization.Serializable

@Serializable
data class Household(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val members: List<HouseholdMember> = emptyList()
)
