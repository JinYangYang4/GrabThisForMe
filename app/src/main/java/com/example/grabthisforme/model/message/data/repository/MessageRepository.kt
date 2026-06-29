package com.example.grabthisforme.model.message.data.repository

import com.example.grabthisforme.model.chat.data.realtime.ChatRealtimeEvent
import com.example.grabthisforme.model.chat.data.realtime.ChatRealtimeManager
import com.example.grabthisforme.model.conversation.data.repository.ConversationRemoteRepository
import com.example.grabthisforme.model.conversation.data.repository.ConversationRepository
import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.message.mapper.toDomain
import com.example.grabthisforme.model.user.data.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class MessageRepository @Inject constructor(
    private val localRepository: MessageLocalRepository,
    private val remoteRepository: MessageRemoteRepository,
    private val conversationRemoteRepository: ConversationRemoteRepository,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository,
    chatRealtimeManager: ChatRealtimeManager
) {
    companion object {
        const val MESSAGE_PAGE_SIZE = 20
    }
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    @Volatile
    private var activeConversationId: String? = null

    init {
        repositoryScope.launch {
            chatRealtimeManager.events.collect { event ->
                if (event is ChatRealtimeEvent.MessageReceived) {
                    handleIncomingRealtimeMessage(event.conversationId, event.message)
                }
            }
        }
    }

    fun setActiveConversation(conversationId: String?) {
        activeConversationId = conversationId
    }

    fun getMessagesByConversation(conversationId: String): Flow<List<Message>> {
        return localRepository.getMessagesByConversation(conversationId)
    }

    suspend fun sendMessage(conversationId: String, message: Message): Message {
        return localRepository.upsertIncomingMessage(conversationId, message)
    }

    suspend fun sendTextMessage(conversationId: String, text: String): Message {
        return remoteRepository.sendTextMessage(conversationId, text)
            .mapCatching { message ->
                localRepository.upsertIncomingMessage(conversationId, message)
            }
            .getOrElse {
                localRepository.sendTextMessage(conversationId, text)
            }
    }

    suspend fun sendImageMessage(conversationId: String, mediaUrl: String): Message {
        return remoteRepository.sendImageMessage(conversationId, mediaUrl)
            .mapCatching { message ->
                localRepository.upsertIncomingMessage(conversationId, message)
            }
            .getOrElse {
                localRepository.sendImageMessage(conversationId, mediaUrl)
            }
    }

    suspend fun deleteMessage(messageId: String) {
        localRepository.deleteMessage(messageId)
    }

    suspend fun deleteMessagesByConversation(conversationId: String) {
        localRepository.deleteMessagesByConversation(conversationId)
    }

    suspend fun refreshConversationMessages(conversationId: String) {
        val messages = conversationRemoteRepository.listMessages(
            conversationId = conversationId,
            beforeTime = null,
            limit = MESSAGE_PAGE_SIZE
        )
            .getOrNull()
            ?.map { dto -> dto.toDomain() }
            ?.sortedWith(compareBy<Message> { it.timestamp }.thenBy { it.messageId })
            ?: return
        ensureMessageSendersCached(messages)
        localRepository.replaceMessages(conversationId, messages)
        messages.lastOrNull()?.let { lastMessage ->
            conversationRepository.syncRemoteConversationSnapshot(
                conversationId = conversationId,
                lastMessage = lastMessage
            )
        }
    }

    suspend fun loadOlderMessages(conversationId: String): Boolean {
        val currentMessages = localRepository.getMessagesByConversation(conversationId)
        val currentSnapshot = currentMessages.first()
        val oldestTimestamp = currentSnapshot.firstOrNull()?.timestamp
        val page = conversationRemoteRepository.listMessages(
            conversationId = conversationId,
            beforeTime = oldestTimestamp,
            limit = MESSAGE_PAGE_SIZE
        )
            .getOrNull()
            ?.map { dto -> dto.toDomain() }
            ?.sortedWith(compareBy<Message> { it.timestamp }.thenBy { it.messageId })
            .orEmpty()

        if (page.isEmpty()) return false
        ensureMessageSendersCached(page)
        localRepository.replaceMessages(conversationId, page)
        return page.size >= MESSAGE_PAGE_SIZE
    }

    private suspend fun handleIncomingRealtimeMessage(conversationId: String, message: Message) {
        if (localRepository.hasMessage(message.messageId)) return

        ensureMessageSendersCached(listOf(message))
        localRepository.upsertIncomingMessage(conversationId, message)

        if (conversationRepository.getConversationById(conversationId) == null) {
            conversationRepository.refreshRemoteConversations()
        }

        val currentUserId = userRepository.currentUserId.value ?: return
        if (message.senderId == currentUserId) return

        if (activeConversationId == conversationId) {
            localRepository.markConversationReadForUser(
                conversationId = conversationId,
                userId = currentUserId,
                lastReadTime = message.timestamp
            )
            conversationRepository.markConversationAsRead(
                conversationId = conversationId,
                lastReadTime = message.timestamp
            )
        } else {
            conversationRepository.syncRemoteConversationSnapshot(
                conversationId = conversationId,
                lastMessage = message
            )
        }
    }

    private suspend fun ensureMessageSendersCached(messages: List<Message>) {
        val cachedUsers = messages.mapNotNull { message ->
            message.senderId
                .takeIf { it > 0L }
                ?.let { senderId ->
                    com.example.grabthisforme.model.user.domain.User(
                        id = senderId,
                        name = "",
                        headPic = "",
                        accountName = senderId.toString(),
                        isLoginAccount = false
                    )
                }
        }
        userRepository.ensureCachedUsers(cachedUsers)
    }
}
