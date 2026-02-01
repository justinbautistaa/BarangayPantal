package com.barangay.pantal.model

data class Announcement(
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val priority: String = "",
    val timestamp: Long = 0
)

data class RecentActivity(
    val title: String = "",
    val user: String = "",
    val timestamp: String = ""
)

data class Report(
    val reporterId: String = "",
    val reportType: String = "Blotter",
    val details: String = "",
    val timestamp: Long = 0,
    val title: String = "",
    val date: String = ""
)

data class RequestAdmin(
    val key: String = "",
    val userId: String = "",
    val serviceName: String = "",
    val status: String = "",
    val timestamp: Long = 0
)

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

data class Service(
    val name: String = "",
    val description: String = "",
    val requirements: List<String> = emptyList(),
    val processingTime: String = "",
    var isExpanded: Boolean = false
)
