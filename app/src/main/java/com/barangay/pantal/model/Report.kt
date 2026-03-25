package com.barangay.pantal.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.util.UUID

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Report(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("reporter_id")
    @JsonNames("reporterId")
    val reporterId: String? = null,
    @SerialName("reporter_name")
    @JsonNames("reporterName")
    val reporterName: String? = null,
    val type: String? = null,
    val details: String? = null,
    val status: String? = "Pending",
    val timestamp: Long? = System.currentTimeMillis()
)
