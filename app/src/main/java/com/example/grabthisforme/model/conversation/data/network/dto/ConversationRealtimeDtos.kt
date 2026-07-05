package com.example.grabthisforme.model.conversation.data.network.dto

import com.example.grabthisforme.model.friendAndGroup.data.network.dto.FriendRequestDto
import com.example.grabthisforme.model.message.data.network.dto.MessageDto

data class ConversationSocketPayloadDto(
    val type: String,
    val conversationId: String? = null,
    val message: MessageDto? = null,
    val friendRequest: FriendRequestDto? = null
)
