package com.example.grabthisforme.model.message.mapper

import com.example.grabthisforme.model.message.data.local.entity.MessageEntity
import com.example.grabthisforme.model.message.data.network.dto.MessageDto
import com.example.grabthisforme.model.message.domain.Message

private fun String.toMessageType(): Message.MessageType {
    return Message.MessageType.entries.firstOrNull { it.name == this }
        ?: Message.MessageType.TEXT
}

private fun String.toMessageStatus(): Message.MessageStatus {
    return Message.MessageStatus.entries.firstOrNull { it.name == this }
        ?: Message.MessageStatus.SENT
}

fun MessageDto.toDomain(): Message {
    return Message(
        messageId = messageId,
        senderId = senderId,
        type = type.toMessageType(),
        content = content,
        mediaUrl = mediaUrl,
        timestamp = timestamp,
        status = status.toMessageStatus()
    )
}

fun MessageDto?.toDomainOrNull(): Message? = this?.toDomain()

fun Message.toDto(): MessageDto {
    return MessageDto(
        messageId = messageId,
        senderId = senderId,
        type = type.name,
        content = content,
        mediaUrl = mediaUrl,
        timestamp = timestamp,
        status = status.name
    )
}

fun Message?.toDtoOrNull(): MessageDto? = this?.toDto()

fun MessageEntity.toDomain(): Message {
    return Message(
        messageId = messageId,
        senderId = senderId ?: 0L,
        type = type.toMessageType(),
        content = content,
        mediaUrl = mediaUrl,
        timestamp = timestamp,
        status = status.toMessageStatus()
    )
}

fun Message.toEntity(conversationId: String): MessageEntity {
    return MessageEntity(
        messageId = messageId,
        conversationId = conversationId,
        senderId = senderId.takeIf { it > 0L },
        type = type.name,
        content = content,
        mediaUrl = mediaUrl,
        timestamp = timestamp,
        status = status.name
    )
}
