package com.barangay.pantal.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "residents")
data class Resident(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val gender: String,
    val address: String,
    val occupation: String = "",
    val isVoter: Boolean = false,
    val isSenior: Boolean = false,
    val isPwd: Boolean = false
) {
    fun getFullName(): String = "$firstName $lastName"
}