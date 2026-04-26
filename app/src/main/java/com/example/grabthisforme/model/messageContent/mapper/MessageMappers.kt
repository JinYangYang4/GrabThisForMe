package com.example.grabthisforme.model.messageContent.mapper

import com.example.grabthisforme.model.messageContent.data.dto.MessageDto
import com.example.grabthisforme.model.messageContent.data.entity.MessageEntity
import com.example.grabthisforme.model.messageContent.domain.MessageContent

private fun String.toMessageType(): MessageContent.MessageType {
    return MessageContent.MessageType.entries.firstOrNull { it.name == this }
        ?: MessageContent.MessageType.TEXT
}

private fun String.toMessageStatus(): MessageContent.MessageStatus {
    return MessageContent.MessageStatus.entries.firstOrNull { it.name == this }
        ?: MessageContent.MessageStatus.SENT
}

fun MessageDto.toDomain(): MessageContent {
    return MessageContent(
        messageId = messageId,
        senderId = senderId,
        type = type.toMessageType(),
        content = content,
        mediaUrl = mediaUrl,
        timestamp = timestamp,
        need_show_time = need_show_time,
        isMine = isMine,
        status = status.toMessageStatus()
    )
}

fun MessageContent.toDto(): MessageDto {
    return MessageDto(
        messageId = messageId,
        senderId = senderId,
        type = type.name,
        content = content,
        mediaUrl = mediaUrl,
        timestamp = timestamp,
        need_show_time = need_show_time,
        isMine = isMine,
        status = status.name
    )
}

fun MessageEntity.toDomain(): MessageContent {
    return MessageContent(
        messageId = messageId,
        senderId = senderId,
        type = type.toMessageType(),
        content = content,
        mediaUrl = mediaUrl,
        timestamp = timestamp,
        need_show_time = need_show_time,
        isMine = isMine,
        status = status.toMessageStatus()
    )
}

fun MessageContent.toEntity(conversationId: String): MessageEntity {
    return MessageEntity(
        messageId = messageId,
        conversationId = conversationId,
        senderId = senderId,
        type = type.name,
        content = content,
        mediaUrl = mediaUrl,
        timestamp = timestamp,
        need_show_time = need_show_time,
        isMine = isMine,
        status = status.name
    )
}
