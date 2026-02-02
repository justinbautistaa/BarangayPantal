package com.barangay.pantal.model

data class Service(
    val name: String = "",
    val description: String = "",
    val requirements: List<String> = emptyList(),
    val processingTime: String = "",
    var isExpanded: Boolean = false
)
