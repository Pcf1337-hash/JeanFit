package com.jeanfit.app.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ClaudeApi {
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: ClaudeRequest
    ): Response<ClaudeResponse>
}

@JsonClass(generateAdapter = true)
data class ClaudeRequest(
    @Json(name = "model") val model: String = "claude-haiku-4-5-20251001",
    @Json(name = "max_tokens") val maxTokens: Int = 512,
    @Json(name = "system") val system: String,
    @Json(name = "messages") val messages: List<ClaudeMessage>
)

@JsonClass(generateAdapter = true)
data class ClaudeMessage(
    @Json(name = "role") val role: String,      // "user" | "assistant"
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ClaudeResponse(
    @Json(name = "content") val content: List<ClaudeContentBlock>,
    @Json(name = "usage") val usage: ClaudeUsage?
)

@JsonClass(generateAdapter = true)
data class ClaudeContentBlock(
    @Json(name = "type") val type: String,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class ClaudeUsage(
    @Json(name = "input_tokens") val inputTokens: Int,
    @Json(name = "output_tokens") val outputTokens: Int
)
