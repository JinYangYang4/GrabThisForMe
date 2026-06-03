package com.example.grabthisforme.model.conversation.data.dto

import com.example.grabthisforme.model.message.data.dto.MessageDto

data class ConversationDto(
    val conversationId: String,
    val conversationType: String = "SINGLE",
    val targetId: Long? = null,
    val peerUserIds: List<Long> = emptyList(),
    val lastMessage: MessageDto,
    val lastTime: Long
)
