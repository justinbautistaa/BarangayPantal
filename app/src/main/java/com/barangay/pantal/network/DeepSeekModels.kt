package com.barangay.pantal.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekRequest(
    @SerialName("model") val model: String = "deepseek-chat",
    @SerialName("messages") val messages: List<DeepSeekMessage>,
    @SerialName("stream") val stream: Boolean = false
)

@Serializable
data class DeepSeekMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@Serializable
data class DeepSeekResponse(
    @SerialName("id") val id: String,
    @SerialName("choices") val choices: List<DeepSeekChoice>
)

@Serializable
data class DeepSeekChoice(
    @SerialName("message") val message: DeepSeekMessage,
    @SerialName("finish_reason") val finishReason: String
)
