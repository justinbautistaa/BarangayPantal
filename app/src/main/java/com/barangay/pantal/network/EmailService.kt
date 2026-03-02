package com.barangay.pantal.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface EmailService {
    @POST("mail/send")
    suspend fun sendEmail(
        @Header("Authorization") apiKey: String,
        @Body request: SendGridRequest
    ): Response<Unit>
}
