package com.example.grabthisforme.model.message.data.repository

import com.example.grabthisforme.model.message.data.network.api.MessageApi
import com.example.grabthisforme.model.message.data.network.api.SendMessageRequest
import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.message.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRemoteRepository @Inject constructor(
    private val messageApi: MessageApi
) {

    suspend fun sendTextMessage(conversationId: String, text: String): Result<Message> {
        return runCatching {
            requireSuccessfulMessage(
                messageApi.sendMessage(
                    conversationId = conversationId,
                    request = SendMessageRequest(
                        type = Message.MessageType.TEXT.name,
                        content = text.trim()
                    )
                )
            )
        }
    }

    suspend fun sendImageMessage(conversationId: String, mediaUrl: String): Result<Message> {
        return runCatching {
            requireSuccessfulMessage(
                messageApi.sendMessage(
                    conversationId = conversationId,
                    request = SendMessageRequest(
                        type = Message.MessageType.IMAGE.name,
                        mediaUrl = mediaUrl
                    )
                )
            )
        }
    }

    private fun requireSuccessfulMessage(response: com.example.grabthisforme.model.network.ApiResponse<com.example.grabthisforme.model.message.data.network.dto.MessageDto>): Message {
        val data = response.data ?: error(response.message.ifBlank { "Network request failed" })
        if (response.code != 0) {
            error(response.message.ifBlank { "Network request failed" })
        }
        return data.toDomain() ?: error("Network request returned empty message body")
    }
}
