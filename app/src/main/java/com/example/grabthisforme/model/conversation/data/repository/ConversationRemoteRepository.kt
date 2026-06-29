package com.example.grabthisforme.model.conversation.data.repository

import android.util.Log
import com.example.grabthisforme.model.conversation.data.network.api.ConversationApi
import com.example.grabthisforme.model.conversation.data.network.api.CreateSingleConversationRequest
import com.example.grabthisforme.model.conversation.data.network.api.MarkConversationReadRequest
import com.example.grabthisforme.model.conversation.data.network.api.SetConversationHiddenRequest
import com.example.grabthisforme.model.conversation.data.network.dto.ConversationDto
import com.example.grabthisforme.model.message.data.network.dto.MessageDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRemoteRepository @Inject constructor(
    private val conversationApi: ConversationApi
) {

    suspend fun listConversations(): Result<List<ConversationDto>> {
        return runCatching {
            requireSuccessfulData(conversationApi.listConversations())
        }
    }

    suspend fun listMessages(
        conversationId: String,
        beforeTime: Long? = null,
        limit: Int = 20
    ): Result<List<MessageDto>> {
        return runCatching {
            requireSuccessfulData(
                conversationApi.listMessages(
                    conversationId = conversationId,
                    beforeTime = beforeTime,
                    limit = limit
                )
            )
        }
    }

    suspend fun createSingleConversation(peerUserId: Long): Result<ConversationDto> {
        return runCatching {
            requireSuccessfulData(
                conversationApi.createSingleConversation(CreateSingleConversationRequest(peerUserId))
            )
        }
    }

    suspend fun openGroupConversation(groupId: Long): Result<ConversationDto> {
        return runCatching {
            requireSuccessfulData(conversationApi.openGroupConversation(groupId))
        }
    }

    suspend fun markRead(conversationId: String, lastReadTime: Long?): Result<Unit> {
        return runCatching {
            requireSuccessful(
                conversationApi.markRead(
                    conversationId = conversationId,
                    request = MarkConversationReadRequest(lastReadTime)
                )
            )
        }
    }

    suspend fun setHidden(conversationId: String, hidden: Boolean): Result<Unit> {
        return runCatching {
            requireSuccessful(
                conversationApi.setHidden(conversationId, SetConversationHiddenRequest(hidden))
            )
        }
    }

    private fun requireSuccessful(response: com.example.grabthisforme.model.network.ApiResponse<*>) {
        if (response.code != 0) {
            error(response.message.ifBlank { "Network request failed" })
        }
    }

    private fun <T> requireSuccessfulData(response: com.example.grabthisforme.model.network.ApiResponse<T>): T {
        val data = response.data
        if (response.code != 0 || data == null) {
            error(response.message.ifBlank { "Network request failed" })
        }
        return data
    }
}
