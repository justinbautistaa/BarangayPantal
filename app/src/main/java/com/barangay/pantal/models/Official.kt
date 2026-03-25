package com.barangay.pantal.models

import kotlinx.serialization.Serializable

@Serializable
data class Official(
    val name: String = "",
    val position: String = "",
    val imageUrl: String = ""
)