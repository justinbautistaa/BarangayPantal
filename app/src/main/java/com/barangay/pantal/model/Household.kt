package com.barangay.pantal.model

data class Household(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val members: List<HouseholdMember> = emptyList()
)