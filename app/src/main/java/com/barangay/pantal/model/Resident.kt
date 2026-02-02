package com.barangay.pantal.model

data class Resident(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val address: String = "",
    val occupation: String = "",
    val isVoter: Boolean = false,
    val isSenior: Boolean = false,
    val isPwd: Boolean = false
)
