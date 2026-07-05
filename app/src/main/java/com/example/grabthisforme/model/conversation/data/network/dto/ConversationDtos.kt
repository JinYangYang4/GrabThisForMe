package com.example.grabthisforme.model.conversation.data.network.dto

import com.example.grabthisforme.model.message.data.network.dto.MessageDto
import com.example.grabthisforme.model.user.data.network.dto.UserBriefDto

data class ConversationDto(
    val conversationId: String,
    val conversationType: String = "SINGLE",
    val targetId: Long? = null,
    val peerUserIds: List<Long> = emptyList(),
    val lastMessage: MessageDto? = null,
    val lastTime: Long,
    val unreadCount: Int = 0,
    val isHidden: Boolean = false,
    val lastReadTime: Long? = null,
    val participants: List<UserBriefDto> = emptyList()
)
