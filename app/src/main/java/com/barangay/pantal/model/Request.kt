package com.barangay.pantal.model

data class Request(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = "",
    val date: String = "",
    val purpose: String? = null,
    val status: String = "",
    val timestamp: Long? = null
)