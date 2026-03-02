package com.barangay.pantal.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val content: String = "",
    val priority: String = "",
    val timestamp: Long = 0
)
