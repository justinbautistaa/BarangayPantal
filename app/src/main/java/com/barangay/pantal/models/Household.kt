package com.barangay.pantal.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "households")
data class Household(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val householdId: String,
    val address: String,
    val headOfFamily: String,
    val totalMembers: Int = 1
)