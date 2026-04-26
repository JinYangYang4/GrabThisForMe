package com.example.grabthisforme.model.conversation.data.dto

import com.example.grabthisforme.model.messageContent.data.dto.MessageDto

data class ConversationDto(
    val conversationId: String,
    val type: String = "SINGLE",
    val peerType: String = "SINGLE",
    val peerUserIds: List<Long> = emptyList(),
    val unreadCount: Int,
    val lastMessage: MessageDto,
    val lastTime: Long
)
