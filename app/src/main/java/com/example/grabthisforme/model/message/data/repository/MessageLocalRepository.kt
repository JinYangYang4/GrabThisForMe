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
    fun getMessagesByConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.observeMessageEntitiesByConversationId(conversationId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun sendMessage(conversationId: String, message: Message): Message {
        return upsertIncomingMessage(conversationId, message)
    }

    suspend fun upsertIncomingMessage(conversationId: String, message: Message): Message {
        conversationDao.insertConversationIfNotExists(
            ConversationEntity(
                conversationId = conversationId,
                conversationType = "SINGLE",
                targetId = null,
                lastMessageId = message.messageId,
                lastTime = message.timestamp
            )
        )
        messageDao.upsertMessage(message.toEntity(conversationId))
        conversationDao.updateLastMessage(
            conversationId = conversationId,
            messageId = message.messageId,
            timestamp = message.timestamp
        )
        updateConversationVisibilityAfterMessage(conversationId, message.senderId)
        return message
    }

    suspend fun sendTextMessage(conversationId: String, text: String): Message {
        val currentUserId = userRepository.currentUserId.value ?: 0L
        val message = Message(
            messageId = "MSG_${System.currentTimeMillis()}",
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
            messageId = "MSG_${System.currentTimeMillis()}",
            senderId = currentUserId,
            type = Message.MessageType.IMAGE,
            mediaUrl = mediaUrl,
            timestamp = System.currentTimeMillis(),
            status = Message.MessageStatus.SENT
        )
        return upsertIncomingMessage(conversationId, message)
    }

    suspend fun replaceMessages(conversationId: String, messages: List<Message>) {
        messageDao.deleteMessagesByConversationId(conversationId)
        if (messages.isEmpty()) return
        messageDao.upsertMessages(messages.map { it.toEntity(conversationId) })
        messages.lastOrNull()?.let { lastMessage ->
            conversationDao.updateLastMessage(conversationId, lastMessage.messageId, lastMessage.timestamp)
        }
    }

    suspend fun hasMessage(messageId: String): Boolean {
        return messageDao.countByMessageId(messageId) > 0
    }

    suspend fun markConversationReadForUser(conversationId: String, userId: Long) {
        conversationUserStateDao.updateUnreadCount(conversationId, userId, 0)
        conversationUserStateDao.updateHiddenState(conversationId, userId, false)
    }

    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }

    suspend fun deleteMessagesByConversation(conversationId: String) {
        messageDao.deleteMessagesByConversationId(conversationId)
    }

    private suspend fun updateConversationVisibilityAfterMessage(conversationId: String, senderId: Long) {
        val participantIds = conversationRelationDao.getParticipantUserIds(conversationId)
        if (participantIds.isEmpty()) return

        conversationUserStateDao.insertStatesIfAbsent(
            participantIds.distinct().map { userId ->
                ConversationUserStateEntity(
                    conversationId = conversationId,
                    userId = userId,
                    unreadCount = 0
                )
            }
        )

        conversationUserStateDao.updateHiddenState(
            conversationId = conversationId,
            userId = senderId,
            isHidden = false
        )

        val receiverIds = participantIds.filterNot { userId -> userId == senderId }.distinct()
        if (receiverIds.isNotEmpty()) {
            conversationUserStateDao.increaseUnreadCount(conversationId, receiverIds)
            conversationUserStateDao.updateHiddenStates(conversationId, receiverIds, false)
        }
    }
}
