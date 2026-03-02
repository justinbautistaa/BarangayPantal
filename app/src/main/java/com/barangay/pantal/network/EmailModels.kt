package com.barangay.pantal.network

import com.google.gson.annotations.SerializedName

data class SendGridRequest(
    @SerializedName("personalizations") val personalizations: List<Personalization>,
    @SerializedName("from") val from: EmailUser,
    @SerializedName("subject") val subject: String,
    @SerializedName("content") val content: List<Content>
)

data class Personalization(
    @SerializedName("to") val to: List<EmailUser>
)

data class EmailUser(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String? = null
)

data class Content(
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: String
)
