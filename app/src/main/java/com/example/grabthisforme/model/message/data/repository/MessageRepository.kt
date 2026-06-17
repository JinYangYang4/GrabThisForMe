package com.example.grabthisforme.model.message.data.repository

import com.example.grabthisforme.model.message.domain.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val localRepository: MessageLocalRepository,
    private val remoteRepository: MessageRemoteRepository
) {
    fun getMessagesByConversation(conversationId: String): Flow<List<Message>> {
        return localRepository.getMessagesByConversation(conversationId)
    }

    suspend fun sendMessage(conversationId: String, message: Message): Message {
        return localRepository.sendMessage(conversationId, message)
    }

    suspend fun sendTextMessage(conversationId: String, text: String): Message {
        return localRepository.sendTextMessage(conversationId, text)
    }

    suspend fun sendImageMessage(conversationId: String, mediaUrl: String): Message {
        return localRepository.sendImageMessage(conversationId, mediaUrl)
    }

    suspend fun deleteMessage(messageId: String) {
        localRepository.deleteMessage(messageId)
    }

    suspend fun deleteMessagesByConversation(conversationId: String) {
        localRepository.deleteMessagesByConversation(conversationId)
    }
}
