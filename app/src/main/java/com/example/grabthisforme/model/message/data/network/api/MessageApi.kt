package com.example.grabthisforme.model.message.data.network.api

import com.example.grabthisforme.model.message.data.network.dto.MessageDto
import com.example.grabthisforme.model.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {

    @POST("api/conversations/{conversationId}/messages")
    suspend fun sendMessage(
        @Path("conversationId") conversationId: String,
        @Body request: SendMessageRequest
    ): ApiResponse<MessageDto>
}

data class SendMessageRequest(
    val type: String,
    val content: String? = null,
    val mediaUrl: String? = null
)
