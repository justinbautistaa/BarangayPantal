package com.barangay.pantal.model

data class RequestAdmin(
    val key: String = "",
    val userId: String = "",
    val serviceName: String = "",
    val status: String = "",
    val timestamp: Long = 0
)
