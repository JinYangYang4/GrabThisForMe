package com.example.grabthisforme.model.conversation.data.network.dto

import com.example.grabthisforme.model.message.data.network.dto.MessageDto

data class ConversationDto(
    val conversationId: String,
    val conversationType: String = "SINGLE",
    val targetId: Long? = null,
    val peerUserIds: List<Long> = emptyList(),
    val lastMessage: MessageDto,
    val lastTime: Long,
    val unreadCount: Int = 0,
    val isHidden: Boolean = false,
    val participants: List<ConversationParticipantDto> = emptyList()
)
