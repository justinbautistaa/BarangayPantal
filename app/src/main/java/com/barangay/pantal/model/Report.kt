package com.barangay.pantal.model

data class Report(
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val reporterId: String = "",
    val details: String = "",
    val priority: String = "",
    val timestamp: Long = 0
)
