package com.barangay.pantal.model

data class Report(
    val reporterId: String = "",
    val reportType: String = "Blotter",
    val details: String = "",
    val timestamp: Long = 0,
    val title: String = "",
    val date: String = ""
)
