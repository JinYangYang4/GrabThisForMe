package com.example.grabthisforme.model.conversation.data.repository

import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.conversation.data.network.dto.ConversationDto
import com.example.grabthisforme.model.conversation.data.network.dto.ConversationParticipantDto
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.conversation.mapper.buildConversationPeer
import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.message.mapper.toDomain
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class ConversationRepository @Inject constructor(
    private val localRepository: ConversationLocalRepository,
    private val remoteRepository: ConversationRemoteRepository,
    private val userRepository: UserRepository
) {
    val allConversations: StateFlow<List<Conversation>> = localRepository.allConversations

    val currentUserConversationStates: StateFlow<List<ConversationUserStateEntity>> =
        localRepository.currentUserConversationStates

    suspend fun saveConversation(conversation: Conversation) {
        localRepository.saveConversation(conversation)
    }

    suspend fun findOrCreateSingleConversation(peerUser: User): Conversation {
        return remoteRepository.createSingleConversation(peerUser.id)
            .mapCatching { dto ->
                syncConversationFromRemote(dto)
            }
            .getOrElse {
                localRepository.findOrCreateSingleConversation(peerUser)
            }
    }

    suspend fun findOrCreateGroupConversation(
        groupId: Long,
        members: List<User>
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
        remoteRepository.setHidden(conversationId, hidden)
        localRepository.setConversationHidden(conversationId, hidden)
    }

    suspend fun markConversationAsRead(conversationId: String) {
        remoteRepository.markRead(conversationId)
        localRepository.markConversationAsRead(conversationId)
    }

    suspend fun refreshRemoteConversations() {
        val conversations = remoteRepository.listConversations().getOrNull() ?: return
        conversations.forEach { dto ->
            syncConversationFromRemote(dto)
        }
    }

    suspend fun syncRemoteConversationSnapshot(
        conversationId: String,
        lastMessage: Message,
        unreadCount: Int = 0,
        isHidden: Boolean = false
    ) {
        val existing = localRepository.getConversationById(conversationId)
        val conversation = existing?.copy(
            lastMessage = lastMessage,
            lastTime = lastMessage.timestamp
        ) ?: Conversation(
            conversationId = conversationId,
            lastMessage = lastMessage,
            lastTime = lastMessage.timestamp
        )
        localRepository.syncRemoteConversation(conversation, unreadCount, isHidden)
    }

    private suspend fun syncConversationFromRemote(dto: ConversationDto): Conversation {
        val participants = dto.participants.map { participant ->
            participant.toDomain()
        }
        if (participants.isNotEmpty()) {
            userRepository.upsertUsers(participants)
        }

        val currentUserId = userRepository.currentUserId.value
        val peerUsers = if (currentUserId == null) {
            participants
        } else {
            participants.filterNot { participant -> participant.id == currentUserId }
        }

        val conversationType = if (dto.conversationType.equals("GROUP", ignoreCase = true)) {
            Conversation.ConversationType.GROUP
        } else {
            Conversation.ConversationType.SINGLE
        }

        val conversation = Conversation(
            conversationId = dto.conversationId,
            type = conversationType,
            targetId = dto.targetId,
            conversationPeer = buildConversationPeer(
                conversationType = dto.conversationType,
                users = peerUsers
            ),
            lastMessage = dto.lastMessage.toDomain(),
            lastTime = dto.lastTime
        )
        localRepository.syncRemoteConversation(conversation, dto.unreadCount, dto.isHidden)
        return conversation
    }

    private fun ConversationParticipantDto.toDomain(): User {
        return User(
            id = id,
            name = name ?: accountName ?: id.toString(),
            headPic = headPic.orEmpty(),
            phone = phone,
            email = email,
            gender = gender ?: 2,
            createTime = createTime ?: System.currentTimeMillis(),
            isVip = isVip ?: false,
            signature = signature,
            accountName = accountName ?: id.toString(),
            lastLoginTime = lastLoginTime
        )
    }
}
