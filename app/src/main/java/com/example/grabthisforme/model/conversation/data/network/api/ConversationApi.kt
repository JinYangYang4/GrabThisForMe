package com.example.grabthisforme.model.conversation.data.network.api

import com.example.grabthisforme.model.conversation.data.network.dto.ConversationDto
import com.example.grabthisforme.model.message.data.network.dto.MessageDto
import com.example.grabthisforme.model.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ConversationApi {

    @GET("api/conversations")
    suspend fun listConversations(): ApiResponse<List<ConversationDto>>

    @GET("api/conversations/{conversationId}/messages")
    suspend fun listMessages(
        @Path("conversationId") conversationId: String,
        @Query("beforeTime") beforeTime: Long? = null,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<MessageDto>>

    @POST("api/conversations/single")
    suspend fun createSingleConversation(@Body request: CreateSingleConversationRequest): ApiResponse<ConversationDto>

    @POST("api/conversations/{conversationId}/read")
    suspend fun markRead(
        @Path("conversationId") conversationId: String,
        @Body request: MarkConversationReadRequest
    ): ApiResponse<Unit>

    @POST("api/conversations/{conversationId}/hidden")
    suspend fun setHidden(
        @Path("conversationId") conversationId: String,
        @Body request: SetConversationHiddenRequest
    ): ApiResponse<Unit>
}

data class CreateSingleConversationRequest(
    val peerUserId: Long
)

data class SetConversationHiddenRequest(
    val hidden: Boolean
)

data class MarkConversationReadRequest(
    val lastReadTime: Long? = null
)
