package com.example.grabthisforme.model.message.data.repository
import com.example.grabthisforme.model.conversation.data.local.dao.ConversationDao
import com.example.grabthisforme.model.conversation.data.local.dao.ConversationUserStateDao
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationEntity
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.message.data.local.dao.MessageDao
import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.message.mapper.toDomain
import com.example.grabthisforme.model.message.mapper.toEntity
import com.example.grabthisforme.model.relation.data.dao.ConversationRelationDao
import com.example.grabthisforme.model.user.data.repository.UserRepository
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageLocalRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val conversationRelationDao: ConversationRelationDao,
    private val conversationUserStateDao: ConversationUserStateDao,
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "MessageCacheDiag"
    }

    fun getMessagesByConversation(conversationId: String): Flow<List<Message>> {
        val Messages =  messageDao.observeMessageEntitiesByConversationId(conversationId)
            .map { entities -> entities.map { it.toDomain() } }
        return Messages
    }

    suspend fun sendMessage(conversationId: String, message: Message): Message {
        return upsertIncomingMessage(conversationId, message)
    }

    suspend fun createPendingTextMessage(conversationId: String, text: String): Message {
        val currentUserId = userRepository.currentUserId.value ?: 0L
        val message = Message(
            clientMsgId = "LOCAL_${UUID.randomUUID()}",
            senderId = currentUserId,
            type = Message.MessageType.TEXT,
            content = text.trim(),
            timestamp = System.currentTimeMillis(),
            status = Message.MessageStatus.SENDING
        )
        return upsertIncomingMessage(conversationId, message)
    }

    suspend fun createPendingImageMessage(conversationId: String, mediaUrl: String): Message {
        val currentUserId = userRepository.currentUserId.value ?: 0L
        val message = Message(
            clientMsgId = "LOCAL_${UUID.randomUUID()}",
            senderId = currentUserId,
            type = Message.MessageType.IMAGE,
            mediaUrl = mediaUrl,
            timestamp = System.currentTimeMillis(),
            status = Message.MessageStatus.SENDING
        )
        return upsertIncomingMessage(conversationId, message)
    }

    suspend fun markMessageStatus(clientMsgId: String, status: Message.MessageStatus) {
        messageDao.updateMessageStatus(clientMsgId, status.name)
    }

    suspend fun markMessageSent(clientMsgId: String, remoteMessage: Message): Message {
        ensureMessageSenderCached(remoteMessage)
        messageDao.markMessageSent(
            clientMsgId = clientMsgId,
            serverMsgId = remoteMessage.serverMsgId ?: error("serverMsgId missing"),
            serverTimestamp = remoteMessage.serverTimestamp,
            status = remoteMessage.status.name
        )
        return messageDao.getMessageEntityByClientMsgId(clientMsgId)?.toDomain()
            ?: remoteMessage.copy(clientMsgId = clientMsgId)
    }

    suspend fun upsertIncomingMessage(conversationId: String, message: Message): Message {
        ensureMessageSenderCached(message)
        conversationDao.insertConversationIfNotExists(
            ConversationEntity(
                conversationId = conversationId,
                conversationType = "SINGLE",
                targetId = null,
                lastMessageId = message.clientMsgId,
                lastTime = message.serverTimestamp ?: message.timestamp
            )
        )
        messageDao.upsertMessage(message.toEntity(conversationId))
        conversationDao.updateLastMessage(
            conversationId = conversationId,
            messageId = message.clientMsgId,
            timestamp = message.serverTimestamp ?: message.timestamp
        )
        updateConversationVisibilityAfterMessage(conversationId, message.senderId)
        return message
    }

    suspend fun sendTextMessage(conversationId: String, text: String): Message {
        val currentUserId = userRepository.currentUserId.value ?: 0L
        val message = Message(
            clientMsgId = "LOCAL_${UUID.randomUUID()}",
            senderId = currentUserId,
            type = Message.MessageType.TEXT,
            content = text.trim(),
            timestamp = System.currentTimeMillis(),
            status = Message.MessageStatus.SENT
        )
        return upsertIncomingMessage(conversationId, message)
    }

    suspend fun sendImageMessage(conversationId: String, mediaUrl: String): Message {
        val currentUserId = userRepository.currentUserId.value ?: 0L
        val message = Message(
            clientMsgId = "LOCAL_${UUID.randomUUID()}",
            senderId = currentUserId,
            type = Message.MessageType.IMAGE,
            mediaUrl = mediaUrl,
            timestamp = System.currentTimeMillis(),
            status = Message.MessageStatus.SENT
        )
        return upsertIncomingMessage(conversationId, message)
    }

    suspend fun replaceMessages(conversationId: String, messages: List<Message>) {
        if (messages.isEmpty()) return
        ensureMessageSendersCached(messages)
        val mergedMessages = (
            messageDao.getMessageEntitiesByConversationId(conversationId).map { it.toDomain() } + messages
            )
            .associateBy { it.serverMsgId ?: it.clientMsgId }
            .values
            .sortedWith(compareBy<Message> { it.serverTimestamp ?: it.timestamp }.thenBy { it.clientMsgId })
        messageDao.upsertMessages(mergedMessages.map { it.toEntity(conversationId) })
        mergedMessages.lastOrNull()?.let { lastMessage ->
            conversationDao.updateLastMessage(
                conversationId,
                lastMessage.clientMsgId,
                lastMessage.serverTimestamp ?: lastMessage.timestamp
            )
        }
    }

    suspend fun hasMessage(clientMsgId: String): Boolean {
        return messageDao.countByClientMsgId(clientMsgId) > 0
    }

    suspend fun hasServerMessage(serverMsgId: String): Boolean {
        return messageDao.countByServerMsgId(serverMsgId) > 0
    }

    suspend fun markConversationReadForUser(conversationId: String, userId: Long, lastReadTime: Long?) {
        conversationUserStateDao.markRead(conversationId, userId, lastReadTime)
    }

    suspend fun deleteMessage(clientMsgId: String) {
        messageDao.deleteMessageByClientMsgId(clientMsgId)
    }

    suspend fun deleteMessagesByConversation(conversationId: String) {
        messageDao.deleteMessagesByConversationId(conversationId)
    }

    private suspend fun updateConversationVisibilityAfterMessage(conversationId: String, senderId: Long) {
        val currentUserId = userRepository.currentUserId.value ?: return
        conversationUserStateDao.insertStateIfAbsent(
            ConversationUserStateEntity(
                conversationId = conversationId,
                userId = currentUserId,
                unreadCount = 0
            )
        )
        if (senderId == currentUserId) {
            conversationUserStateDao.updateHiddenState(
                conversationId = conversationId,
                userId = currentUserId,
                isHidden = false
            )
            return
        }
        conversationUserStateDao.increaseUnreadCount(conversationId, listOf(currentUserId))
        conversationUserStateDao.updateHiddenState(conversationId, currentUserId, false)
    }

    private suspend fun ensureMessageSenderCached(message: Message) {
        ensureMessageSendersCached(listOf(message))
    }

    private suspend fun ensureMessageSendersCached(messages: List<Message>) {
        val cachedUsers = messages
            .mapNotNull { message ->
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

