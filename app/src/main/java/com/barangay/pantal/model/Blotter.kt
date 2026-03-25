package com.barangay.pantal.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.util.UUID

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Blotter(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("blotter_number")
    @JsonNames("blotterNumber")
    val blotterNumber: String = "",
    @SerialName("complainant_id")
    @JsonNames("complainantId")
    val complainantId: String = "",
    @SerialName("complainant_name")
    @JsonNames("complainantName")
    val complainantName: String = "",
    val respondent: String = "",
    @SerialName("incident_date")
    @JsonNames("incidentDate")
    val incidentDate: String? = null,
    @SerialName("incident_location")
    @JsonNames("incidentLocation")
    val incidentLocation: String? = null,
    val complaint: String = "",
    val witnesses: String? = null,
    val priority: String = "normal",
    val status: String = "pending",
    val resolution: String? = null,
    @SerialName("resolved_at")
    @JsonNames("resolvedAt")
    val resolvedAt: String? = null,
    @SerialName("dismiss_reason")
    @JsonNames("dismissReason")
    val dismissReason: String? = null,
    @SerialName("dismissed_at")
    @JsonNames("dismissedAt")
    val dismissedAt: String? = null,
    @SerialName("scheduled_at")
    @JsonNames("scheduledAt")
    val scheduledAt: String? = null,
    @SerialName("created_at")
    @JsonNames("createdAt")
    val createdAt: String? = null
)

fun reportToBlotter(report: Report): Blotter {
    val normalizedStatus = when (report.status.orEmpty().trim().lowercase()) {
        "resolved", "completed" -> "resolved"
        "cancelled", "dismissed", "rejected" -> "dismissed"
        "in progress", "investigating", "scheduled" -> "investigating"
        else -> "pending"
    }

    val timestamp = report.timestamp ?: System.currentTimeMillis()

    return Blotter(
        id = report.id,
        blotterNumber = "RPT-${report.id.take(8).uppercase()}",
        complainantId = report.reporterId.orEmpty(),
        complainantName = report.reporterName.orEmpty(),
        respondent = report.type.orEmpty().ifBlank { "General concern" },
        complaint = report.details.orEmpty(),
        status = normalizedStatus,
        createdAt = java.time.Instant.ofEpochMilli(timestamp).toString()
    )
}
