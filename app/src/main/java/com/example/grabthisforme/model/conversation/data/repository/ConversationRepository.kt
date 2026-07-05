package com.example.grabthisforme.model.conversation.data.repository
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.conversation.data.network.dto.ConversationDto
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.conversation.mapper.buildConversationPeer
import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.message.mapper.toDomainOrNull
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class ConversationRepository @Inject constructor(
    private val localRepository: ConversationLocalRepository,
    private val remoteRepository: ConversationRemoteRepository,
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "ConversationRemoteDiag"
    }
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allConversations: StateFlow<List<Conversation>> = localRepository.allConversations

    val currentUserConversationStates: StateFlow<List<ConversationUserStateEntity>> =
        localRepository.currentUserConversationStates

    val totalUnreadCount: StateFlow<Int> = currentUserConversationStates
        .map { states ->
            states.sumOf { state -> state.unreadCount.coerceAtLeast(0) }
        }
        .stateIn(repositoryScope, SharingStarted.Eagerly, 0)

    suspend fun saveConversation(conversation: Conversation) {
        localRepository.saveConversation(conversation)
    }

    suspend fun findOrCreateSingleConversation(peerUser: User): Result<Conversation> {
        localRepository.findSingleConversationByPeerId(peerUser.id)?.let { localConversation ->
            return Result.success(localConversation)
        }
        return remoteRepository.createSingleConversation(peerUser.id)
            .mapCatching { dto ->
                syncConversationFromRemote(dto)
            }
    }

    suspend fun findOrCreateGroupConversation(
        groupId: Long,
        members: List<User>
    ): Result<Conversation> {
        localRepository.getAllConversations().firstOrNull { conversation ->
            conversation.type == Conversation.ConversationType.GROUP && conversation.targetId == groupId
        }?.let { localConversation ->
            return Result.success(localConversation)
        }
        return remoteRepository.openGroupConversation(groupId)
            .mapCatching { dto ->
                syncConversationFromRemote(dto)
            }
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

    suspend fun markConversationAsRead(conversationId: String, lastReadTime: Long? = null) {
        remoteRepository.markRead(conversationId, lastReadTime)
        localRepository.markConversationAsRead(conversationId, lastReadTime)
    }

    suspend fun refreshRemoteConversations() {
        val conversations = remoteRepository.listConversations().getOrNull() ?: return
        val syncedConversations = conversations.map { dto ->
            syncConversationFromRemote(dto)
        }
        val currentUserId = userRepository.currentUserId.value ?: return
        localRepository.syncCurrentUserConversationMembership(
            remoteConversationIds = syncedConversations.map { it.conversationId }.toSet(),
            remoteStatesByConversationId = conversations.associate { dto ->
                dto.conversationId to ConversationUserStateEntity(
                    conversationId = dto.conversationId,
                    userId = currentUserId,
                    unreadCount = dto.unreadCount,
                    isHidden = dto.isHidden,
                    lastReadTime = dto.lastReadTime
                )
            }
        )
    }

    suspend fun syncRemoteConversationSnapshot(
        conversationId: String,
        lastMessage: Message?,
        unreadCount: Int = 0,
        isHidden: Boolean = false
    ) {
        val existing = localRepository.getConversationById(conversationId)
        val existingState = localRepository.getCurrentUserConversationState(conversationId)
        val resolvedLastTime = lastMessage?.timestamp ?: existing?.lastTime ?: 0L
        val conversation = existing?.copy(
            lastMessage = lastMessage,
            lastTime = resolvedLastTime
        ) ?: Conversation(
            conversationId = conversationId,
            lastMessage = lastMessage,
            lastTime = resolvedLastTime
        )
        localRepository.syncRemoteConversation(
            conversation = conversation,
            unreadCount = existingState?.unreadCount ?: unreadCount,
            isHidden = existingState?.isHidden ?: isHidden,
            lastReadTime = existingState?.lastReadTime
        )
    }

    private suspend fun syncConversationFromRemote(dto: ConversationDto): Conversation {
        val participants = dto.participants.map { participant ->
            participant.toDomain()
        }
        if (participants.isNotEmpty()) {
            userRepository.ensureCachedUsers(participants)
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

        val resolvedTargetId = when (conversationType) {
            Conversation.ConversationType.GROUP -> dto.targetId
            Conversation.ConversationType.SINGLE -> {
                val peerUserId = peerUsers.firstOrNull()?.id
                if (dto.targetId == currentUserId && peerUserId != null) {
                }
                peerUserId ?: dto.targetId
            }
        }

        val conversation = Conversation(
            conversationId = dto.conversationId,
            type = conversationType,
            targetId = resolvedTargetId,
            conversationPeer = buildConversationPeer(
                conversationType = dto.conversationType,
                users = peerUsers
            ),
            lastMessage = dto.lastMessage.toDomainOrNull(),
            lastTime = dto.lastMessage?.timestamp ?: dto.lastTime
        )
        localRepository.syncRemoteConversation(
            conversation = conversation,
            unreadCount = dto.unreadCount,
            isHidden = dto.isHidden,
            lastReadTime = dto.lastReadTime
        )
        return conversation
    }
}

