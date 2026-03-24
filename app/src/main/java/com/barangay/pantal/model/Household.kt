package com.barangay.pantal.model

import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable

@Serializable
data class Household(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    @Transient
    val members: List<HouseholdMember> = emptyList()
)
