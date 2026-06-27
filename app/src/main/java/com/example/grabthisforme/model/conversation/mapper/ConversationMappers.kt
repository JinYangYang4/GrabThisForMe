package com.example.grabthisforme.model.conversation.mapper

import com.example.grabthisforme.model.conversation.data.local.entity.ConversationBundleEntity
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationEntity
import com.example.grabthisforme.model.conversation.data.network.dto.ConversationDto
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.message.mapper.toDomain
import com.example.grabthisforme.model.message.mapper.toDomainOrNull
import com.example.grabthisforme.model.message.mapper.toDtoOrNull
import com.example.grabthisforme.model.user.domain.User

private fun String.toConversationType(): Conversation.ConversationType {
    return Conversation.ConversationType.entries.firstOrNull { it.name == this }
        ?: Conversation.ConversationType.SINGLE
}

private fun Conversation.ConversationType.asStoredName(): String = name

private fun String.normalizeConversationType(): String = if (uppercase() == "GROUP") "GROUP" else "SINGLE"

private fun buildPlaceholderUser(userId: Long): User {
    return User(
        id = userId,
        name = "",
        headPic = ""
    )
}

fun buildConversationPeerFromIds(
    conversationType: String,
    peerUserIds: List<Long>
): Conversation.ConversationPeer {
    return buildConversationPeer(
        conversationType = conversationType,
        users = peerUserIds.map { buildPlaceholderUser(it) }
    )
}

fun buildConversationPeer(
    conversationType: String,
    users: List<User>
): Conversation.ConversationPeer {
    return when (conversationType.normalizeConversationType()) {
        "GROUP" -> Conversation.ConversationPeer.Group(users)
        else -> Conversation.ConversationPeer.Single(users.firstOrNull())
    }
}

fun buildPeerIds(peer: Conversation.ConversationPeer): List<Long> {
    return when (peer) {
        is Conversation.ConversationPeer.Single -> listOfNotNull(peer.user?.id)
        is Conversation.ConversationPeer.Group -> peer.users.map { it.id }
    }
}

fun ConversationDto.toDomain(): Conversation {
    return Conversation(
        conversationId = conversationId,
        type = conversationType.toConversationType(),
        targetId = targetId,
        conversationPeer = buildConversationPeerFromIds(conversationType, peerUserIds),
        lastMessage = lastMessage.toDomainOrNull(),
        lastTime = lastTime
    )
}

fun Conversation.toDto(): ConversationDto {
    return ConversationDto(
        conversationId = conversationId,
        conversationType = type.asStoredName(),
        targetId = targetIdOrNull(),
        peerUserIds = buildPeerIds(conversationPeer),
        lastMessage = lastMessage.toDtoOrNull(),
        lastTime = lastTime
    )
}

fun ConversationEntity.toDomain(lastMessage: Message?, peerUsers: List<User>): Conversation {
    return Conversation(
        conversationId = conversationId,
        type = conversationType.toConversationType(),
        targetId = targetId,
        conversationPeer = buildConversationPeer(conversationType, peerUsers),
        lastMessage = lastMessage,
        lastTime = lastTime
    )
}

fun ConversationBundleEntity.toDomain(peerUsers: List<User>): Conversation {
    return conversation.toDomain(
        lastMessage = lastMessage?.toDomain(),
        peerUsers = peerUsers
    )
}

fun Conversation.toEntity(): ConversationEntity {
    return ConversationEntity(
        conversationId = conversationId,
        conversationType = type.asStoredName(),
        targetId = targetIdOrNull(),
        lastMessageId = lastMessage?.messageId,
        lastTime = lastTime
    )
}

private fun Conversation.targetIdOrNull(): Long? {
    return targetId ?: when (val peer = conversationPeer) {
        is Conversation.ConversationPeer.Single -> peer.user?.id
        is Conversation.ConversationPeer.Group -> null
    }
}
