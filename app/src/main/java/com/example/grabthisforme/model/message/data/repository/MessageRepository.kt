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
    private val chatRealtimeManager: ChatRealtimeManager
) {
    companion object {
        const val MESSAGE_PAGE_SIZE = 20
        private const val REALTIME_ACK_TAG = "RealtimeAckDiag"
    }
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    @Volatile
    private var activeConversationId: String? = null

    fun initializeRealtimeSync() = Unit

    init {
        repositoryScope.launch {
            chatRealtimeManager.events.collect { event ->
                if (event is ChatRealtimeEvent.MessageReceived) {
                    handleIncomingRealtimeMessage(event.conversationId, event.message)
                    chatRealtimeManager.ack(event.stompAckId, event.deliveryAckId)
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
        val pendingMessage = localRepository.createPendingTextMessage(conversationId, text)
        return remoteRepository.sendTextMessage(conversationId, pendingMessage.clientMsgId, text)
            .mapCatching { remoteMessage ->
                localRepository.markMessageSent(pendingMessage.clientMsgId, remoteMessage)
            }
            .getOrElse {
                localRepository.markMessageStatus(
                    pendingMessage.clientMsgId,
                    Message.MessageStatus.FAILED
                )
                pendingMessage.copy(status = Message.MessageStatus.FAILED)
            }
    }

    suspend fun sendImageMessage(conversationId: String, mediaUrl: String): Message {
        val pendingMessage = localRepository.createPendingImageMessage(conversationId, mediaUrl)
        return remoteRepository.sendImageMessage(conversationId, pendingMessage.clientMsgId, mediaUrl)
            .mapCatching { remoteMessage ->
                localRepository.markMessageSent(pendingMessage.clientMsgId, remoteMessage)
            }
            .getOrElse {
                localRepository.markMessageStatus(
                    pendingMessage.clientMsgId,
                    Message.MessageStatus.FAILED
                )
                pendingMessage.copy(status = Message.MessageStatus.FAILED)
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
            ?.sortedWith(compareBy<Message> { it.serverTimestamp ?: it.timestamp }.thenBy { it.clientMsgId })
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
        val oldestTimestamp = currentSnapshot
            .minOfOrNull { message -> message.serverTimestamp ?: message.timestamp }
        val page = conversationRemoteRepository.listMessages(
            conversationId = conversationId,
            beforeTime = oldestTimestamp,
            limit = MESSAGE_PAGE_SIZE
        )
            .getOrNull()
            ?.map { dto -> dto.toDomain() }
            ?.sortedWith(compareBy<Message> { it.serverTimestamp ?: it.timestamp }.thenBy { it.clientMsgId })
            .orEmpty()

        if (page.isEmpty()) return false
        ensureMessageSendersCached(page)
        localRepository.replaceMessages(conversationId, page)
        return page.size >= MESSAGE_PAGE_SIZE
    }

    private suspend fun handleIncomingRealtimeMessage(conversationId: String, message: Message) {
        message.serverMsgId?.let { serverMsgId ->
            if (localRepository.hasServerMessage(serverMsgId)) return
        }
        if (localRepository.hasMessage(message.clientMsgId)) {
            if (message.status == Message.MessageStatus.SENT) {
                localRepository.markMessageSent(message.clientMsgId, message)
            }
            return
        }

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
                lastReadTime = message.serverTimestamp ?: message.timestamp
            )
            conversationRepository.markConversationAsRead(
                conversationId = conversationId,
                lastReadTime = message.serverTimestamp ?: message.timestamp
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

