package com.example.grabthisforme.model.conversation.data.repository

import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.conversation.domain.Conversation
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class ConversationRepository @Inject constructor(
    private val localRepository: ConversationLocalRepository,
    private val remoteRepository: ConversationRemoteRepository
) {
    val allConversations: StateFlow<List<Conversation>> = localRepository.allConversations

    val currentUserConversationStates: StateFlow<List<ConversationUserStateEntity>> =
        localRepository.currentUserConversationStates

    suspend fun saveConversation(conversation: Conversation) {
        localRepository.saveConversation(conversation)
    }

    suspend fun findOrCreateSingleConversation(peerUser: com.example.grabthisforme.model.user.domain.User): Conversation {
        return localRepository.findOrCreateSingleConversation(peerUser)
    }

    suspend fun findOrCreateGroupConversation(
        groupId: Long,
        members: List<com.example.grabthisforme.model.user.domain.User>
    ): Conversation {
        return localRepository.findOrCreateGroupConversation(groupId, members)
    }

    suspend fun getConversationById(conversationId: String): Conversation? {
        return localRepository.getConversationById(conversationId)
    }

    suspend fun getAllConversations(): List<Conversation> {
        return localRepository.getAllConversations()
    }

    suspend fun deleteConversationById(conversationId: String) {
        localRepository.deleteConversationById(conversationId)
    }

    suspend fun setConversationHidden(conversationId: String, hidden: Boolean) {
        localRepository.setConversationHidden(conversationId, hidden)
    }

    suspend fun markConversationAsRead(conversationId: String) {
        localRepository.markConversationAsRead(conversationId)
    }
}
