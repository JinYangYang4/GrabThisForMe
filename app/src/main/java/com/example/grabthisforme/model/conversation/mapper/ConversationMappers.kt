package com.example.grabthisforme.model.conversation.mapper

import com.example.grabthisforme.model.conversation.data.dto.ConversationDto
import com.example.grabthisforme.model.conversation.data.entity.ConversationBundleEntity
import com.example.grabthisforme.model.conversation.data.entity.ConversationEntity
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.messageContent.domain.MessageContent
import com.example.grabthisforme.model.messageContent.mapper.toDomain
import com.example.grabthisforme.model.messageContent.mapper.toDto
import com.example.grabthisforme.model.user.domain.User

private fun String.toConversationType(): Conversation.ConversationType {
    return Conversation.ConversationType.entries.firstOrNull { it.name == this }
        ?: Conversation.ConversationType.SINGLE
}

private fun Conversation.ConversationType.asStoredName(): String = name

private fun List<Long>.toCsv(): String = joinToString(",")

private fun String.toIdList(): List<Long> =
    split(",").mapNotNull { it.trim().takeIf(String::isNotBlank)?.toLongOrNull() }

private fun String.normalizePeerType(): String = if (uppercase() == "GROUP") "GROUP" else "SINGLE"

private fun buildPlaceholderUser(userId: Long): User {
    return User(
        id = userId,
        name = "",
        headPic = ""
    )
}

private fun buildConversationPeer(peerType: String, peerUserIds: List<Long>): Conversation.ConversationPeer {
    return when (peerType.normalizePeerType()) {
        "GROUP" -> Conversation.ConversationPeer.Group(peerUserIds.map { buildPlaceholderUser(it) })
        else -> Conversation.ConversationPeer.Single(peerUserIds.firstOrNull()?.let(::buildPlaceholderUser))
    }
}

private fun buildPeerIds(peer: Conversation.ConversationPeer): List<Long> {
    return when (peer) {
        is Conversation.ConversationPeer.Single -> listOfNotNull(peer.user?.id)
        is Conversation.ConversationPeer.Group -> peer.users.map { it.id }
    }
}

fun ConversationDto.toDomain(): Conversation {
    return Conversation(
        conversationId = conversationId,
        type = type.toConversationType(),
        conversationPeer = buildConversationPeer(peerType, peerUserIds),
        unreadCount = unreadCount,
        lastMessage = lastMessage.toDomain(),
        lastTime = lastTime
    )
}

fun Conversation.toDto(): ConversationDto {
    return ConversationDto(
        conversationId = conversationId,
        type = type.asStoredName(),
        peerType = when (conversationPeer) {
            is Conversation.ConversationPeer.Group -> "GROUP"
            is Conversation.ConversationPeer.Single -> "SINGLE"
        },
        peerUserIds = buildPeerIds(conversationPeer),
        unreadCount = unreadCount,
        lastMessage = lastMessage.toDto(),
        lastTime = lastTime
    )
}

fun ConversationEntity.toDomain(lastMessage: MessageContent): Conversation {
    return Conversation(
        conversationId = conversationId,
        type = type.toConversationType(),
        conversationPeer = buildConversationPeer(peerType, peerUserIdsCsv.toIdList()),
        unreadCount = unreadCount,
        lastMessage = lastMessage,
        lastTime = lastTime
    )
}

fun ConversationBundleEntity.toDomain(): Conversation {
    return conversation.toDomain(
        lastMessage = lastMessage?.toDomain()
            ?: MessageContent(
                messageId = conversation.lastMessageId,
                senderId = 0L,
                type = MessageContent.MessageType.TEXT,
                content = "",
                timestamp = conversation.lastTime,
                isMine = false,
                status = MessageContent.MessageStatus.SENT
            )
    )
}

fun Conversation.toEntity(): ConversationEntity {
    return ConversationEntity(
        conversationId = conversationId,
        type = type.asStoredName(),
        peerType = when (conversationPeer) {
            is Conversation.ConversationPeer.Group -> "GROUP"
            is Conversation.ConversationPeer.Single -> "SINGLE"
        },
        peerUserIdsCsv = buildPeerIds(conversationPeer).toCsv(),
        unreadCount = unreadCount,
        lastMessageId = lastMessage.messageId,
        lastTime = lastTime
    )
}
