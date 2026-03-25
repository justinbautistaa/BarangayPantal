package com.barangay.pantal.model

data class ProgramService(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val requirements: List<String>,
    val schedule: String,
    val venue: String,
    val contact: String,
    val duration: String,
    val benefits: List<String>
)
